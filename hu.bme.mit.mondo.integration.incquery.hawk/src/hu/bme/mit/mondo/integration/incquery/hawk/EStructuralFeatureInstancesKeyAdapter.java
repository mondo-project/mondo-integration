package hu.bme.mit.mondo.integration.incquery.hawk;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.base.api.FeatureListener;
import org.eclipse.incquery.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;

class EStructuralFeatureInstancesKeyAdapter extends ListenerAdapter implements FeatureListener {
	private Object seedHost;
	private Object seedValue;
	public EStructuralFeatureInstancesKeyAdapter(IQueryRuntimeContextListener listener, Object seedHost, Object seedValue) {
		super(listener, seedHost, seedValue);
		this.seedHost = seedHost;
		this.seedValue = seedValue;
	}
	@Override
	public void featureInserted(EObject host, EStructuralFeature feature,
			Object value) {
		if (seedHost != null && !seedHost.equals(host)) return;
		if (seedValue != null && !seedValue.equals(value)) return;
		listener.update(new EStructuralFeatureInstancesKey(feature), 
				new FlatTuple(host, value), true);
	}
	@Override
	public void featureDeleted(EObject host, EStructuralFeature feature,
			Object value) {
		if (seedHost != null && !seedHost.equals(host)) return;
		if (seedValue != null && !seedValue.equals(value)) return;
		listener.update(new EStructuralFeatureInstancesKey(feature), 
				new FlatTuple(host, value), false);
	}    	
}