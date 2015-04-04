package org.gwems.util;

import org.apache.log4j.Logger;

public abstract class PubSub {

	public static Logger logger = Logger.getLogger(PubSub.class);

	public abstract void subcribe(String... channels);

	public abstract void publish(String channel, String message);

	public abstract void unsubcribe(String... channels);

	public static interface Handler {
		public void handle(String channel, String message);
	}

	/**
	 * One should really only need to call this once ever.
	 */
	public abstract void setHandler(Handler handler);

	public abstract void stop();

}
