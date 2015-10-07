package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IBaseIndex;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import uk.ac.york.mondo.integration.api.Hawk.Client;

public class HawkEngineContext implements IEngineContext {

	protected HawkScope hawkScope;
	protected HawkQueryRuntimeContext hawkQueryRuntimeContext;

	protected IncQueryEngine engine;
	protected Logger logger;
	protected Client client;

	public HawkEngineContext(HawkScope hawkScope, IncQueryEngine engine, Logger logger, Client client) {
		this.hawkScope = hawkScope;
		this.client = client;
	}

	@Override
	public void initializeBackends(IQueryBackendInitializer initializer) throws IncQueryException {
		if (hawkQueryRuntimeContext == null) {
			hawkQueryRuntimeContext = new HawkQueryRuntimeContext(client, logger);
			initializer.initializeWith(hawkQueryRuntimeContext);
		}
	}

	@Override
	public IBaseIndex getBaseIndex() throws IncQueryException {
		return null;
	}

	@Override
	public void dispose() {
		if (hawkQueryRuntimeContext != null) {
			hawkQueryRuntimeContext.dispose();
		}

		this.engine = null;
		this.logger = null;
	}

}
