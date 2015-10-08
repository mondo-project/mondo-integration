package hu.bme.mit.mondo.integration.incquery.hawk;

import org.eclipse.incquery.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.incquery.runtime.matchers.tuple.FlatTuple;
import org.eclipse.incquery.runtime.matchers.tuple.Tuple;

/**
 * Abstract internal listener wrapper for a {@link IQueryRuntimeContextListener}. 
 * Due to the overridden equals/hashCode(), it is safe to create a new instance for the same listener.
 * 
 * @author Bergmann Gabor
 */
abstract class ListenerAdapter { 
	IQueryRuntimeContextListener listener;
	Tuple seed;
	/**
	 * @param listener
	 * @param seed must be non-null
	 */
	public ListenerAdapter(IQueryRuntimeContextListener listener, Object... seed) {
		this.listener = listener;
		this.seed = new FlatTuple(seed);
	}
			
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((listener == null) ? 0 : listener.hashCode());
		result = prime * result + ((seed == null) ? 0 : seed.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj.getClass().equals(this.getClass())))
			return false;
		ListenerAdapter other = (ListenerAdapter) obj;
		if (listener == null) {
			if (other.listener != null)
				return false;
		} else if (!listener.equals(other.listener))
			return false;
		if (seed == null) {
			if (other.seed != null)
				return false;
		} else if (!seed.equals(other.seed))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Wrapped<Seed:" + seed + ">#" + listener;
	}
	
	
}