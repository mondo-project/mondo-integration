package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.base.api.InstanceListener;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.hawk.emfresource.HawkResourceChangeListener;

class EClassTransitiveInstancesAdapter extends ListenerAdapter implements InstanceListener, HawkResourceChangeListener {

	private static final Logger LOGGER = Logger.getLogger(EClassTransitiveInstancesAdapter.class);
	private final Object seedInstance;
	private final EClass filterClass;

	public EClassTransitiveInstancesAdapter(final IQueryRuntimeContextListener listener, final EClass filterClass, final Object seedInstance) {
		super(listener, seedInstance);
		this.seedInstance = seedInstance;
		this.filterClass = filterClass;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Listening on instances of class " + filterClass);
		}
	}

	@Override
	public void instanceInserted(final EClass clazz, final EObject instance) {
		if (filterClass.isSuperTypeOf(clazz)) {
			if (seedInstance != null && !seedInstance.equals(instance))
				return;
			listener.update(new EClassTransitiveInstancesKey(clazz), new FlatTuple(instance), true);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Notified of new instance of type " + filterClass + ": " + instance);
			}
		}
	}

	@Override
	public void instanceDeleted(final EClass clazz, final EObject instance) {
		if (filterClass.isSuperTypeOf(clazz)) {
			if (seedInstance != null && !seedInstance.equals(instance))
				return;
			listener.update(new EClassTransitiveInstancesKey(clazz), new FlatTuple(instance), false);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Notified of deleted instance of type " + filterClass + ": " + instance);
			}
		}
	}

	@Override
	public void featureInserted(final EObject source, final EStructuralFeature eAttr, final Object o) {
		// do nothing
	}

	@Override
	public void featureDeleted(final EObject eob, final EStructuralFeature eAttr, final Object oldValue) {
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
		result = prime * result + ((filterClass == null) ? 0 : filterClass.hashCode());
		result = prime * result + ((seedInstance == null) ? 0 : seedInstance.hashCode());
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
		final EClassTransitiveInstancesAdapter other = (EClassTransitiveInstancesAdapter) obj;
		if (filterClass == null) {
			if (other.filterClass != null)
				return false;
		} else if (!filterClass.equals(other.filterClass))
			return false;
		if (seedInstance == null) {
			if (other.seedInstance != null)
				return false;
		} else if (!seedInstance.equals(other.seedInstance))
			return false;
		return true;
	}
	
}