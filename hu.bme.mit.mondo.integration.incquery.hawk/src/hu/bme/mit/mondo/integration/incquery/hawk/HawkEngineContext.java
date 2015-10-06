package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.exception.IncQueryException;

public class HawkEngineContext extends EMFEngineContext {

	protected HawkScope hawkScope;
	protected HawkQueryRuntimeContext hawkQueryRuntimeContext; 
	
	public HawkEngineContext(HawkScope hawkScope, IncQueryEngine engine, IIndexingErrorListener taintListener,
			Logger logger) {
		super(hawkScope, engine, taintListener, logger);
		this.hawkScope = hawkScope;
	}
	
	@Override
	public void initializeBackends(IQueryBackendInitializer initializer) throws IncQueryException {
		try {
			NavigationHelper nh = getNavHelper(false);
			if (hawkQueryRuntimeContext == null)
				hawkQueryRuntimeContext = new HawkQueryRuntimeContext(nh, logger);

			initializer.initializeWith(/* logger, */ hawkQueryRuntimeContext);
		} finally {
			// lazy navHelper initialization now,
			ensureIndexLoaded();
		}
	}

}
