package org.gwems.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/** Use a hash map for the main look up but use trees for the sets of items.
 * 
 * @author awootton
 *
 * @param <I>
 * @param <T>
 */
public class HalfHashTwoWayMapping<I extends Comparable<? super I>, T extends Comparable<? super T>> extends TwoWayMapping<I, T> {
	public HalfHashTwoWayMapping() {
		initPlainTreeMaps();
	}

	void initPlainTreeMaps() {
		item2things = new HashMap<I, Set<T>>();
		thing2items = new HashMap<T, Set<I>>();
	}

	protected Set<I> itemSetFactory() {
		return new TreeSet<I>();
	}

	protected Set<I> itemSetFactory(Collection<I> items) {
		if (items == null)
			return itemSetFactory();
		return new TreeSet<I>(items);
	}

	protected Set<T> thingSetFactory() {
		return new TreeSet<T>();
	}

	protected Set<T> thingSetFactory(Collection<T> things) {
		if (things == null)
			return thingSetFactory();
		return new TreeSet<T>(things);
	}
}
