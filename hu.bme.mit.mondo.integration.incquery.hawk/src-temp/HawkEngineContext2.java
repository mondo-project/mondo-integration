package hu.bme.mit.mondo.integration.incquery.hawk.temp;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IBaseIndex;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.base.api.IncQueryBaseFactory;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.base.exception.IncQueryBaseException;
import org.eclipse.incquery.runtime.emf.EMFBaseIndexWrapper;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import hu.bme.mit.mondo.integration.incquery.hawk.HawkQueryRuntimeContext;

public class HawkEngineContext2 implements IEngineContext {

	private IBaseIndex baseIndex;
	private NavigationHelper navHelper;
	private HawkScope hawkScope;
	private IncQueryEngine engine;
	private IIndexingErrorListener taintListener;
	private Logger logger;
	private HawkQueryRuntimeContext hawkRuntimeContext;

	public HawkEngineContext2(HawkScope hawkScope, IncQueryEngine engine, IIndexingErrorListener taintListener,
			Logger logger) {
		this.hawkScope = hawkScope;
		this.engine = engine;
		this.taintListener = taintListener;
		this.logger = logger;
	}

	@Override
	public IBaseIndex getBaseIndex() throws IncQueryException {
		if (baseIndex == null) {
			final NavigationHelper navigationHelper = getNavHelper();
			baseIndex = new EMFBaseIndexWrapper(navigationHelper);
		}
		return baseIndex;
	}

	@Override
	public void initializeBackends(IQueryBackendInitializer initializer) throws IncQueryException {
		try {
			NavigationHelper nh = getNavHelper(false);
			if (hawkRuntimeContext == null)
				hawkRuntimeContext = new HawkQueryRuntimeContext(nh, logger);

			initializer.initializeWith(/* logger, */ hawkRuntimeContext);
		} finally {
			// lazy navHelper initialization now,
			ensureIndexLoaded();
		}
	}

	@Override
	public void dispose() {
		if (hawkRuntimeContext != null)
			hawkRuntimeContext.dispose();
		if (navHelper != null)
			navHelper.dispose();

		this.baseIndex = null;
		this.engine = null;
		this.logger = null;
		this.navHelper = null;
	}

	public NavigationHelper getNavHelper() throws IncQueryException {
		return getNavHelper(true);
	}

	private NavigationHelper getNavHelper(boolean ensureInitialized) throws IncQueryException {
		if (navHelper == null) {
			try {
				// sync to avoid crazy compiler reordering which would matter if
				// derived features use eIQ and call this
				// reentrantly
				synchronized (this) {
					navHelper = IncQueryBaseFactory.getInstance().createNavigationHelper(null,
							this.hawkScope.getOptions(), logger);
					getBaseIndex().addIndexingErrorListener(taintListener);
				}
			} catch (IncQueryBaseException e) {
				throw new IncQueryException("Could not create EMF-IncQuery base index", "Could not create base index",
						e);
			}

			if (ensureInitialized) {
				ensureIndexLoaded();
			}

		}
		return navHelper;
	}

	private void ensureIndexLoaded() throws IncQueryException {
		try {
			for (Notifier scopeRoot : this.hawkScope.getScopeRoots()) {
				navHelper.addRoot(scopeRoot);
			}
		} catch (IncQueryBaseException e) {
			throw new IncQueryException("Could not initialize EMF-IncQuery base index",
					"Could not initialize base index", e);
		}
	}

}
