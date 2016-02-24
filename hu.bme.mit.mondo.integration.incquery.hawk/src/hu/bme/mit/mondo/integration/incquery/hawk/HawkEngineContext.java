package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IBaseIndex;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContext;

import uk.ac.york.mondo.integration.api.Hawk.Client;

public class HawkEngineContext implements IEngineContext {

	protected HawkScope hawkScope;
	protected HawkQueryRuntimeContext<?> hawkQueryRuntimeContext;

	protected IncQueryEngine engine;
	protected Logger logger;
	protected Client client;

	public HawkEngineContext(final HawkScope hawkScope, final IncQueryEngine engine, final Logger logger, final Client client) {
		this.hawkScope = hawkScope;
		this.client = client;
	}

	// TODO: Marton should have a look at this (no initializeBackends method anymore in IEngineContext?)

//	@Override
//	public void initializeBackends(final IQueryBackendInitializer initializer) throws IncQueryException {
//		HawkResource hawkResource = null;
//		outer: for (final Notifier notifier : hawkScope.getScopeRoots()) {
//			if (notifier instanceof HawkResource) {
//				hawkResource = (HawkResource) notifier;
//				break;
//			}
//
//			if (notifier instanceof ResourceSet) {
//				for (final Resource resource : ((ResourceSet) notifier).getResources()) {
//					if (resource instanceof HawkResource) {
//						hawkResource = (HawkResource) resource;
//						break outer;
//					}
//				}
//			}
//		}
//		if (hawkResource == null) {
//			final String msg = "Could not find a HawkResource in the HawkScope.";
//			throw new IncQueryException(msg, msg);
//		}
//
//		if (hawkQueryRuntimeContext == null) {
//			hawkQueryRuntimeContext = new HawkQueryRuntimeContext<>(hawkResource, logger);
//			initializer.initializeWith(hawkQueryRuntimeContext);
//		}
//	}

	@Override
	public IBaseIndex getBaseIndex() throws IncQueryException {
		return new HawkBaseIndex();
	}

	@Override
	public void dispose() {
		if (hawkQueryRuntimeContext != null) {
			hawkQueryRuntimeContext.dispose();
		}

		this.engine = null;
		this.logger = null;
	}

	@Override
	public IQueryRuntimeContext getQueryRuntimeContext() throws IncQueryException {
		return hawkQueryRuntimeContext;
	}

}
