package hu.bme.mit.mondo.integration.incquery.hawk;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.eclipse.incquery.runtime.api.scope.IBaseIndex;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.api.scope.IInstanceObserver;
import org.eclipse.incquery.runtime.api.scope.IncQueryBaseIndexChangeListener;

public class HawkBaseIndex implements IBaseIndex {

	@Override
	public <V> V coalesceTraversals(Callable<V> callable) throws InvocationTargetException {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	@Override
	public void addBaseIndexChangeListener(IncQueryBaseIndexChangeListener listener) {
	}

	@Override
	public void removeBaseIndexChangeListener(IncQueryBaseIndexChangeListener listener) {
	}

	@Override
	public void resampleDerivedFeatures() {
	}

	@Override
	public boolean addIndexingErrorListener(IIndexingErrorListener listener) {
		return false;
	}

	@Override
	public boolean removeIndexingErrorListener(IIndexingErrorListener listener) {
		return false;
	}

	@Override
	public boolean addInstanceObserver(IInstanceObserver observer, Object observedObject) {
		return false;
	}

	@Override
	public boolean removeInstanceObserver(IInstanceObserver observer, Object observedObject) {
		return false;
	}

}
