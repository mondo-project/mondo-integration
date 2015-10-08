package hu.bme.mit.mondo.integration.incquery.hawk;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.incquery.runtime.base.api.DataTypeListener;
import org.eclipse.incquery.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

class EDataTypeInSlotsAdapter extends ListenerAdapter implements DataTypeListener {

	private final InstanceCounter counter = new InstanceCounter();
	private Object seedValue;

	public EDataTypeInSlotsAdapter(IQueryRuntimeContextListener listener, Object seedValue) {
		super(listener, seedValue);
		this.seedValue = seedValue;
	}

	@Override
	public void dataTypeInstanceInserted(EDataType type, Object instance,
			boolean firstOccurrence) {
		if (firstOccurrence) {
    		if (seedValue != null && !seedValue.equals(instance)) return;
			listener.update(new EDataTypeInSlotsKey(type), 
    				new FlatTuple(instance), true);
		}
	}

	@Override
	public void dataTypeInstanceDeleted(EDataType type, Object instance,
			boolean lastOccurrence) {
		if (lastOccurrence) {
    		if (seedValue != null && !seedValue.equals(instance)) return;
    		listener.update(new EDataTypeInSlotsKey(type), 
    				new FlatTuple(instance), false);
		}
	}

	public InstanceCounter getCounter() {
		return counter;
	}

}