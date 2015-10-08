package hu.bme.mit.mondo.integration.incquery.hawk;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.incquery.runtime.emf.EMFQueryRuntimeContext;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.context.common.JavaTransitiveInstancesKey;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import uk.ac.york.mondo.integration.api.HawkModelElementAdditionEvent;
import uk.ac.york.mondo.integration.hawk.emf.HawkChangeEventAdapter;
import uk.ac.york.mondo.integration.hawk.emf.HawkResource;

public class HawkQueryRuntimeContext<E> extends EMFQueryRuntimeContext {

	private static final class InstanceCounter {
		private final Map<Object, Integer> counts = new HashMap<>();

		/**
		 * Counts an additional instance of a specific value. Returns true if this is the first instance of that value.
		 */
		public boolean add(Object o) {
			Integer count = counts.get(o);
			if (count == null) {
				count = 0;
			}
			count++;
			counts.put(o, count);
			return count == 1;
		}

		/**
		 * Counts a removed instance of a specific value. Returns true if this is the last instance of that value (the
		 * next call to {@link #add(Object)} with the same value will return <code>true</code>).
		 * 
		 * @throws NoSuchElementException
		 *             The value was never counted before.
		 */
		public boolean remove(Object o) {
			Integer count = counts.get(o);
			if (count == null) {
				throw new NoSuchElementException();
			}
			count--;
			if (count == 0) {
				counts.remove(o);
			} else {
				counts.put(o, count);
			}
			return count == 0;
		}
	}

	private final class EStructuralFeatureChangeAdapter extends AdapterImpl {
		private final EStructuralFeatureInstancesKeyAdapter incqAdapter;
		private final EObject eob;
		private final EStructuralFeature feature;

		private EStructuralFeatureChangeAdapter(EObject eob, EStructuralFeature feature,
				EStructuralFeatureInstancesKeyAdapter incqAdapter) {
			this.incqAdapter = incqAdapter;
			this.eob = eob;
			this.feature = feature;
		}

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getFeature() != feature) {
				return;
			}

