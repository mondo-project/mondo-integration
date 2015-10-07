package hu.bme.mit.mondo.integration.incquery.hawk;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.emf.EMFQueryRuntimeContext;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import uk.ac.york.mondo.integration.hawk.emf.HawkResource;

public class HawkQueryRuntimeContext extends EMFQueryRuntimeContext {

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
		try {
			if (key instanceof EClassTransitiveInstancesKey) {
				EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				EList<EObject> instances = hawkResource.fetchNodesByType(eClass);
				if (seedInstance == null) { // unseeded
					return instances.size();
				} else { // fully seeded
					return instances.contains(seedInstance) ? 1 : 0;
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				List<Object> values = hawkResource.fetchValuesByEClassifier(dataType);
				if (seedInstance == null) { // unseeded
					return values.size();
				} else { // fully seeded
					return values.contains(seedInstance) ? 1 : 0;
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);

				Map<EObject, Object> featureValues = hawkResource.fetchValuesByEStructuralFeature(feature);

				if (seedSource == null && seedTarget != null) {
					return featureValues.values().contains(seedTarget) ? 1 : 0;
				} else if (seedSource != null && seedTarget != null) { // fully seeded
					return seedTarget.equals(featureValues.get(seedSource)) ? 1 : 0;
				} else if (seedSource == null && seedTarget == null) { // fully unseeded
					return featureValues.size();
				} else if (seedSource != null && seedTarget == null) {
					Object value = featureValues.get(seedSource);
					return value != null ? 1 : 0;
				}
			} else {
				illegalInputKey(key);
			}
		} catch (TException | IOException e) {
			throw new RuntimeException(e);
		}

		return 0;
	}

	@Override
	public Iterable<Tuple> enumerateTuples(IInputKey key, Tuple seed) {
		Collection<Tuple> result = new HashSet<Tuple>();
		try {

			if (key instanceof EClassTransitiveInstancesKey) {
				EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				EList<EObject> instances = hawkResource.fetchNodesByType(eClass);
				if (seedInstance == null) { // unseeded
					return Iterables.transform(instances, wrapUnary);
				} else { // fully seeded
					if (instances.contains(seedInstance)) {
						result.add(new FlatTuple(seedInstance));
					}
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
				Object seedInstance = getFromSeed(seed, 0);

				List<Object> values = hawkResource.fetchValuesByEClassifier(dataType);
				if (seedInstance == null) { // unseeded
					return Iterables.transform(values, wrapUnary);
				} else { // fully seeded
					if (values.contains(seedInstance)) {
						result.add(new FlatTuple(seedInstance));
					}
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);

				Map<EObject, Object> featureValues = hawkResource.fetchValuesByEStructuralFeature(feature);

				if (seedSource == null && seedTarget != null) {
					for (Entry<EObject, Object> entry : featureValues.entrySet()) {
						if (entry.getValue().equals(seedTarget)) {
							result.add(new FlatTuple(entry.getKey(), entry.getValue()));
						}
					}
				} else if (seedSource != null && seedTarget != null) { // fully seeded
					if (seedTarget.equals(featureValues.get(seedSource))) {
						result.add(new FlatTuple(seedSource, seedTarget));
					}
				} else if (seedSource == null && seedTarget == null) { // fully unseeded
					for (Entry<EObject, Object> entry : featureValues.entrySet()) {
						result.add(new FlatTuple(entry.getKey(), entry.getValue()));
					}
				} else if (seedSource != null && seedTarget == null) {
					Object value = featureValues.get(seedSource);
					if (value != null) {
						result.add(new FlatTuple(seedSource, value));
					}
				}
			} else {
				illegalInputKey(key);
			}
		} catch (TException | IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	@Override
	public Iterable<? extends Object> enumerateValues(IInputKey key, Tuple seed) {
		try {

			if (key instanceof EClassTransitiveInstancesKey) {

				EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();

				Object seedInstance = getFromSeed(seed, 0);
				if (seedInstance == null) { // unseeded
					return hawkResource.fetchNodesByType(eClass);
				} else {
					// must be unseeded, this is enumerateValues after all!
					illegalEnumerateValues(seed);
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();

				Object seedInstance = getFromSeed(seed, 0);
				if (seedInstance == null) { // unseeded
					return hawkResource.fetchValuesByEClassifier(dataType);
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
					return features.keySet();
				} else if (seedSource != null && seedTarget == null) {
					return features.values();
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

		return null;

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

}
