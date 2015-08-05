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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.HawkInstanceNotRunning;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor.LoadingMode;

/**
 * EStore implementation used for the lazy loading mode. Tries to avoid hitting the
 * network as much as possible (e.g. {@link #size} calls).
 */
class LazyEStore implements InternalEObject.EStore {

	private static final class ImmutablePair<L, R> {
		public final L left;
		public final R right;

		public ImmutablePair(L l, R r) {
			this.left = l;
			this.right = r;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(LazyEStore.class);

	private final HawkResourceImpl resource;

	public LazyEStore(HawkResourceImpl resource) {
		this.resource = resource;
	}

	/**
	 * Used to store the container of an object and the feature through
	 * which the object is contained.
	 */
	private Map<EObject, ImmutablePair<EStructuralFeature, InternalEObject>> containers = new IdentityHashMap<>(); 

	/** Values for the EAttributes and the EReferences that have been resolved through the network. */ 
	private Map<EObject, Map<EStructuralFeature, Object>> store = new IdentityHashMap<>();

	/** Objects for which we don't know their attributes yet (and their IDs). */
	private Map<EObject, String> pendingAttrs = new IdentityHashMap<>();

	/** Pending EReferences to be fetched.*/
	private Map<EObject, Map<EStructuralFeature, List<String>>> pendingRefs = new IdentityHashMap<>();

	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		final Map<EStructuralFeature, Object> values = store.get(object);
		if (values != null) {
			values.remove(feature);
		} else {
			Map<EStructuralFeature, List<String>> pending = pendingRefs.get(feature);
			if (pending != null) {
				pending.remove(feature);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(InternalEObject object, EStructuralFeature feature, T[] array) {
		final Object value = get(object, feature, -1);
		if (value instanceof Collection) {
			final Collection<?> c = (Collection<?>)value;
			if (array.length != c.size()) {
				array = (T[]) Array.newInstance(array.getClass().getComponentType(), c.size());
			}
			
			final Iterator<?> it = c.iterator();
			for (int i = 0; i < array.length && it.hasNext(); i++) {
				array[i] = (T)it.next();
			}
		}
		return array;
	}

	@Override
	public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
		return toArray(object, feature, new Object[0]);
	}

	@Override
	public int size(InternalEObject object, EStructuralFeature feature) {
		// We can do this without hitting the network, even for refs
		Map<EStructuralFeature, Object> values = store.get(object);
		Object value = null;
		if (values != null) {
			value = values.get(feature);
		}

		// Grab the size from the existing value
		int size = 0;
		if (value instanceof Collection) {
			size = ((Collection<?>)value).size();
		}

		// Need to take into account pending refs into the size as well:
		// the reference may not have been fetched yet, or the encoder
		// may have produced a combination of ID-based and position-based
		// references.
		if (feature instanceof EReference) {
			Map<EStructuralFeature, List<String>> pending = pendingRefs.get(object);
			if (pending != null) {
				List<String> ids = pending.get(feature);
				if (ids != null) {
					size += ids.size();
				}
			}
		}

		return size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		Map<EStructuralFeature, Object> values = store.get(object);
		if (values == null) {
			values = new IdentityHashMap<EStructuralFeature, Object>();
			store.put(object, values);
		}

		if (index == NO_INDEX || !feature.isMany()) {
			return values.put(feature, value);
		} else /* index != NO_INDEX && feature.isMany() */ {
			List<Object> l = (List<Object>)values.get(feature);
			if (l == null) {
				l = new BasicEList<>();
				values.put(feature, l);
			}

			Object oldValue = null;
			if (index < l.size()) {
				oldValue = l.set(index, value);
			} else {
				l.add(value);
			}
			return oldValue;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		Map<EStructuralFeature, Object> values = store.get(object);
		if (values != null) {
			List<Object> l = (List<Object>) values.get(feature);
			if (l != null) {
				return l.remove(index);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		Map<EStructuralFeature, Object> values = store.get(object);
		if (values != null) {
			// We only cast to EList here because it already implements move
			EList<Object> l = (EList<Object>) values.get(feature);
			if (l != null) {
				return l.move(targetIndex, sourceIndex);
			}
		}
		return null;
	}

	@Override
	public int lastIndexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		final Object featureValue = get(object, feature, -1);
		if (featureValue instanceof List) {
			final List<?> l = (List<?>)featureValue;
			for (int i = l.size() - 1; i > 0; i--) {
				if (l.get(i).equals(value)) {
					return i; 
				}
			}
		}
		return -1;
	}

	@Override
	public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		final Object featureValue = get(object, feature, -1);
		if (featureValue instanceof List) {
			final List<?> l = (List<?>)featureValue;
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i).equals(value)) {
					return i; 
				}
			}
		}
		return -1;
	}

	@Override
	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		Map<EStructuralFeature, Object> values = store.get(object);
		if (values == null) {
			values = new IdentityHashMap<>();
			store.put(object, values);
		}

