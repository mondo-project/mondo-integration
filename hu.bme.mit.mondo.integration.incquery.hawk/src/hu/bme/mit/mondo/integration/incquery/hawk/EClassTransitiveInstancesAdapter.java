package hu.bme.mit.mondo.integration.incquery.hawk;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.incquery.runtime.base.api.InstanceListener;
import org.eclipse.incquery.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

class EClassTransitiveInstancesAdapter extends ListenerAdapter implements InstanceListener {
	private Object seedInstance;
	public EClassTransitiveInstancesAdapter(IQueryRuntimeContextListener listener, Object seedInstance) {
		super(listener, seedInstance);
		this.seedInstance = seedInstance;
	}
	@Override
	public void instanceInserted(EClass clazz, EObject instance) {
		if (seedInstance != null && !seedInstance.equals(instance)) return;
		listener.update(new EClassTransitiveInstancesKey(clazz), 
				new FlatTuple(instance), true);
	}
	@Override
	public void instanceDeleted(EClass clazz, EObject instance) {
		if (seedInstance != null && !seedInstance.equals(instance)) return;
		listener.update(new EClassTransitiveInstancesKey(clazz), 
				new FlatTuple(instance), false);
	}    	
}