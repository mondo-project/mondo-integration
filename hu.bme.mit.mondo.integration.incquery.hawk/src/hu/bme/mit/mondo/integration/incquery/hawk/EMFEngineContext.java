package hu.bme.mit.mondo.integration.incquery.hawk;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IBaseIndex;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.base.api.IncQueryBaseFactory;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.base.exception.IncQueryBaseException;
import org.eclipse.incquery.runtime.emf.DynamicEMFQueryRuntimeContext;
import org.eclipse.incquery.runtime.emf.EMFBaseIndexWrapper;
import org.eclipse.incquery.runtime.emf.EMFQueryRuntimeContext;
import org.eclipse.incquery.runtime.emf.EMFScope;
import org.eclipse.incquery.runtime.exception.IncQueryException;

/**
 * Implements an engine context on EMF models.
 * @author Bergmann Gabor
 *
 */
class EMFEngineContext implements IEngineContext {

    private final EMFScope emfScope;
    IncQueryEngine engine;
    Logger logger;
    NavigationHelper navHelper;
    IBaseIndex baseIndex;
    IIndexingErrorListener taintListener;
    private EMFQueryRuntimeContext runtimeContext;
    
    public EMFEngineContext(EMFScope emfScope, IncQueryEngine engine, IIndexingErrorListener taintListener, Logger logger) {
        this.emfScope = emfScope;
        this.engine = engine;
        this.logger = logger;
        this.taintListener = taintListener;
    }
    
    public NavigationHelper getNavHelper() throws IncQueryException {
        return getNavHelper(true);
    }
    protected NavigationHelper getNavHelper(boolean ensureInitialized) throws IncQueryException {
        if (navHelper == null) {
            try {
                // sync to avoid crazy compiler reordering which would matter if derived features use eIQ and call this
                // reentrantly
                synchronized (this) {
                    navHelper = IncQueryBaseFactory.getInstance().createNavigationHelper(null, this.emfScope.getOptions(),
                            logger);
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

    protected void ensureIndexLoaded() throws IncQueryException {
        try {
            for (Notifier scopeRoot : this.emfScope.getScopeRoots()) {
                navHelper.addRoot(scopeRoot);
            }
        } catch (IncQueryBaseException e) {
            throw new IncQueryException("Could not initialize EMF-IncQuery base index",
                    "Could not initialize base index", e);
        }
    }

    @Override
    public void initializeBackends(IQueryBackendInitializer initializer) throws IncQueryException {
       try {
    	   NavigationHelper nh = getNavHelper(false);
           if (runtimeContext == null) 
        	   runtimeContext = 
        	   		emfScope.getOptions().isDynamicEMFMode() ?
        				new DynamicEMFQueryRuntimeContext(nh, logger) :
        				new EMFQueryRuntimeContext(nh, logger);
           
           initializer.initializeWith(/*logger,*/ runtimeContext);
       } finally {
           // lazy navHelper initialization now,
           ensureIndexLoaded();
       }        
    }   
    
    @Override
    public void dispose() {
        if (runtimeContext != null) runtimeContext.dispose();
        if (navHelper != null) navHelper.dispose();
        
        this.baseIndex = null;
        this.engine = null;
        this.logger = null;
        this.navHelper = null;
    }
    
    
    @Override
    public IBaseIndex getBaseIndex() throws IncQueryException {
        if (baseIndex == null) {
            final NavigationHelper navigationHelper = getNavHelper();
            baseIndex = new EMFBaseIndexWrapper(navigationHelper);
        }
        return baseIndex;
    }
}