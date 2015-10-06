package hu.bme.mit.mondo.integration.incquery.hawk.temp;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.base.api.DataTypeListener;
import org.eclipse.incquery.runtime.base.api.FeatureListener;
import org.eclipse.incquery.runtime.base.api.InstanceListener;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.emf.EMFQueryMetaContext;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IInputKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryMetaContext;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContext;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.context.common.JavaTransitiveInstancesKey;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;

public class HawkQueryRuntimeContext2 implements IQueryRuntimeContext {

	private Logger logger;
	private NavigationHelper baseIndex;
	protected final EMFQueryMetaContext metaContext = EMFQueryMetaContext.INSTANCE;

	public HawkQueryRuntimeContext2(NavigationHelper baseIndex, Logger logger) {
		this.baseIndex = baseIndex;
		this.logger = logger;
	}

	@Override
	public IQueryMetaContext getMetaContext() {
		return metaContext;
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
		// if (key instanceof EClassTransitiveInstancesKey) {
		// EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
		//
		// // Hawk
		// String eolQuery = String.format("return %s.all.size()",
		// key.getStringID());
		//
		// Object seedInstance = getFromSeed(seed, 0);
		// if (seedInstance == null) { // unseeded
		// return baseIndex.getAllInstances(eClass).size();
		// } else { // fully seeded
		// return (containsTuple(key, seed)) ? 1 : 0;
		// }
		// } else if (key instanceof EDataTypeInSlotsKey) {
		// EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
		//
		// Object seedInstance = getFromSeed(seed, 0);
		// if (seedInstance == null) { // unseeded
		// return baseIndex.getDataTypeInstances(dataType).size();
		// } else { // fully seeded
		// return (containsTuple(key, seed)) ? 1 : 0;
		// }
		// } else if (key instanceof EStructuralFeatureInstancesKey) {
		// EStructuralFeature feature = ((EStructuralFeatureInstancesKey)
		// key).getEmfKey();
		//
		// final Object seedSource = getFromSeed(seed, 0);
		// final Object seedTarget = getFromSeed(seed, 1);
		// if (seedSource == null && seedTarget != null) {
		// return baseIndex.findByFeatureValue(seedTarget, feature).size();
		// } else if (seedSource != null && seedTarget != null) { // fully
		// // seeded
		// return (containsTuple(key, seed)) ? 1 : 0;
		// } else if (seedSource == null && seedTarget == null) { // fully
		// // unseeded
		// int result = 0;
		// Set<Entry<EObject, Set<Object>>> entrySet =
		// baseIndex.getFeatureInstances(feature).entrySet();
		// for (Entry<EObject, Set<Object>> entry : entrySet) {
		// result += entry.getValue().size();
		// }
		// return result;
		// } else if (seedSource != null && seedTarget == null) {
		// return baseIndex.getFeatureTargets((EObject) seedSource,
		// feature).size();
		// }
		// } else {
		// illegalInputKey(key);
		// }

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

	@Override
	public boolean containsTuple(IInputKey key, Tuple seed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addUpdateListener(IInputKey key, Tuple seed /* TODO ignored */, IQueryRuntimeContextListener listener) {
		// stateless, so NOP
		if (key instanceof JavaTransitiveInstancesKey)
			return;

		ensureIndexed(key);
		if (key instanceof EClassTransitiveInstancesKey) {
			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			baseIndex.addInstanceListener(Collections.singleton(eClass),
					new EClassTransitiveInstancesAdapter(listener, seed.get(0)));
		} else if (key instanceof EDataTypeInSlotsKey) {
			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			baseIndex.addDataTypeListener(Collections.singleton(dataType),
					new EDataTypeInSlotsAdapter(listener, seed.get(0)));
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			baseIndex.addFeatureListener(Collections.singleton(feature),
					new EStructuralFeatureInstancesKeyAdapter(listener, seed.get(0), seed.get(1)));
		} else {
			illegalInputKey(key);
		}
	}

	@Override
	public void removeUpdateListener(IInputKey key, Tuple seed, IQueryRuntimeContextListener listener) {
		// stateless, so NOP
		if (key instanceof JavaTransitiveInstancesKey)
			return;

		ensureIndexed(key);
		if (key instanceof EClassTransitiveInstancesKey) {
			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			baseIndex.removeInstanceListener(Collections.singleton(eClass),
					new EClassTransitiveInstancesAdapter(listener, seed.get(0)));
		} else if (key instanceof EDataTypeInSlotsKey) {
			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			baseIndex.removeDataTypeListener(Collections.singleton(dataType),
					new EDataTypeInSlotsAdapter(listener, seed.get(0)));
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			baseIndex.removeFeatureListener(Collections.singleton(feature),
					new EStructuralFeatureInstancesKeyAdapter(listener, seed.get(0), seed.get(1)));
		} else {
			illegalInputKey(key);
		}
	}

	@Override
	public Object wrapElement(Object externalElement) {
		return externalElement;
	}

	@Override
	public Object unwrapElement(Object internalElement) {
		return internalElement;
	}

	@Override
	public Tuple wrapTuple(Tuple externalElements) {
		return externalElements;
	}

	@Override
	public Tuple unwrapTuple(Tuple internalElements) {
		return internalElements;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	private static class EClassTransitiveInstancesAdapter extends ListenerAdapter implements InstanceListener {
		private Object seedInstance;

		public EClassTransitiveInstancesAdapter(IQueryRuntimeContextListener listener, Object seedInstance) {
			super(listener, seedInstance);
			this.seedInstance = seedInstance;
		}

		@Override
		public void instanceInserted(EClass clazz, EObject instance) {
			if (seedInstance != null && !seedInstance.equals(instance))
				return;
			listener.update(new EClassTransitiveInstancesKey(clazz), new FlatTuple(instance), true);
		}

		@Override
		public void instanceDeleted(EClass clazz, EObject instance) {
			if (seedInstance != null && !seedInstance.equals(instance))
				return;
			listener.update(new EClassTransitiveInstancesKey(clazz), new FlatTuple(instance), false);
		}
	}

	private static class EDataTypeInSlotsAdapter extends ListenerAdapter implements DataTypeListener {
		private Object seedValue;

		public EDataTypeInSlotsAdapter(IQueryRuntimeContextListener listener, Object seedValue) {
			super(listener, seedValue);
			this.seedValue = seedValue;
		}

		@Override
		public void dataTypeInstanceInserted(EDataType type, Object instance, boolean firstOccurrence) {
			if (firstOccurrence) {
				if (seedValue != null && !seedValue.equals(instance))
					return;
				listener.update(new EDataTypeInSlotsKey(type), new FlatTuple(instance), true);
			}
		}

		@Override
		public void dataTypeInstanceDeleted(EDataType type, Object instance, boolean lastOccurrence) {
			if (lastOccurrence) {
				if (seedValue != null && !seedValue.equals(instance))
					return;
				listener.update(new EDataTypeInSlotsKey(type), new FlatTuple(instance), false);
			}
		}
	}

	private static class EStructuralFeatureInstancesKeyAdapter extends ListenerAdapter implements FeatureListener {
		private Object seedHost;
		private Object seedValue;

		public EStructuralFeatureInstancesKeyAdapter(IQueryRuntimeContextListener listener, Object seedHost,
				Object seedValue) {
			super(listener, seedHost, seedValue);
			this.seedHost = seedHost;
			this.seedValue = seedValue;
		}

		@Override
		public void featureInserted(EObject host, EStructuralFeature feature, Object value) {
			if (seedHost != null && !seedHost.equals(host))
				return;
			if (seedValue != null && !seedValue.equals(value))
				return;
			listener.update(new EStructuralFeatureInstancesKey(feature), new FlatTuple(host, value), true);
		}

		@Override
		public void featureDeleted(EObject host, EStructuralFeature feature, Object value) {
			if (seedHost != null && !seedHost.equals(host))
				return;
			if (seedValue != null && !seedValue.equals(value))
				return;
			listener.update(new EStructuralFeatureInstancesKey(feature), new FlatTuple(host, value), false);
		}
	}

	private abstract static class ListenerAdapter {
		IQueryRuntimeContextListener listener;
		Tuple seed;

		/**
		 * @param listener
		 * @param seed
		 *            must be non-null
		 */
		public ListenerAdapter(IQueryRuntimeContextListener listener, Object... seed) {
			this.listener = listener;
			this.seed = new FlatTuple(seed);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((listener == null) ? 0 : listener.hashCode());
			result = prime * result + ((seed == null) ? 0 : seed.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj.getClass().equals(this.getClass())))
				return false;
			ListenerAdapter other = (ListenerAdapter) obj;
			if (listener == null) {
				if (other.listener != null)
					return false;
			} else if (!listener.equals(other.listener))
				return false;
			if (seed == null) {
				if (other.seed != null)
					return false;
			} else if (!seed.equals(other.seed))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Wrapped<Seed:" + seed + ">#" + listener;
		}

	}

	public void ensureValidKey(IInputKey key) {
		metaContext.ensureValidKey(key);
	}

	public void illegalInputKey(IInputKey key) {
		metaContext.illegalInputKey(key);
	}

}
