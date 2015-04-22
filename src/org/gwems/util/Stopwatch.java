package org.gwems.util;

import java.util.concurrent.TimeUnit;

public class Stopwatch {

	long startNanos;

	private Stopwatch() {
		reset();
	}

	public static Stopwatch start() {
		return new Stopwatch();
	}

	public Stopwatch reset() {
		startNanos = System.nanoTime();
		return this;
	}

	public long time() {
		long ends = System.nanoTime();
		return ends - startNanos;
	}

	public long elapsed() {
		long ends = System.nanoTime();
		return ends - startNanos;
	}

	public interface CheckCondition {
		public boolean test();
	}

	/** Test the condition until until it becomes false or the time expires.
	 * 
	 * @param seconds
	 * @param check
	 * @return - true if everything was ok, false if the time expired.
	 */
	public static boolean tryAwhile(double seconds, CheckCondition check) {
		long maxTime = (long) (seconds * 10e9);
		Stopwatch watch = Stopwatch.start();
		while (check.test()) {
			if (watch.elapsed() >= maxTime)
				return false;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}

		}
		return true;// we're OK
	}

	public static boolean tryAwhile(int seconds, CheckCondition check) {
		return tryAwhile(seconds * 1.0, check);
	}
	
	// in approx milliseconds
	public long passed() {
		long ends = System.nanoTime();
		return (ends - startNanos) >> 10;
	}

	public long time(TimeUnit unit) {
		return unit.convert(time(), TimeUnit.NANOSECONDS);
	}

	public String toMinuteSeconds() {
		return String.format("%d min, %d sec", time(TimeUnit.MINUTES), time(TimeUnit.SECONDS) - time(TimeUnit.MINUTES));
	}


}