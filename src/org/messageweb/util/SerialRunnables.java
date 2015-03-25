package org.messageweb.util;

/**
 * 
 */

import java.util.LinkedList;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

/**
 * 
 */
public class SerialRunnables {

	static Logger logger = Logger.getLogger(SerialRunnables.class);

	protected LinkedList<Runnable> q;
	protected boolean isRunning;
	private Runnable myRunner;
	protected String myThread = "off";
	private Executor executor;

	public SerialRunnables(Executor executor) {
		this.executor = executor;
		isRunning = false;
		q = new LinkedList<Runnable>();
		myRunner = new MyLocalRunner();
	}

	public void run(Runnable runnable) {

		synchronized (this) {
			q.add(runnable);
			if (!isRunning) {
				isRunning = true;
				executor.execute(myRunner);// send myself to thread pool
			}
		}
	}

	protected synchronized Runnable hasMore() {
		if (q.size() > 0)
			return q.removeFirst();
		else {
			isRunning = false;
			return null;
		}
	}

	private class MyLocalRunner implements Runnable {
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
