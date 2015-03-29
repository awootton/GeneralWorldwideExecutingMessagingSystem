package org.messageweb.util;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TwoWayTreeMapping<I extends Comparable<? super I>, T extends Comparable<? super T>> extends TwoWayMapping<I, T> {
	public TwoWayTreeMapping() {
		initPlainTreeMaps();
	}

	void initPlainTreeMaps() {
		item2things = new TreeMap<I, Set<T>>();
		thing2items = new TreeMap<T, Set<I>>();
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
