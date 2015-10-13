package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.base.api.DataTypeListener;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

import uk.ac.york.mondo.integration.hawk.emf.IHawkResourceChangeListener;

class EDataTypeInSlotsAdapter extends ListenerAdapter implements DataTypeListener, IHawkResourceChangeListener {

	private static final Logger LOGGER = Logger.getLogger(EDataTypeInSlotsAdapter.class);
	private final InstanceCounter counter = new InstanceCounter();
	private final Object seedValue;
	private final EDataType filterDataType;

	public EDataTypeInSlotsAdapter(final IQueryRuntimeContextListener listener, final EDataType filterDataType, final Object seedValue) {
		super(listener, seedValue);
		this.seedValue = seedValue;
		this.filterDataType = filterDataType;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Listening on instances of data type " + filterDataType);
		}
	}

	@Override
	public void dataTypeInstanceInserted(final EDataType type, final Object instance,
			final boolean firstOccurrence) {
		if (firstOccurrence) {
    		if (seedValue != null && !seedValue.equals(instance)) return;
			listener.update(new EDataTypeInSlotsKey(type), 
    				new FlatTuple(instance), true);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Notified of new instance of data type " + type + ": " + instance);
			}
		}
	}

	@Override
	public void dataTypeInstanceDeleted(final EDataType type, final Object instance,
			final boolean lastOccurrence) {
		if (lastOccurrence) {
    		if (seedValue != null && !seedValue.equals(instance)) return;
    		listener.update(new EDataTypeInSlotsKey(type), 
    				new FlatTuple(instance), false);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Notified of deletion of instance of data type " + type + ": " + instance);
			}
		}
	}

	public InstanceCounter getCounter() {
		return counter;
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
	public void instanceDeleted(final EClass eClass, final EObject eob) {
		// do nothing
	}

	@Override
	public void instanceInserted(final EClass eClass, final EObject eob) {
		// do nothing
	}

	@Override
	public void dataTypeDeleted(final EClassifier eType, final Object oldValue) {
		if (filterDataType == eType) {
			 dataTypeInstanceDeleted(filterDataType, oldValue, counter.remove(oldValue));
		}
	}

	@Override
	public void dataTypeInserted(final EClassifier eType, final Object newValue) {
		if (filterDataType == eType) {
			 dataTypeInstanceInserted(filterDataType, newValue, counter.add(newValue));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filterDataType == null) ? 0 : filterDataType.hashCode());
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
		final EDataTypeInSlotsAdapter other = (EDataTypeInSlotsAdapter) obj;
		if (filterDataType == null) {
			if (other.filterDataType != null)
				return false;
		} else if (!filterDataType.equals(other.filterDataType))
			return false;
		if (seedValue == null) {
			if (other.seedValue != null)
				return false;
		} else if (!seedValue.equals(other.seedValue))
			return false;
		return true;
	}

	
}