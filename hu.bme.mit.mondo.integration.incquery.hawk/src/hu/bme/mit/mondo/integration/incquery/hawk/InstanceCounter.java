package hu.bme.mit.mondo.integration.incquery.hawk;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

final class InstanceCounter {
	private final Map<Object, Integer> counts = new HashMap<>();

	/**
	 * Counts an additional instance of a specific value. Returns true if this
	 * is the first instance of that value.
	 */
	public boolean add(Object o) {
		Integer count = counts.get(o);
		if (count == null) {
			count = 0;
		}
		count++;
		counts.put(o, count);
		return count == 1;
	}

	/**
	 * Counts a removed instance of a specific value. Returns true if this is
	 * the last instance of that value (the next call to {@link #add(Object)}
	 * with the same value will return <code>true</code>).
	 * 
	 * @throws NoSuchElementException
	 *             The value was never counted before.
	 */
	public boolean remove(Object o) {
		Integer count = counts.get(o);
		if (count == null) {
			throw new NoSuchElementException();
		}
		count--;
		if (count == 0) {
			counts.remove(o);
		} else {
			counts.put(o, count);
		}
		return count == 0;
	}
}