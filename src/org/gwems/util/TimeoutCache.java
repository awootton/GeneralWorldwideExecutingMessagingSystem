package org.gwems.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * This is a basic key value cache with the addition of a time-to-live for every key/value. There is an ordered
 * collection of runnables that will execute if the key/value expire. Also, the ttl can be extended and that is expected
 * to be a major use case so that needs to be efficient.
 * 
 * FIXME: this will be a bottleneck at high volume.
 * 
 * @author awootton
 *
 */
public class TimeoutCache {

	public static Logger logger = Logger.getLogger(TimeoutCache.class);

	// use a map and also a priority Q of keys and also a thread.

	Cache<String, ObjectHolder> cache = CacheBuilder.newBuilder().concurrencyLevel(8).build();

	TreeSet<TimedKey> timedQ;

	Thread watcher;
	Executor pool;
	Thread thread;
	boolean running = true;

	public TimeoutCache(Executor pool, String name) {
		this.pool = pool;
		timedQ = new TreeSet<TimeoutCache.TimedKey>((TimedKey o1, TimedKey o2) -> {
			long val = o1.expires - o2.expires;
			if (val > 0) {
				return 1;
			} else if (val < 0) {
				return -1;
			}
			return o1.key.compareTo(o2.key);
		});

		String timerThreadName = "TimeoutCache.time.runner " + name;

		thread = new Thread(new RunTimer());
		thread.setName(timerThreadName);
		thread.setDaemon(true);
		thread.start();
		logger.debug("Started " + timerThreadName);

	}

	public void stop() {
		running = false;
	}

	TimeoutCache XXXgetSyncObject() {
		return this;
	}

	private class RunTimer implements Runnable {
		long delta = 0;// for debugging

		@Override
		public void run() {
			logger.debug(" RunTimer Started " + Thread.currentThread().getName());

			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				if (!running) {
					return;
				}
				long time = System.currentTimeMillis() - delta;
				while (timedQ.size() != 0 && timedQ.first().expires <= time) {
					TimedKey head;
					synchronized (TimeoutCache.this) {
						head = timedQ.pollFirst();
						if (logger.isTraceEnabled()) {
							logger.trace("popped = " + head.key + " " + timedQ.size() + " remaining");
						}

					}
					if (logger.isTraceEnabled()) {
						logger.trace("sending key to pool = " + head.key);
					}
					// pool.execute(new RunTimerList(head));
					pool.execute(() -> {
						if (logger.isTraceEnabled()) {
							logger.trace("timing out key = " + head.key + " calling " + head.runners.size() + " watchers");
						}
						List<Runnable> runList;
						synchronized (TimeoutCache.this) {
							runList = new ArrayList<Runnable>(head.runners);
						}
						for (Runnable r : runList) {
							r.run();
						}
						// only invalidate it if it's still timed out.
						// the runners might have renewed it.
						synchronized (TimeoutCache.this) {
							if (head.expires <= time)
								cache.invalidate(head);
						}
					});
				}
			}
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

	/**
	 * The equeals and hashCode are for the key only.
	 * 
	 * @author awootton
	 *
	 */
	private static class TimedKey {
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
	 *            these get called if the ttl runs out
	 */
	public synchronized void put(String key, Object value, int ttl, Runnable... notified) {
		// check for existing !
		ObjectHolder holder = cache.getIfPresent(key);
		TimedKey tkey;
		if (holder != null) {
			tkey = holder.key;
			timedQ.remove(tkey);
		} else {
			tkey = new TimedKey();
		}
		tkey.key = key;
		tkey.expires = System.currentTimeMillis() + ttl;
		tkey.runners = new ArrayList<Runnable>(notified.length);
		for (Runnable r : notified) {
			tkey.runners.add(r);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Adding (put) key = " + key + " val = " + value + " for " + ttl + " ms " + notified.length + " watchers");
		}
		cache.put(key, new ObjectHolder(tkey, value));
		timedQ.add(tkey);
	}

	public synchronized Object get(String key) {
		ObjectHolder holder = cache.getIfPresent(key);
		if (holder != null) {
			return holder.value;
		} else
			return null;
	}

	/**
	 * Reset the ttl. Aka refresh the object. If it's not present then do nothing
	 * 
	 * @param key
	 * @param ttl
	 */
	public synchronized void setTtl(String key, int ttl) {
		ObjectHolder holder = cache.getIfPresent(key);
		if (holder != null) {
			timedQ.remove(holder.key);
			holder.key.expires = System.currentTimeMillis() + ttl;
			timedQ.add(holder.key);

			if (logger.isTraceEnabled()) {
				logger.trace("Reset key = " + key + " for " + ttl + " ms ");
			}

		}
	}

	/**
	 * We don't really remove it. We just set the timeout to -1 and let it expire without the watchers. No expire
	 * routines are run.
	 * 
	 * @param key
	 */
	public synchronized void remove(String key) {
		ObjectHolder holder = cache.getIfPresent(key);
		if (holder != null) {
			timedQ.remove(holder.key);
			holder.key.runners.clear();
			holder.key.expires = System.currentTimeMillis() - 1;
			timedQ.add(holder.key);

			if (logger.isTraceEnabled()) {
				logger.trace("Removed key = " + key);
			}
		}
	}

}
