package org.messageweb.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/** FIXME: this will be a bottleneck at high volume.
 * 
 * @author awootton
 *
 */
public class TimeoutCache {

	// use a map and also a priority Q of keys and also a thread.

	Cache<String, ObjectHolder> cache = CacheBuilder.newBuilder().concurrencyLevel(8).build();

	TreeSet<TimedKey> timedQ;

	Thread watcher;
	Executor pool;
	Thread thread;

	public TimeoutCache(Executor pool) {
		this.pool = pool;
		//timedQ = new PriorityQueue<TimeoutCache.TimedKey>(1024, new TimedCompare());
		timedQ = new TreeSet<TimeoutCache.TimedKey>(new TimedCompare());

		thread = new Thread(new RunTimer());
		thread.setName("TimeoutCache.time.runner");
		thread.setDaemon(true);
		thread.start();
	}
	
	TimeoutCache getSyncObject()
	{
		return this;
	}

	private class RunTimer implements Runnable {

		long delta = 0;// for debugging

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
				long time = System.currentTimeMillis() - delta;
				while (timedQ.size() != 0 && timedQ.first().expires <= time) {
					TimedKey head;
					synchronized( getSyncObject() ) {
						  head = timedQ.pollFirst();
					}
					pool.execute(new RunTimerList(head));
				}
			}
		}
	}

	private class RunTimerList implements Runnable {

		TimedKey key;

		public RunTimerList(TimedKey key) {
			this.key = key;
		}

		@Override
		public void run() {
			for (Runnable r : key.runners) {
				r.run();
			}
			synchronized( getSyncObject() ) {
				cache.invalidate(key);
			}
		}

	}

	private static class TimedCompare implements Comparator<TimedKey> {

		@Override
		public int compare(TimedKey o1, TimedKey o2) {
			long val = o1.expires - o2.expires;
			if (val != 0)
				return (int) (val >> 32);
			return o1.key.compareTo(o2.key);
		}

	}

	private static class ObjectHolder {
		TimedKey key;
		Object value;
		public ObjectHolder(TimedKey key, Object value) {
			super();
			this.key = key;
			this.value = value;
		}
	}

	/** The equeals and hashCode are for the key only. 
	 * 
	 * @author awootton
	 *
	 */
	private  static class TimedKey {
		String key;
		long expires;

		List<Runnable> runners = new ArrayList<Runnable>();

		// for the hashmap - only the string matters
		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TimedKey other = (TimedKey) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}

	}

	/**
	 * A Key Value memory store with expiration. Nothing lasts forever.
	 * 
	 * 
	 * @param key
	 * @param value
	 * @param ttl
	 *            - time to live in ms. 1000 means call the notifiers in 1 sec.
	 * @param notified
	 *            these get called when the ttl runs out
	 */
	public synchronized void put(String key, Object value, int ttl, Runnable... notified) {
		TimedKey tkey = new TimedKey();
		tkey.key = key;
		tkey.expires = System.currentTimeMillis() + ttl;
		tkey.runners = new ArrayList<Runnable>(notified.length);
		for (Runnable r : notified) {
			tkey.runners.add(r);
		}
		cache.put(key, new ObjectHolder(tkey,value));
		timedQ.add(tkey);
	}

	public synchronized Object get(String key) {
		ObjectHolder holder = cache.getIfPresent(key);
		if ( holder != null ){
			return holder.value;
		}else
			return null;
	}

	/**
	 * Reset the ttl. Aka refresh the object.
	 * IF it's not present then do nothing
	 * @param key
	 * @param ttl
	 */
	public synchronized void setTtl(String key, int ttl) {
		ObjectHolder holder = cache.getIfPresent(key);
		if ( holder != null ){
			timedQ.remove(holder.key);
			holder.key.expires = System.currentTimeMillis() + ttl;
			timedQ.add(holder.key);
		}else
			return ;
	}

	public static void main(String[] args) {
		
		

	}
}
