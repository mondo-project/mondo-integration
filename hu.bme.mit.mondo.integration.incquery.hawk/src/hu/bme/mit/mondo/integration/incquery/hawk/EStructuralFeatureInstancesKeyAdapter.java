package hu.bme.mit.mondo.integration.incquery.hawk;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.base.api.FeatureListener;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

import uk.ac.york.mondo.integration.hawk.emf.IHawkResourceChangeListener;

class EStructuralFeatureInstancesKeyAdapter extends ListenerAdapter implements FeatureListener, IHawkResourceChangeListener {
	private final Object seedHost;
	private final Object seedValue;
	private final EStructuralFeature filterFeature;

	public EStructuralFeatureInstancesKeyAdapter(final IQueryRuntimeContextListener listener, final EStructuralFeature filterSF, final Object seedHost,
			final Object seedValue) {
		super(listener, seedHost, seedValue);
		this.filterFeature = filterSF;
		this.seedHost = seedHost;
		this.seedValue = seedValue;
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