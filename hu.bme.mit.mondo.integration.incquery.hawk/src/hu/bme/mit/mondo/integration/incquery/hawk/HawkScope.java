package hu.bme.mit.mondo.integration.incquery.hawk;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.scope.IEngineContext;
import org.eclipse.incquery.runtime.api.scope.IIndexingErrorListener;
import org.eclipse.incquery.runtime.api.scope.IncQueryScope;
import org.eclipse.incquery.runtime.base.api.BaseIndexOptions;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

public class HawkScope extends IncQueryScope {

	private BaseIndexOptions options = new BaseIndexOptions();
	private Set<? extends Notifier> scopeRoots;

	public HawkScope(Set<? extends Notifier> scopeRoots, BaseIndexOptions options) throws IncQueryException {
		super();
		if (scopeRoots.isEmpty()) {
			throw new IllegalArgumentException("No scope roots given");
		} else if (scopeRoots.size() == 1) {
			checkScopeRoots(scopeRoots, Predicates.or(ImmutableSet.of(Predicates.instanceOf(EObject.class),
					Predicates.instanceOf(Resource.class), Predicates.instanceOf(ResourceSet.class))));
		} else {
			checkScopeRoots(scopeRoots, Predicates.instanceOf(ResourceSet.class));
		}
		this.scopeRoots = ImmutableSet.copyOf(scopeRoots);
		this.options = options.copy();
	}

	private void checkScopeRoots(Set<? extends Notifier> scopeRoots, Predicate<Object> predicate)
			throws IncQueryException {
		for (Notifier scopeRoot : scopeRoots) {
			if (!predicate.apply(scopeRoot))
				throw new IncQueryException(
						IncQueryException.INVALID_EMFROOT
								+ (scopeRoot == null ? "(null)" : scopeRoot.getClass().getName()),
						IncQueryException.INVALID_EMFROOT_SHORT);
		}
	}

	@Override
	protected IEngineContext createEngineContext(IncQueryEngine engine, IIndexingErrorListener errorListener,
			Logger logger) {
		// TODO Auto-generated method stub
		return new HawkEngineContext(this, engine, errorListener, logger);
	}

	public BaseIndexOptions getOptions() {
		return options.copy();
	}

	public Set<? extends Notifier> getScopeRoots() {
		return scopeRoots;
	}

}
