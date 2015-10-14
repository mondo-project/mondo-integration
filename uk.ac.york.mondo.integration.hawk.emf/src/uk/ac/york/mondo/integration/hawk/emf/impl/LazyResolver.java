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
package uk.ac.york.mondo.integration.hawk.emf.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor.LoadingMode;

/**
 * Stores which attributes or references are to be lazily resolved, and resolves
 * them when they are needed. This is a simplified implementation of the
 * original LazyEStore class, which used the EStore API and
 * DynamicEStoreEObjectImpl objects.
 */
class LazyResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(LazyResolver.class);

	private final HawkResourceImpl resource;

	public LazyResolver(HawkResourceImpl resource) {
		this.resource = resource;
	}

	/** Objects for which we don't know their attributes yet (and their IDs). */
	private Map<EObject, String> pendingAttrs = new IdentityHashMap<>();

	/** Pending EReferences to be fetched.*/
	private Map<EObject, Map<EReference, EList<Object>>> pendingRefs = new IdentityHashMap<>();

	/**
	 * Resolves the referenced feature, if it has been marked as lazy. After
	 * fetching it from the network, it will update the object accordingly so
	 * the actual call to {@link EObject#eGet(EStructuralFeature)} will retrieve
	 * the appropriate value.
	 */
	public void resolve(InternalEObject object, EStructuralFeature feature) {
		try {
			if (feature instanceof EReference) {
				Map<EReference, EList<Object>> pending = pendingRefs.get(object);
				if (pending != null) {
					EList<Object> pendingObjects = pending.remove(feature);
					if (pendingObjects != null) {
						resolvePendingReference(object, (EReference)feature, pending, pendingObjects);
					}
				}
			}
			else if (feature instanceof EAttribute) {
				String pendingId = pendingAttrs.remove(object);
				if (pendingId != null) {
					resource.fetchAttributes(object, Arrays.asList(pendingId));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Returns <code>true</code> if the fetch for feature is pending,
	 * <code>false</code> otherwise.
	 */
	public boolean isPending(InternalEObject object, EStructuralFeature feature) {
		if (feature instanceof EReference) {
			Map<EReference, EList<Object>> pending = pendingRefs.get(object);
			if (pending != null) {
				return pending.containsKey(feature);
			}
		} else if (feature instanceof EAttribute) {
			return pendingAttrs.containsKey(object);
		}
		return false;
	}

	/**
	 * Adds a reference to the store, to be fetched later on demand.
	 * 
	 * @param sourceObj
	 *            EObject whose reference will be fetched later on.
	 * @param feature
	 *            Reference to fetch.
	 * @param value
	 *            Mixed list of {@link String}s (from ID-based references) or
	 *            {@link EObject}s (from position-based references).
	 */
	public void markLazyReferences(EObject sourceObj, EReference feature, EList<Object> value) {
		Map<EReference, EList<Object>> allPending = pendingRefs.get(sourceObj);
		if (allPending == null) {
			allPending = new IdentityHashMap<>();
			pendingRefs.put(sourceObj, allPending);
		}
		allPending.put(feature, value);
		//LOGGER.debug("Set lazy references of feature {} in #{}: {}", feature.getName(), sourceObj, value);
	}

	public boolean addToLazyReferences(EObject sourceObj, EReference feature, Object value) {
		Map<EReference, EList<Object>> allPending = pendingRefs.get(sourceObj);
		EList<Object> pending = allPending.get(feature);
		//LOGGER.debug("Added {} to lazy references of feature {} in #{}: {}", value, feature.getName(), sourceObj, pending);
		return pending.add(value);
	}

	public boolean removeFromLazyReferences(EObject sourceObj, EReference feature, Object value) {
		Map<EReference, EList<Object>> allPending = pendingRefs.get(sourceObj);
		EList<Object> pending = allPending.get(feature);
		//LOGGER.debug("Removed {} from lazy references of feature {} in #{}: {}", value, feature.getName(), sourceObj, pending);
		return pending.remove(value);
	}

	/**
	 * Marks a certain {@link EObject} so its attributes will be fetched on
	 * demand.
	 */
	public void markLazyAttributes(String id, EObject eObject) {
		pendingAttrs.put(eObject, id);
	}

	private void resolvePendingReference(InternalEObject object, EReference feature, Map<EReference, EList<Object>> pending, EList<Object> ids) throws Exception {
			final LoadingMode loadingMode = resource.getDescriptor().getLoadingMode();
			if (loadingMode.isGreedyReferences()) {
				// The loading mode says we should prefetch all referenced nodes
				final List<String> childrenIds = new ArrayList<>();
				for (EList<Object> elems : pending.values()) {
					addAllStrings(elems, childrenIds);
				}
				addAllStrings(ids, childrenIds);
				resource.fetchNodes(childrenIds);
			}

			// This is a pending ref: resolve its proper value
			final EList<Object> eObjs = resolveReference(object, feature, ids);
			if (feature.isMany()) {
				object.eSet(feature, eObjs);
			} else if (!eObjs.isEmpty()) {
				object.eSet(feature, eObjs.get(0));
			}
	}

	private EList<Object> resolveReference(InternalEObject source, EReference feature, EList<Object> targets) throws Exception {
		final List<String> ids = new ArrayList<>();
		addAllStrings(targets, ids);
		final EList<EObject> resolved = resource.fetchNodes(ids);

		// Replace all old String elements with their corresponding EObjects
		final EList<Object> result = new BasicEList<>();
		int iResolved = 0;
		for (int iElem = 0; iElem < targets.size(); iElem++) {
			final Object elem = targets.get(iElem);
			if (elem instanceof String) {
				result.add(resolved.get(iResolved++));
			} else {
				result.add(elem);
			}
		}

		return result;
	}

	private void addAllStrings(EList<Object> source, final List<String> target) {
		for (Object elem : source) {
			if (elem instanceof String) {
				target.add((String)elem);
			}
		}
	}

	public boolean hasChildren(InternalEObject o, EReference r) {
		assert isPending(o, r) : "Callers to hasChildren should always check first with isPending";

		Map<EReference, EList<Object>> allPending = pendingRefs.get(o);
		EList<Object> pending = allPending.get(r);
		return !pending.isEmpty();
	}
}