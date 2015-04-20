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
	
	// When this channel is unsubscribed then the client quits and returns from the thread - need to change that.
	// We leak this channel permanently, use it for keep alives. 
	public static final String dummyChannel = "AHUAp4xu9FqRobj8zwn2vBI6Anag1t8Z5z6SWjn8_neverUseThisChannel";// random.org

}
