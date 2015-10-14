package hu.bme.mit.mondo.integration.incquery.hawk;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
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

import uk.ac.york.mondo.integration.hawk.emf.HawkResource;

public class HawkQueryRuntimeContext<E> extends EMFQueryRuntimeContext {

	protected String clean(final String s) {
		return s.replaceAll("http://www.semanticweb.org/ontologies/2015/trainbenchmark/", "")
				.replaceAll("hu\\.bme\\.mit\\.trainbenchmark\\.railway\\.impl\\.", "").replaceAll(";", "); ");
	}

	private final HawkResource hawkResource;

	public HawkQueryRuntimeContext(final HawkResource hawkResource, final Logger logger) {
		super(null, logger);
		this.hawkResource = hawkResource;
	}

	@Override
	public <V> V coalesceTraversals(final Callable<V> callable) throws InvocationTargetException {
		try {
			return callable.call();
		} catch (final Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	@Override
	public boolean isCoalescing() {
		return false;
	}

	@Override
	public boolean isIndexed(final IInputKey key) {
		return true;
	}

	@Override
	public void ensureIndexed(final IInputKey key) {
		// do nothing
	}

	@Override
	public int countTuples(final IInputKey key, final Tuple seed) {
		int result = 0;

		try {
			if (key instanceof EClassTransitiveInstancesKey) {
				final EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
				final Object seedInstance = getFromSeed(seed, 0);

				final EList<EObject> instances = hawkResource.fetchNodes(eClass);
				if (seedInstance == null) { // unseeded
					result = instances.size();
				} else { // fully seeded
					result = instances.contains(seedInstance) ? 1 : 0;
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				final EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
				final Object seedInstance = getFromSeed(seed, 0);

				final List<Object> values = hawkResource.fetchValuesByEClassifier(dataType);
				if (seedInstance == null) { // unseeded
					result = values.size();
				} else { // fully seeded
					result = values.contains(seedInstance) ? 1 : 0;
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);

				final Map<EObject, Object> featureValues = hawkResource.fetchValuesByEStructuralFeature(feature);

				if (seedSource == null && seedTarget != null) {
					result = featureValues.values().contains(seedTarget) ? 1 : 0;
				} else if (seedSource != null && seedTarget != null) { // fully
																		// seeded
					result = seedTarget.equals(featureValues.get(seedSource)) ? 1 : 0;
				} else if (seedSource == null && seedTarget == null) { // fully
																		// unseeded
					result = featureValues.size();
				} else if (seedSource != null && seedTarget == null) {
					final Object value = featureValues.get(seedSource);
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
	public Iterable<Tuple> enumerateTuples(final IInputKey key, final Tuple seed) {
		Iterable<Tuple> result = null;
		try {

			if (key instanceof EClassTransitiveInstancesKey) {
				final EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
				final Object seedInstance = getFromSeed(seed, 0);

				final EList<EObject> instances = hawkResource.fetchNodes(eClass);
				if (seedInstance == null) { // unseeded
					result = Iterables.transform(instances, wrapUnary);
				} else { // fully seeded
					if (instances.contains(seedInstance)) {
						result = Arrays.asList(new FlatTuple(seedInstance));
					}
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				final EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
				final Object seedInstance = getFromSeed(seed, 0);

				final List<Object> values = hawkResource.fetchValuesByEClassifier(dataType);
				if (seedInstance == null) { // unseeded
					result = Iterables.transform(values, wrapUnary);
				} else { // fully seeded
					if (values.contains(seedInstance)) {
						result = Arrays.asList(new FlatTuple(seedInstance));
					}
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);

				final Map<EObject, Object> featureValues = hawkResource.fetchValuesByEStructuralFeature(feature);

				final List<Tuple> tuples = new ArrayList<>();
				if (seedSource == null && seedTarget != null) {
					for (final Entry<EObject, Object> entry : featureValues.entrySet()) {
						if (entry.getValue().equals(seedTarget)) {
							tuples.add(new FlatTuple(entry.getKey(), entry.getValue()));
						}
					}
				} else if (seedSource != null && seedTarget != null) { // fully
																		// seeded
					if (seedTarget.equals(featureValues.get(seedSource))) {
						tuples.add(new FlatTuple(seedSource, seedTarget));
					}
				} else if (seedSource == null && seedTarget == null) { // fully
																		// unseeded
					for (final Entry<EObject, Object> entry : featureValues.entrySet()) {
						if (entry.getValue() instanceof Collection) {
							for (final Object value : (Collection<?>) entry.getValue()) {
								tuples.add(new FlatTuple(entry.getKey(), value));
							}
						} else {
							tuples.add(new FlatTuple(entry.getKey(), entry.getValue()));
						}
					}
				} else if (seedSource != null && seedTarget == null) {
					final Object value = featureValues.get(seedSource);
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
		// System.out.println(clean(result.toString()));
		int n = 0;
		for (final Tuple tuple : result) {
			n++;
		}
		System.out.println(n);
		System.out.println();

		return result;
	}

	@Override
	public Iterable<? extends Object> enumerateValues(final IInputKey key, final Tuple seed) {
		Iterable<? extends Object> result = null;
		try {

			if (key instanceof EClassTransitiveInstancesKey) {

				final EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();

				final Object seedInstance = getFromSeed(seed, 0);
				if (seedInstance == null) { // unseeded
					result = hawkResource.fetchNodes(eClass);
				} else {
					// must be unseeded, this is enumerateValues after all!
					illegalEnumerateValues(seed);
				}
			} else if (key instanceof EDataTypeInSlotsKey) {
				final EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();

				final Object seedInstance = getFromSeed(seed, 0);
				if (seedInstance == null) { // unseeded
					result = hawkResource.fetchValuesByEClassifier(dataType);
				} else {
					// must be unseeded, this is enumerateValues after all!
					illegalEnumerateValues(seed);
				}
			} else if (key instanceof EStructuralFeatureInstancesKey) {
				final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();

				final Map<EObject, Object> features = hawkResource.fetchValuesByEStructuralFeature(feature);

				final Object seedSource = getFromSeed(seed, 0);
				final Object seedTarget = getFromSeed(seed, 1);
				if (seedSource == null && seedTarget != null) {
					result = features.keySet();
				} else if (seedSource != null && seedTarget == null) {
					result = features.values();
				} else {
					// must be singly unseeded, this is enumerateValues after
					// all!
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

	protected Object getFromSeed(final Tuple seed, final int index) {
		return seed == null ? null : seed.get(index);
	}

	protected static Function<Object, Tuple> wrapUnary = new Function<Object, Tuple>() {
		@Override
		public Tuple apply(final Object obj) {
			return new FlatTuple(obj);
		}
	};

	@Override
	public void addUpdateListener(final IInputKey key, final Tuple seed, final IQueryRuntimeContextListener listener) {
		// stateless, so NOP
		if (key instanceof JavaTransitiveInstancesKey)
			return;

		if (key instanceof EClassTransitiveInstancesKey) {
			final EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			final Object seedInstance = seed.get(0);
			hawkResource.addChangeListener(new EClassTransitiveInstancesAdapter(listener, eClass, seedInstance));
		} else if (key instanceof EDataTypeInSlotsKey) {
			final EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			final Object seedInstance = seed.get(0);
			final EDataTypeInSlotsAdapter incqAdapter = new EDataTypeInSlotsAdapter(listener, dataType, seedInstance);

			try {
				final InstanceCounter occurrences = incqAdapter.getCounter();
				final Map<EClass, List<EAttribute>> candidateTypes = hawkResource.fetchTypesWithEClassifier(dataType);

				// Instrument existing instances of the candidate EClasses
				for (final Entry<EClass, List<EAttribute>> entry : candidateTypes.entrySet()) {
					final EClass eClass = entry.getKey();
					final List<EAttribute> eAttrs = entry.getValue();
					for (final EObject eob : hawkResource.fetchNodes(eClass)) {
						for (final EAttribute attr : eAttrs) {
							final Object value = eob.eGet(attr);
							if (value instanceof Iterable) {
								for (final Object o : (Iterable<?>) value) {
									occurrences.add(o);
								}
							} else if (value != null) {
								occurrences.add(value);
							}
						}
					}
				}

				hawkResource.addChangeListener(incqAdapter);
			} catch (TException | IOException e) {
				throw new RuntimeException(e);
			}

		} else if (key instanceof EStructuralFeatureInstancesKey) {
			final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			final Object seedHost = seed.get(0);
			final Object seedValue = seed.get(1);

			try {
				hawkResource.fetchNodes(feature.getEContainingClass());
				final EStructuralFeatureInstancesKeyAdapter incqAdapter = new EStructuralFeatureInstancesKeyAdapter(
						listener, feature, seedHost, seedValue);
				hawkResource.addChangeListener(incqAdapter);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			illegalInputKey(key);
		}
	}

	@Override
	public void removeUpdateListener(final IInputKey key, final Tuple seed,
			final IQueryRuntimeContextListener listener) {
		if (key instanceof JavaTransitiveInstancesKey)
			return;

		if (key instanceof EClassTransitiveInstancesKey) {
			final EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			final Object seedInstance = seed.get(0);
			final EClassTransitiveInstancesAdapter incqAdapter = new EClassTransitiveInstancesAdapter(listener, eClass,
					seedInstance);
			hawkResource.removeChangeListener(incqAdapter);
		} else if (key instanceof EDataTypeInSlotsKey) {
			final EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			final Object seedInstance = seed.get(0);
			final EDataTypeInSlotsAdapter incqAdapter = new EDataTypeInSlotsAdapter(listener, dataType, seedInstance);
			hawkResource.removeChangeListener(incqAdapter);
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			final EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			final Object seedHost = seed.get(0);
			final Object seedValue = seed.get(1);
			final EStructuralFeatureInstancesKeyAdapter incqAdapter = new EStructuralFeatureInstancesKeyAdapter(
					listener, feature, seedHost, seedValue);
			hawkResource.removeChangeListener(incqAdapter);
		} else {
			illegalInputKey(key);
		}
	}

}
