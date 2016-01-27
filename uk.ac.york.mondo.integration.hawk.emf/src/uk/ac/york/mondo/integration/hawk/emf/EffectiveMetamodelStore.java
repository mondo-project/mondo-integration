/*******************************************************************************
 * Copyright (c) 2015 University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antonio Garcia-Dominguez - initial API and implementation
 *******************************************************************************/
package uk.ac.york.mondo.integration.hawk.emf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * In-memory representation of a collection of effective metamodels in Hawk.
 *
 * By default, all metamodels and types are included. As soon as one type is
 * added, the effective metamodel will only include the explicitly added types.
 *
 * The contents of this effective metamodel can only be changed through the
 * {@link #addType(String, String)}, {@link #addType(String, String, Set)} and
 */
public class EffectiveMetamodelStore {

	private Map<String, EffectiveMetamodel> store = new HashMap<>();

	/**
	 * Adds or replaces a type to the effective metamodels, including a specific
	 * set of slots (which might be empty). Returns the previous set of fields
	 * included for the type, or <code>null</code> if the type wasn't part of
	 * the empty metamodel yet.
	 */
	public ImmutableSet<String> addType(String metamodel, String type, ImmutableSet<String> slots) {
		final EffectiveMetamodel mmEntry = getOrPutMetamodel(metamodel);
		return mmEntry.addType(type, slots);
	}

	/**
	 * Convenience method for {@link #addType(String, String)} that adds all
	 * the fields in the type.
	 */
	public ImmutableSet<String> addType(String metamodel, String type) {
		final EffectiveMetamodel mmEntry = getOrPutMetamodel(metamodel);
		return mmEntry.addType(type);
	}

	/**
	 * Removes a type from the effective metamodels, if it was included.
	 * 
	 * @return The set of previously included fields for the removed type, or
	 *         <code>null</code> if the type was not previously included.
	 */
	public ImmutableSet<String> removeType(String metamodel, String type) {
		EffectiveMetamodel mmEntry = store.get(metamodel);
		if (mmEntry == null) {
			return null;
		}
		return mmEntry.removeType(type);
	}

	/**
	 * Returns <code>true</code> if all metamodels, types and fields are
	 * included. This is the default state before any types are added.
	 */
	public boolean isEverythingIncluded() {
		return store.isEmpty();
	}

	/**
	 * Returns an unmodifiable view of all the explicitly included metamodels.
	 * Callers should check {@link #isEverythingIncluded()} first.
	 */
	public Map<String, EffectiveMetamodel> getIncludedMetamodels() {
		return Collections.unmodifiableMap(store);
	}

	/**
	 * Returns a raw unmodifiable view of all the explicitly
	 * included metamodels. Callers should check {@link #isEverythingIncluded()} first.
	 */
	public Map<String, Map<String, Set<String>>> getRawIncludedMetamodels() {
		final Map<String, Map<String, Set<String>>> ret = new HashMap<>();
		for (Entry<String, EffectiveMetamodel> entry : store.entrySet()) {
			ret.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue().store));
		}
		return Collections.unmodifiableMap(ret);
	}

	/**
	 * Returns <code>true</code> if the type is included in the effective
	 * metamodel, <code>false</code> otherwise.
	 */
	public boolean isTypeIncluded(String metamodel, String type) {
		if (isEverythingIncluded()) {
			return true;
		} else {
			EffectiveMetamodel mmEntry = store.get(metamodel);
			return mmEntry != null && mmEntry.isTypeIncluded(type);
		}
	}

	/**
	 * Returns <code>true</code>  if the slot is included in the effective
	 * metamodel, <code>false</code> otherwise.
	 */
	public boolean isSlotIncluded(String metamodel, String type, String slot) {
		if (isEverythingIncluded()) {
			return true;
		}

		EffectiveMetamodel mmEntry = store.get(metamodel);
		if (mmEntry == null) {
			return false;
		}

		return mmEntry.isSlotIncluded(type, slot);
	}

	protected EffectiveMetamodel getOrPutMetamodel(String metamodel) {
		EffectiveMetamodel mmEntry = store.get(metamodel);
		if (mmEntry == null) {
			mmEntry = new EffectiveMetamodel();
			store.put(metamodel, mmEntry);
		}
		return mmEntry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((store == null) ? 0 : store.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EffectiveMetamodelStore other = (EffectiveMetamodelStore) obj;
		if (store == null) {
			if (other.store != null)
				return false;
		} else if (!store.equals(other.store))
			return false;
		return true;
	}
}