		try {
			boolean isSet = false;
			if (values.containsKey(feature)) {
				isSet = true;
			} else if (feature instanceof EAttribute) {
				isSet = resolvePendingAttribute(object, (EAttribute) feature, values) != null;
			} else if (feature instanceof EReference) {
				final Map<EStructuralFeature, List<String>> pending = pendingRefs.get(object);
				isSet = pending != null && pending.containsKey(feature);
			}

			LOGGER.debug("isSet({}, {}) = {}", object, feature.getName(), isSet);
			return isSet;
		} catch (TException | IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		return size(object, feature) == 0;
	}

	@Override
	public int hashCode(InternalEObject object, EStructuralFeature feature) {
		final Object value = get(object, feature, -1);
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public EStructuralFeature getContainingFeature(InternalEObject object) {
		final ImmutablePair<EStructuralFeature, InternalEObject> immutablePair = containers.get(object);
		return immutablePair == null ? null : immutablePair.left;
	}

	@Override
	public InternalEObject getContainer(InternalEObject object) {
		final ImmutablePair<EStructuralFeature, InternalEObject> immutablePair = containers.get(object);
		return immutablePair == null ? null : immutablePair.right;
	}

	@Override
	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		try {
			Map<EStructuralFeature, Object> values = store.get(object);
			if (values == null) {
				values = new IdentityHashMap<>();
				store.put(object, values);
			}

			if (feature instanceof EReference) {
				resolvePendingReference(object, (EReference)feature, values);
			} else if (feature instanceof EAttribute) {
				resolvePendingAttribute(object, (EAttribute)feature, values);
			}

			Object value = values.get(feature);
			LOGGER.debug("get({}, {}) = {}", object, feature.getName(), value);
			if (index == NO_INDEX || !feature.isMany()) {
				return value;
			} else {
				final EList<?> l = (EList<?>)value;
				return l.get(index);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public EObject create(EClass eClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		if (value instanceof InternalEObject) {
			final InternalEObject container = getContainer((InternalEObject)value);
			return container == object;
		}
		return indexOf(object, feature, value) != -1;
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		Map<EStructuralFeature, Object> values = store.get(object);
		if (values != null) {
			EList<?> l = (EList<?>)values.get(feature);
			if (l != null) {
				l.clear();
			}
		}
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		set(object, feature, index, value);
	}

	public void addLazyReferences(EObject sourceObj, EReference feature, List<String> l) {
		Map<EStructuralFeature, List<String>> allPending = pendingRefs.get(sourceObj);
		if (allPending == null) {
			allPending = new IdentityHashMap<>();
			pendingRefs.put(sourceObj, allPending);
		}
		allPending.put(feature, l);
	}

	public void addLazyAttributes(String id, EObject eObject) {
		pendingAttrs.put(eObject, id);
	}

	private Object resolvePendingAttribute(InternalEObject object,
			EAttribute feature,
			Map<EStructuralFeature, Object> values)
			throws HawkInstanceNotFound, HawkInstanceNotRunning,
			TException, IOException {
		final String pendingId = pendingAttrs.remove(object);
		if (pendingId != null) {
			resource.fetchAttributes(object, Arrays.asList(pendingId));
			return values.get(feature);
		}
		return null;
	}

	private void resolvePendingReference(InternalEObject object,
			EReference feature,
			Map<EStructuralFeature, Object> values)
			throws Exception {
		Map<EStructuralFeature, List<String>> pending = pendingRefs.get(object);
		if (pending != null) {
			final LoadingMode loadingMode = resource.getDescriptor().getLoadingMode();
			if (loadingMode.isGreedyReferences()) {
				// The loading mode says we should prefetch all referenced nodes
				final List<String> childrenIds = new ArrayList<>();
				for (List<String> ids : pending.values()) {
					childrenIds.addAll(ids);
				}
				resource.fetchNodes(childrenIds);
			}

			// This is a pending ref: resolve its proper value
			List<String> ids = pending.remove(feature);
			if (ids != null) {
				final EList<EObject> eObjs = resolveReference(object, feature, ids);
				if (feature.isMany()) {
					values.put(feature, eObjs);
				} else if (!eObjs.isEmpty()) {
					values.put(feature, eObjs.get(0));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private EList<EObject> resolveReference(InternalEObject container, EStructuralFeature feature, List<String> ids) throws Exception {
		assert store.get(container) != null : "The store for this feature should have been already set";

		EList<EObject> resolved = resource.fetchNodes(ids);
		if (container != null) {
			for (EObject eObj : resolved) {
				containers.put(eObj, new ImmutablePair<>(feature, container));
			}
		}

		// We might already have some objects in the EReference from a ReferenceSlot
		// with both positions and IDs.
		//
		// XXX We lose the relative ordering of the elements if we use a combination
		// of positions and IDs, though.
		EList<EObject> result = (EList<EObject>) store.get(container).get(feature);
		if (result == null) {
			result = resolved;
		} else {
			result.addAll(resolved);
		}

		return result;
	}
}