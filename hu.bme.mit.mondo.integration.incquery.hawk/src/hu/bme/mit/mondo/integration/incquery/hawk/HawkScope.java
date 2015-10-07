package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.emf.EMFScope;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import uk.ac.york.mondo.integration.api.Hawk.Client;

public class HawkScope extends EMFScope {

	private Client client;

	public HawkScope(Notifier scopeRoot, Client client) throws IncQueryException {
		super(scopeRoot);
		this.client = client;
	}
	
	@Override
	protected IEngineContext createEngineContext(IncQueryEngine engine, IIndexingErrorListener errorListener,
			Logger logger) {
		return new HawkEngineContext(this, engine, logger, client);
	}

}
