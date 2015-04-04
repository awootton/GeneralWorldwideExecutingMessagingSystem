package org.gwems.util;

/**
 * 
 */

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;

/**
 * 
 */
public class RunnablesQueue {

	static Logger logger = Logger.getLogger(RunnablesQueue.class);

	protected LinkedList<Runnable> q;
	protected boolean isRunning;
	private Runnable myRunner;
	protected String myThread = "off";
	private Global global;

	public RunnablesQueue(Global global) {
		this.global = global;
		isRunning = false;
		q = new LinkedList<Runnable>();
		myRunner = getLocalRunner();
	}
	
	public Global getGlobal(){
		return global;
	}

	public void run(Runnable runnable) {
		synchronized (this) {
			q.add(runnable);
			if (!isRunning) {
				isRunning = true;
				global.execute(myRunner);// send myself to thread pool
			}
		}
	}

	protected Runnable getLocalRunner() {
		return new MyLocalRunner();
	}

	protected synchronized Runnable hasMore() {
		if (q.size() > 0)
			return q.removeFirst();
		else {
			isRunning = false;
			return null;
		}
	}

	protected class MyLocalRunner implements Runnable {
		@Override
		public void run() {
			myThread = Thread.currentThread().getName();
			Runnable r;
			while ((r = hasMore()) != null) {
				try {
					r.run();
				} catch (Exception e) {
					logger.error("badness", e);
				}
			}
			myThread = "off";
		}
	}

	@Override
	public String toString() {
		return myThread + " q=" + q.size() + (isRunning ? " running " : "");
	}

	public int size() {
		return q.size();
	}

}
