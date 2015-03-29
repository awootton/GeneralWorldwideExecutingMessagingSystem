package org.messageweb.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A map from an Item to a list of Things and also a map from a Thing to a list of items
 * 
 * @author awootton
 * 
 * @param <I>
 * @param <T>
 */

public class TwoWayMapping<I extends Comparable<? super I>, T extends Comparable<? super T>> {
	static Logger logger = Logger.getLogger(TwoWayMapping.class);

	protected Map<I, Set<T>> item2things = null;
	protected Map<T, Set<I>> thing2items = null;

	public int thingSetSize = 4;
	public int itemSetSize = 4;

	public TwoWayMapping() {
		initConcurrentHashMaps();
	}

	void initConcurrentHashMaps() {
		item2things = new HashMap<I, Set<T>>();
		thing2items = new HashMap<T, Set<I>>();
	}

	protected Set<I> itemSetFactory() {
		return new HashSet<I>(itemSetSize);
	}

	protected Set<I> itemSetFactory(Collection<I> list) {
		if (list == null)
			return itemSetFactory();
		return new HashSet<I>(list);
	}

	protected Set<T> thingSetFactory() {
		return new HashSet<T>(thingSetSize);
	}

	protected Set<T> thingSetFactory(Collection<T> list) {
		if (list == null)
			return thingSetFactory();
		return new HashSet<T>(list);
	}

	public synchronized Set<T> item2things_get(I item) {
		Set<T> tmp = item2things.get(item);
		return thingSetFactory(tmp);
	}

	public synchronized int item2things_size(I item) {
		Set<T> tmp = item2things.get(item);
		if (tmp == null)
			return 0;
		return tmp.size();
	}

	public synchronized Set<I> thing2items_get(T thing) {
		Set<I> tmp = thing2items.get(thing);
		return itemSetFactory(tmp);
	}

	public synchronized int thing2items_size(T thing) {
		Set<I> tmp = thing2items.get(thing);
		if (tmp == null)
			return 0;
		return tmp.size();
	}

	public synchronized Set<T> thing2items_keySet() {
		Set<T> tmp = thing2items.keySet();
		return thingSetFactory(tmp);
	}

	public synchronized Set<I> item2things_keySet() {
		Set<I> tmp = item2things.keySet();
		return itemSetFactory(tmp);
	}

	public synchronized int item2things_size() {
		return item2things.keySet().size();
	}

	public synchronized int thing2items_size() {
		return thing2items.keySet().size();
	}

	/**
	 * item or thing can be null and it will still init the table
	 * 
	 * @param item
	 * @param thing
	 */

	public synchronized void add(I item, T thing) {
		if (item != null) {
			Set<T> things = item2things.get(item);
			if (things == null) {
				things = thingSetFactory();
				item2things.put(item, things);
			}
			if (thing != null)
			// synchronized (things)
			{
				things.add(thing);
			}
		}
		if (thing != null) {
			Set<I> items = thing2items.get(thing);
			if (items == null) {
				items = itemSetFactory();
				thing2items.put(thing, items);
			}
			if (item != null)
			// synchronized (items)
			{
				items.add(item);
			}
		}
	}

	public synchronized Orphan<I, T> remove(I session, T widget)// remove session from widget, and widget from
	// session
	{
		Orphan<I, T> orphan = new Orphan<I, T>();
		Set<I> sessions = thing2items.get(widget);
		if (sessions != null) {
			// synchronized (sessions)
			{
				sessions.remove(session);
			}
			if (sessions.size() == 0) {
				orphan.thing = widget;// this widget no longer exists
				thing2items.remove(widget);
			}
		}
		Set<T> widgets = item2things.get(session);
		if (widgets != null) {
			// synchronized (widgets)
			{
				widgets.remove(widget);
			}
			if (widgets.size() == 0) {
				orphan.item = session;
				item2things.remove(session);// this session no longer exists
			}
		}
		return orphan;
	}

	// remove all the items associated with this thing, return the list
	public synchronized Set<I> removeThing(T thing)// remove widget
	{
		Set<I> tmp = thing2items.get(thing);
		if (tmp == null)
			tmp = new HashSet<I>();
		Set<I> items = new HashSet<I>(tmp); //
		for (I item : items) {
			remove(item, thing);
		}
		return items;
	}

	public synchronized Set<T> removeItem(I item)// remove item
	{
		Set<T> tmp = item2things.get(item);
		if (tmp == null)
			tmp = new HashSet<T>();
		Set<T> things = new HashSet<T>(tmp); //
		for (T thing : things) {
			remove(item, thing);
		}
		return things;
	}

	// FIXME return string

	public synchronized void dumpItems() {
		Set<I> set = item2things.keySet();
		List<I> list = new ArrayList<I>(set);
		Collections.sort(list);
		for (I item : list) {
			Set<T> things = item2things.get(item);
			System.out.print("item=" + item);
			for (T t : things) {
				System.out.print(" " + t);
			}
			System.out.println();
		}
	}

	// FIXME return string

	public synchronized void dumpThings() {
		Set<T> set = thing2items.keySet();
		List<T> list = new ArrayList<T>(set);
		Collections.sort(list);
		for (T thing : list) {
			Set<I> items = thing2items.get(thing);
			System.out.print("thing=" + thing);
			for (I i : items) {
				System.out.print(" " + i);
			}
			System.out.println();
		}
	}

}
