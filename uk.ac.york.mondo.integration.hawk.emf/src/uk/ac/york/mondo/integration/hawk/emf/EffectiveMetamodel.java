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
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * In-memory representation of an effective metamodel. By default, all types are
 * included. This representation is designed to be modified only from
 * {@link EffectiveMetamodelStore}.
 *
 * TODO: use a more efficient representation based on includes/excludes rules.
 */
public class EffectiveMetamodel {
	public static final String ALL_FIELDS = "*";
	public static final String ALL_TYPES = "*";
	protected Map<String, Set<String>> store = new HashMap<>();

	/**
	 * Creates an empty instance that includes no types.
	 */
	protected EffectiveMetamodel() {}

	/**
	 * Copy constructor.
	 */
	protected EffectiveMetamodel(EffectiveMetamodel toCopy) {
		// We can do this simple copy *because* we use ImmutableSet
		this.store = new HashMap<>(toCopy.store);
	}

	/**
	 * Adds a type to the effective metamodel, including all slots.
	 */
	protected ImmutableSet<String> add(String type) {
		return add(type, ImmutableSet.of(ALL_FIELDS));
	}

	/**
	 * Removes a type from the effective metamodel if it was included.
	 * 
	 * @return The set of included fields from the removed type (see
	 *         {@link #getIncludedSlots(String)} for details).
	 */
	protected ImmutableSet<String> remove(String type) {
		return (ImmutableSet<String>) store.remove(type);
	}

	/**
	 * Adds a type to the effective metamodel, including a specific set of slots.
	 */
	protected ImmutableSet<String> add(String type, ImmutableSet<String> slots) {
		return (ImmutableSet<String>) store.put(type, slots);
	}

	/** Returns <code>true</code> if the effective metamodel does not include any types. */
	public boolean isEmpty() {
		return store.isEmpty();
	}

	public boolean isTypeIncluded(String type) {
		return store.containsKey(type);
	}

	public boolean isSlotIncluded(String type, String slot) {
		Set<String> slots = store.get(type);
		return slots != null && (slots.contains(slot) || slots.contains(ALL_FIELDS));
	}

	/**
	 * Returns an unmodifiable view of the included types. See
	 * {@link #getIncludedSlots(String)} for details about the <code>Set</code>
	 * values.
	 */
	public Map<String, Set<String>> getIncludedTypes() {
		return Collections.unmodifiableMap(store);
	}

	/**
	 * Returns an unmodifiable view of the included slots for that type.
	 * 
	 * @return Returns one of the following: a set with the names of the
	 *         included slots, a singleton set with {@link #ALL_FIELDS} if
	 *         all fields are included implicitly, or <code>null</code> if
	 *         the type is not included.
	 */
	public ImmutableSet<String> getIncludedSlots(String type) {
		return (ImmutableSet<String>) store.get(type);
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
		EffectiveMetamodel other = (EffectiveMetamodel) obj;
		if (store == null) {
			if (other.store != null)
				return false;
		} else if (!store.equals(other.store))
			return false;
		return true;
	}
}