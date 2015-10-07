package hu.bme.mit.mondo.integration.incquery.hawk;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.emf.EMFQueryRuntimeContext;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;

import uk.ac.york.mondo.integration.api.Hawk.Client;

public class HawkQueryRuntimeContext extends EMFQueryRuntimeContext {

	private Client client;

	public HawkQueryRuntimeContext(Client client, Logger logger) {
		super(null, logger);
		this.client = client;
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
		if (key instanceof EClassTransitiveInstancesKey) {
			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();

			// Hawk
			String eolQuery = String.format("return %s.all.size()", key.getStringID());

//			Object seedInstance = getFromSeed(seed, 0);
//			if (seedInstance == null) { // unseeded
//				return baseIndex.getAllInstances(eClass).size();
//			} else { // fully seeded
//				return (containsTuple(key, seed)) ? 1 : 0;
//			}
		} else if (key instanceof EDataTypeInSlotsKey) {
			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();

//			Object seedInstance = getFromSeed(seed, 0);
//			if (seedInstance == null) { // unseeded
//				return baseIndex.getDataTypeInstances(dataType).size();
//			} else { // fully seeded
//				return (containsTuple(key, seed)) ? 1 : 0;
//			}
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();

//			final Object seedSource = getFromSeed(seed, 0);
//			final Object seedTarget = getFromSeed(seed, 1);
//			if (seedSource == null && seedTarget != null) {
//				return baseIndex.findByFeatureValue(seedTarget, feature).size();
//			} else if (seedSource != null && seedTarget != null) { // fully
//																	// seeded
//				return (containsTuple(key, seed)) ? 1 : 0;
//			} else if (seedSource == null && seedTarget == null) { // fully
//																	// unseeded
//				int result = 0;
//				Set<Entry<EObject, Set<Object>>> entrySet = baseIndex.getFeatureInstances(feature).entrySet();
//				for (Entry<EObject, Set<Object>> entry : entrySet) {
//					result += entry.getValue().size();
//				}
//				return result;
//			} else if (seedSource != null && seedTarget == null) {
//				return baseIndex.getFeatureTargets((EObject) seedSource, feature).size();
//			}
		} else {
			illegalInputKey(key);
		}

		return 0;
	}

	@Override
	public Iterable<Tuple> enumerateTuples(IInputKey key, Tuple seed) {
		return null;
		// ensureIndexed(key);
		// final Collection<Tuple> result = new HashSet<Tuple>();
		//
		// if (key instanceof EClassTransitiveInstancesKey) {
		// EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
		//
		// Object seedInstance = getFromSeed(seed, 0);
		// if (seedInstance == null) { // unseeded
		// return Iterables.transform(baseIndex.getAllInstances(eClass),
		// wrapUnary);
		// } else { // fully seeded
		// if (containsTuple(key, seed))
		// result.add(new FlatTuple(seedInstance));
		// }
		// } else if (key instanceof EDataTypeInSlotsKey) {
		// EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
		//
		// Object seedInstance = getFromSeed(seed, 0);
		// if (seedInstance == null) { // unseeded
		// return Iterables.transform(baseIndex.getDataTypeInstances(dataType),
		// wrapUnary);
		// } else { // fully seeded
		// if (containsTuple(key, seed))
		// result.add(new FlatTuple(seedInstance));
		// }
		// } else if (key instanceof EStructuralFeatureInstancesKey) {
		// EStructuralFeature feature = ((EStructuralFeatureInstancesKey)
		// key).getEmfKey();
		//
		// final Object seedSource = getFromSeed(seed, 0);
		// final Object seedTarget = getFromSeed(seed, 1);
		// if (seedSource == null && seedTarget != null) {
		// final Set<EObject> results = baseIndex.findByFeatureValue(seedTarget,
		// feature);
		// return Iterables.transform(results, new Function<Object, Tuple>() {
		// @Override
		// public Tuple apply(Object obj) {
		// return new FlatTuple(obj, seedTarget);
		// }
		// });
		// } else if (seedSource != null && seedTarget != null) { // fully
		// // seeded
		// if (containsTuple(key, seed))
		// result.add(new FlatTuple(seedSource, seedTarget));
		// } else if (seedSource == null && seedTarget == null) { // fully
		// // unseeded
		// baseIndex.processAllFeatureInstances(feature, new
		// IEStructuralFeatureProcessor() {
		// public void process(EStructuralFeature feature, EObject source,
		// Object target) {
		// result.add(new FlatTuple(source, target));
		// };
		// });
		// } else if (seedSource != null && seedTarget == null) {
		// final Set<Object> results = baseIndex.getFeatureTargets((EObject)
		// seedSource, feature);
		// return Iterables.transform(results, new Function<Object, Tuple>() {
		// public Tuple apply(Object obj) {
		// return new FlatTuple(seedSource, obj);
		// }
		// });
		// }
		// } else {
		// illegalInputKey(key);
		// }
		//
		// return result;
	}

	@Override
	public Iterable<? extends Object> enumerateValues(IInputKey key, Tuple seed) {
		// ensureIndexed(key);
		//
		// if (key instanceof EClassTransitiveInstancesKey) {
		// EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
		//
		// Object seedInstance = getFromSeed(seed, 0);
		// if (seedInstance == null) { // unseeded
		// return baseIndex.getAllInstances(eClass);
		// } else {
		// // must be unseeded, this is enumerateValues after all!
		// illegalEnumerateValues(seed);
		// }
		// } else if (key instanceof EDataTypeInSlotsKey) {
		// EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
		//
		// Object seedInstance = getFromSeed(seed, 0);
		// if (seedInstance == null) { // unseeded
		// return baseIndex.getDataTypeInstances(dataType);
		// } else {
		// // must be unseeded, this is enumerateValues after all!
		// illegalEnumerateValues(seed);
		// }
		// } else if (key instanceof EStructuralFeatureInstancesKey) {
		// EStructuralFeature feature = ((EStructuralFeatureInstancesKey)
		// key).getEmfKey();
		//
		// Object seedSource = getFromSeed(seed, 0);
		// Object seedTarget = getFromSeed(seed, 1);
		// if (seedSource == null && seedTarget != null) {
		// return baseIndex.findByFeatureValue(seedTarget, feature);
		// } else if (seedSource != null && seedTarget == null) {
		// return baseIndex.getFeatureTargets((EObject) seedSource, feature);
		// } else {
		// // must be singly unseeded, this is enumerateValues after all!
		// illegalEnumerateValues(seed);
		// }
		// } else {
		// illegalInputKey(key);
		// }
		return null;
	}

}
