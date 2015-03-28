package org.messagewseb;

import org.apache.log4j.Level;
import org.messageweb.impl.JedisRedisPubSubImpl;
import org.messageweb.impl.MyRedisPubSub;
import org.messageweb.util.PubSub;

public class TryJedis {

	boolean hit = false;

	JedisRedisPubSubImpl ps;

	class Handle implements PubSub.Handler {

		@Override
		public void handle(String channel, String message) {
			System.out.println("have message on channel " + channel + " = " + message);
			hit = true;
		}
	}

	static String aChannel = "dummyChannel#1";

	public void t1() {
		ps = new JedisRedisPubSubImpl("localhost", new Handle());

		ps.subcribe(aChannel);

		ps.publish(aChannel, "Hello World from ATW");

		System.out.println("sent");

		while ( ! hit )
		try {
			Thread.sleep(1);//1000 * 10);
		} catch (InterruptedException e) {
		}

	}

	public static void main(String[] args) {

		MyRedisPubSub.logger.setLevel(Level.TRACE);
		JedisRedisPubSubImpl.logger.setLevel(Level.TRACE);

		TryJedis test = new TryJedis();

		test.t1();

		test.ps.stop();
		System.out.println("(#(  (#(  (#(  (#(  (#(    main finished      main finished      main finished      main finished  ");

	}

}
