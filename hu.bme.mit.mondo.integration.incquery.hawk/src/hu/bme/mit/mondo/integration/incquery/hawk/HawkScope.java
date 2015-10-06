package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.emf.EMFScope;
import org.eclipse.incquery.runtime.exception.IncQueryException;

public class HawkScope extends EMFScope {

	public HawkScope(Notifier scopeRoot) throws IncQueryException {
		super(scopeRoot);
	}
	
	@Override
	protected IEngineContext createEngineContext(IncQueryEngine engine, IIndexingErrorListener errorListener,
			Logger logger) {
		return new HawkEngineContext(this, engine, errorListener, logger);
	}

}
