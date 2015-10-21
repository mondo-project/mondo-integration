package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.base.api.FeatureListener;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.hawk.emfresource.HawkResourceChangeListener;

class EStructuralFeatureInstancesKeyAdapter extends ListenerAdapter implements FeatureListener, HawkResourceChangeListener {
	private final Object seedHost;
	private final Object seedValue;
	private final EStructuralFeature filterFeature;
	private static final Logger LOGGER = Logger.getLogger(EStructuralFeatureInstancesKeyAdapter.class);

	public EStructuralFeatureInstancesKeyAdapter(final IQueryRuntimeContextListener listener, final EStructuralFeature filterSF, final Object seedHost,
			final Object seedValue) {
		super(listener, seedHost, seedValue);
		this.filterFeature = filterSF;
		this.seedHost = seedHost;
		this.seedValue = seedValue;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Listening on values of structural feature " + filterFeature.getEContainingClass().getName() + "." + filterFeature.getName());
		}
	}

	@Override
	public void featureInserted(final EObject host, final EStructuralFeature feature, final Object value) {
		if (feature != filterFeature)
			return;
		if (seedHost != null && !seedHost.equals(host))
			return;
		if (seedValue != null && !seedValue.equals(value))
			return;

		listener.update(new EStructuralFeatureInstancesKey(feature), new FlatTuple(host, value), true);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Notified of feature insertion: feature " + feature.getEContainingClass().getName() + "." + feature.getName() + " in " + host + ": new value " + value);
		}
	}

	@Override
	public void featureDeleted(final EObject host, final EStructuralFeature feature, final Object value) {
		if (feature != filterFeature)
			return;
		if (seedHost != null && !seedHost.equals(host))
			return;
		if (seedValue != null && !seedValue.equals(value))
			return;

		listener.update(new EStructuralFeatureInstancesKey(feature), new FlatTuple(host, value), false);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Notified of feature deletion: feature " + feature.getEContainingClass().getName() + "." + feature.getName() + " in " + host + ": old value " + value);
		}
	}

	@Override
	public void instanceDeleted(final EClass eClass, final EObject eob) {
		// do nothing
	}

	@Override
	public void instanceInserted(final EClass eClass, final EObject eob) {
		// do nothing
	}

	@Override
	public void dataTypeDeleted(final EClassifier eType, final Object oldValue) {
		// do nothing
	}

	@Override
	public void dataTypeInserted(final EClassifier eType, final Object newValue) {
		// do nothing
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filterFeature == null) ? 0 : filterFeature.hashCode());
		result = prime * result + ((seedHost == null) ? 0 : seedHost.hashCode());
		result = prime * result + ((seedValue == null) ? 0 : seedValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EStructuralFeatureInstancesKeyAdapter other = (EStructuralFeatureInstancesKeyAdapter) obj;
		if (filterFeature == null) {
			if (other.filterFeature != null)
				return false;
		} else if (!filterFeature.equals(other.filterFeature))
			return false;
		if (seedHost == null) {
			if (other.seedHost != null)
				return false;
		} else if (!seedHost.equals(other.seedHost))
			return false;
		if (seedValue == null) {
			if (other.seedValue != null)
				return false;
		} else if (!seedValue.equals(other.seedValue))
			return false;
		return true;
	}
	
}