			switch (notification.getEventType()) {
			case Notification.SET:
				// TODO: what to do with the last boolean flag?
				if (notification.getOldValue() != null) {
					incqAdapter.featureDeleted(eob, feature, notification.getOldValue());
				}
				incqAdapter.featureInserted(eob, feature, notification.getNewValue());
				break;
			case Notification.UNSET:
				incqAdapter.featureDeleted(eob, feature, notification.getOldValue());
				break;

			case Notification.ADD:
				incqAdapter.featureInserted(eob, feature, notification.getNewValue());
				break;
			case Notification.REMOVE:
				incqAdapter.featureDeleted(eob, feature, notification.getOldValue());
				break;

			case Notification.ADD_MANY:
				if (notification.getNewValue() instanceof Iterable) {
					for (Object o : (Iterable<?>) notification.getNewValue()) {
						incqAdapter.featureInserted(eob, feature, o);
					}
				}
				break;
			case Notification.REMOVE_MANY:
				if (notification.getOldValue() instanceof Iterable) {
					for (Object o : (Iterable<?>) notification.getOldValue()) {
						incqAdapter.featureDeleted(eob, feature, o);
					}
				}
				break;
			}
		}
	}

	private final class EDataTypeChangeAdapter extends AdapterImpl {
		private final EDataType dataType;
		private final List<EAttribute> eAttrs;
		private final EDataTypeInSlotsAdapter incqAdapter;
		private final InstanceCounter occurrences;

		private EDataTypeChangeAdapter(EDataType dataType, List<EAttribute> eAttrs,
				EDataTypeInSlotsAdapter incqAdapter, InstanceCounter occurrences) {
			this.dataType = dataType;
			this.eAttrs = eAttrs;
			this.incqAdapter = incqAdapter;
			this.occurrences = occurrences;
		}

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (!eAttrs.contains(notification.getFeature())) {
				return;
			}

			switch (notification.getEventType()) {
			case Notification.SET:
				if (notification.getOldValue() != null) {
					final boolean lastOccurrenceSet = occurrences.remove(notification.getOldValue());
					incqAdapter.dataTypeInstanceDeleted(dataType, notification.getOldValue(), lastOccurrenceSet);
				}
				final boolean firstOccurrenceSet = occurrences.add(notification.getNewValue());
				incqAdapter.dataTypeInstanceInserted(dataType, notification.getNewValue(), firstOccurrenceSet);
				break;
			case Notification.UNSET:
				final boolean lastOccurrenceUnset = occurrences.remove(notification.getOldValue());
				incqAdapter.dataTypeInstanceDeleted(dataType, notification.getOldValue(), lastOccurrenceUnset);
				break;
			case Notification.ADD:
				final boolean firstOccurrenceAdd = occurrences.add(notification.getNewValue());
				incqAdapter.dataTypeInstanceInserted(dataType, notification.getNewValue(), firstOccurrenceAdd);
				break;
			case Notification.REMOVE:
				final boolean lastOccurrenceRemove = occurrences.remove(notification.getOldValue());
				incqAdapter.dataTypeInstanceDeleted(dataType, notification.getOldValue(), lastOccurrenceRemove);
				break;

			case Notification.ADD_MANY:
				if (notification.getNewValue() instanceof Iterable) {
					for (Object o : (Iterable<?>) notification.getNewValue()) {
						final boolean firstOccurrence = occurrences.add(o);
						incqAdapter.dataTypeInstanceInserted(dataType, o, firstOccurrence);
					}
				}
				break;
			case Notification.REMOVE_MANY:
				if (notification.getOldValue() instanceof Iterable) {
					for (Object o : (Iterable<?>) notification.getOldValue()) {
						final boolean lastOccurrence = occurrences.remove(o);
						incqAdapter.dataTypeInstanceDeleted(dataType, o, lastOccurrence);
					}
				}
				break;
			}
		}
	}

	protected String clean(String s) {
		return s.replaceAll("http://www.semanticweb.org/ontologies/2015/trainbenchmark/", "")
				.replaceAll("hu\\.bme\\.mit\\.trainbenchmark\\.railway\\.impl\\.", "").replaceAll(";", "); ");
	}

	private HawkResource hawkResource;

	public HawkQueryRuntimeContext(HawkResource hawkResource, Logger logger) {
		super(null, logger);
		this.hawkResource = hawkResource;
	}

	@Override
	public <V> V coalesceTraversals(Callable<V> callable) throws InvocationTargetException {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	@Override
	public boolean isCoalescing() {
		return false;
	}

	@Override
	public boolean isIndexed(IInputKey key) {
		return true;
	}

	@Override
	public void ensureIndexed(IInputKey key) {
		// do nothing
	}

	@Override
	public int countTuples(IInputKey key, Tuple seed) {
		int result = 0;

		try {
			if (key instanceof EClassTransitiveInstancesKey) {
				EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				EList<EObject> instances = hawkResource.fetchNodesByType(eClass);
				if (seedInstance == null) { // unseeded
					result = instances.size();
				} else { // fully seeded
					result = instances.contains(seedInstance) ? 1 : 0;
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				List<Object> values = hawkResource.fetchValuesByEClassifier(dataType);
				if (seedInstance == null) { // unseeded
					result = values.size();
				} else { // fully seeded
					result = values.contains(seedInstance) ? 1 : 0;
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);

				Map<EObject, Object> featureValues = hawkResource.fetchValuesByEStructuralFeature(feature);

				if (seedSource == null && seedTarget != null) {
					result = featureValues.values().contains(seedTarget) ? 1 : 0;
				} else if (seedSource != null && seedTarget != null) { // fully seeded
					result = seedTarget.equals(featureValues.get(seedSource)) ? 1 : 0;
				} else if (seedSource == null && seedTarget == null) { // fully unseeded
					result = featureValues.size();
				} else if (seedSource != null && seedTarget == null) {
					Object value = featureValues.get(seedSource);
					result = value != null ? 1 : 0;
				}
			} else {
				illegalInputKey(key);
			}
		} catch (TException | IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("countTuples: " + clean(key.toString()) + ", " + seed);
		System.out.println(result);
		System.out.println();

		return result;
	}

	@Override
	public Iterable<Tuple> enumerateTuples(IInputKey key, Tuple seed) {
		Iterable<Tuple> result = null;
		try {

			if (key instanceof EClassTransitiveInstancesKey) {
				EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				EList<EObject> instances = hawkResource.fetchNodesByType(eClass);
				if (seedInstance == null) { // unseeded
					result = Iterables.transform(instances, wrapUnary);
				} else { // fully seeded
					if (instances.contains(seedInstance)) {
						result = Arrays.asList(new FlatTuple(seedInstance));
					}
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				List<Object> values = hawkResource.fetchValuesByEClassifier(dataType);
				if (seedInstance == null) { // unseeded
					result = Iterables.transform(values, wrapUnary);
				} else { // fully seeded
					if (values.contains(seedInstance)) {
						result = Arrays.asList(new FlatTuple(seedInstance));
					}
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);

				Map<EObject, Object> featureValues = hawkResource.fetchValuesByEStructuralFeature(feature);

				List<Tuple> tuples = new ArrayList<>();
				if (seedSource == null && seedTarget != null) {
					for (Entry<EObject, Object> entry : featureValues.entrySet()) {
						if (entry.getValue().equals(seedTarget)) {
							tuples.add(new FlatTuple(entry.getKey(), entry.getValue()));
						}
					}
				} else if (seedSource != null && seedTarget != null) { // fully seeded
					if (seedTarget.equals(featureValues.get(seedSource))) {
						tuples.add(new FlatTuple(seedSource, seedTarget));
					}
				} else if (seedSource == null && seedTarget == null) { // fully unseeded
					for (Entry<EObject, Object> entry : featureValues.entrySet()) {
						if (entry.getValue() instanceof Collection) {
							for (Object value : (Collection) entry.getValue()) {
								tuples.add(new FlatTuple(entry.getKey(), value));
							}
						} else {
							tuples.add(new FlatTuple(entry.getKey(), entry.getValue()));
						}
					}
				} else if (seedSource != null && seedTarget == null) {
					Object value = featureValues.get(seedSource);
					if (value != null) {
						tuples.add(new FlatTuple(seedSource, value));
					}
				}
				result = tuples;
			} else {
				illegalInputKey(key);
			}
		} catch (TException | IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("enumerateTuples: " + clean(key.toString()) + ", " + seed);
		System.out.println(clean(result.toString()));
		System.out.println();

		return result;
	}

	@Override
	public Iterable<? extends Object> enumerateValues(IInputKey key, Tuple seed) {
		Iterable<? extends Object> result = null;
		try {

			if (key instanceof EClassTransitiveInstancesKey) {

				EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();

				Object seedInstance = getFromSeed(seed, 0);
				if (seedInstance == null) { // unseeded
					result = hawkResource.fetchNodesByType(eClass);
				} else {
					// must be unseeded, this is enumerateValues after all!
					illegalEnumerateValues(seed);
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();

				Object seedInstance = getFromSeed(seed, 0);
				if (seedInstance == null) { // unseeded
					result = hawkResource.fetchValuesByEClassifier(dataType);
				} else {
					// must be unseeded, this is enumerateValues after all!
					illegalEnumerateValues(seed);
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();

				Map<EObject, Object> features = hawkResource.fetchValuesByEStructuralFeature(feature);

				Object seedSource = getFromSeed(seed, 0);
				Object seedTarget = getFromSeed(seed, 1);
				if (seedSource == null && seedTarget != null) {
					result = features.keySet();
				} else if (seedSource != null && seedTarget == null) {
					result = features.values();
				} else {
					// must be singly unseeded, this is enumerateValues after all!
					illegalEnumerateValues(seed);
				}
			} else {
				illegalInputKey(key);
			}
		} catch (TException | IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("enumerateValues: " + clean(key.toString()) + ", " + seed);
		System.out.println(clean(result.toString()));
		System.out.println();
		return result;

	}

	protected Object getFromSeed(Tuple seed, int index) {
		return seed == null ? null : seed.get(index);
	}

	protected static Function<Object, Tuple> wrapUnary = new Function<Object, Tuple>() {
		@Override
		public Tuple apply(Object obj) {
			return new FlatTuple(obj);
		}
	};

	public void addUpdateListener(IInputKey key, Tuple seed, IQueryRuntimeContextListener listener) {
		// stateless, so NOP
		if (key instanceof JavaTransitiveInstancesKey)
			return;

		if (key instanceof EClassTransitiveInstancesKey) {
			final EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			final Object seedInstance = seed.get(0);
			final EClassTransitiveInstancesAdapter incqAdapter = new EClassTransitiveInstancesAdapter(listener, seedInstance);

			// We need to fetch all the existing instances in advance so the lazy loading won't
			// result in unwanted notifications.
			try {
				hawkResource.fetchNodesByType(eClass);
			} catch (TException | IOException e) {
				throw new RuntimeException(e);
			}

			// Removal is only done through Artemis events, and not through lazy loading,
			// but by the time we get to the HawkModelElementRemovalEvent the EObject has
			// already been removed and is not available, so we have to use EMF notifications.
			hawkResource.getResourceSet().eAdapters().add(new EContentAdapter() {
				@Override
				public void notifyChanged(Notification notification) {
					super.notifyChanged(notification);
					if (notification.getOldValue() instanceof EObject) {
						final EObject eob = (EObject)notification.getOldValue();
						if (eClass.isSuperTypeOf(eob.eClass())) {
							incqAdapter.instanceDeleted(eClass, eob);
						}
					}
				}
			});

			// Model element instances may be added through lazy loading or Artemis notifications:
			// we're only interested in new instances (coming from Artemis).
			hawkResource.addChangeEventHandler(new HawkChangeEventAdapter(){
				@Override
				public void handle(HawkModelElementAdditionEvent ev) {
					final EObject eob = hawkResource.getEObjectFromNodeID(ev.id);
					final EClass eobEClass = eob.eClass();
					if (eClass.isSuperTypeOf(eobEClass)) {
						incqAdapter.instanceInserted(eClass, eob);
					}
				}
			});

		} else if (key instanceof EDataTypeInSlotsKey) {
			final EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			final Object seedInstance = seed.get(0);
			final EDataTypeInSlotsAdapter incqAdapter = new EDataTypeInSlotsAdapter(listener, seedInstance);

			try {
				final InstanceCounter occurrences = new InstanceCounter();
				final Map<EClass, List<EAttribute>> candidateTypes = hawkResource.fetchTypesWithEClassifier(dataType);

				// Instrument existing instances of the candidate EClasses
				for (Entry<EClass, List<EAttribute>> entry : candidateTypes.entrySet()) {
					final EClass eClass = entry.getKey();
					final List<EAttribute> eAttrs = entry.getValue();
					for (final EObject eob : hawkResource.fetchNodesByType(eClass)) {
						for (EAttribute attr : eAttrs) {
							final Object value = eob.eGet(attr);
							if (value instanceof Iterable) {
								for (Object o : (Iterable<?>)value) {
									occurrences.add(o);
								}
							} else if (value != null) {
								occurrences.add(value);
							}
						}
						eob.eAdapters().add(new EDataTypeChangeAdapter(dataType, eAttrs, incqAdapter, occurrences));
					}
				}

				// Instrument new instances of the candidate EClasses
				hawkResource.addChangeEventHandler(new HawkChangeEventAdapter() {
					@Override
					public void handle(HawkModelElementAdditionEvent ev) {
						final EObject eob = hawkResource.getEObjectFromNodeID(ev.id);
						final List<EAttribute> attrs = candidateTypes.get(eob.eClass());
						if (attrs != null) {
							eob.eAdapters().add(new EDataTypeChangeAdapter(dataType, attrs, incqAdapter, occurrences));
						}
					}
				});

			} catch (TException | IOException e) {
				throw new RuntimeException(e);
			}

		} else if (key instanceof EStructuralFeatureInstancesKey) {
			final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			final Object seedHost = seed.get(0);
			final Object seedValue = seed.get(1);
			final EStructuralFeatureInstancesKeyAdapter incqAdapter = new EStructuralFeatureInstancesKeyAdapter(listener, seedHost, seedValue);

			try {
				final EClass eClass = feature.getEContainingClass();

				// Instrument existing instances
				for (final EObject eob : hawkResource.fetchNodesByType(eClass)) {
					// We need to fetch all values in advance to avoid duplicate notifications during lazy loading.
					eob.eGet(feature);
					eob.eAdapters().add(new EStructuralFeatureChangeAdapter(eob, feature, incqAdapter));
				}

				// Instrument new instances
				hawkResource.addChangeEventHandler(new HawkChangeEventAdapter() {
					@Override
					public void handle(HawkModelElementAdditionEvent ev) {
						final EObject eob = hawkResource.getEObjectFromNodeID(ev.id);
						if (eob != null) {
							if (eClass.isSuperTypeOf(eob.eClass())) {
								eob.eAdapters().add(new EStructuralFeatureChangeAdapter(eob, feature, incqAdapter));
							}
						} else {
							System.err.println("WARNING: did not find eobject for node id " + ev.id);
						}
					}
				});
				
			} catch (TException | IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			illegalInputKey(key);
		}
	}

	@Override
	public void removeUpdateListener(IInputKey key, Tuple seed, IQueryRuntimeContextListener listener) {
		// do nothing
	}

}
