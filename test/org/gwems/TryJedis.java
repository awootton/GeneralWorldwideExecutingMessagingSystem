package org.gwems;

import org.apache.log4j.Level;
import org.gwems.servers.impl.JedisRedisPubSubImpl;
import org.gwems.servers.impl.MyRedisPubSub;
import org.gwems.util.PubSub;

/** OMG kill me quick because my corpse is rotting.
 * 
 * @author awootton
 *
 */
public class TryJedis {

	boolean hit = false;
	String receivedMessage = "none";

	JedisRedisPubSubImpl ps;

	class Handle implements PubSub.Handler {
		@Override
		public void handle(String channel, String message) {
			System.out.println("have message on channel " + channel + " = " + message);
			hit = true;
			receivedMessage = message;
		}
	}

	static String aChannel = "dummyChannel#1";

//	@Test
//	public void t1() {
//		ps = new JedisRedisPubSubImpl("localhost", 6380, new Handle(), "noGlobal");
//
//		receivedMessage = "none";
//
//		ps.subcribe(aChannel);
//
//		ps.publish(aChannel, "Hello World from ATW");
//
//		System.out.println("sent");
//
//		while (!hit)
//			try {
//				Thread.sleep(1);// 1000 * 10);
//			} catch (InterruptedException e) {
//			}
//		//
//		Assert.assertTrue(hit);
//		Assert.assertEquals("Hello World from ATW", receivedMessage);
//
//		ps.stop();
//	}

	public static void main(String[] args) {

		MyRedisPubSub.logger.setLevel(Level.TRACE);
		JedisRedisPubSubImpl.logger.setLevel(Level.TRACE);

		TryJedis test = new TryJedis();

//		test.t1();

		System.out.println("(#(  (#(  (#(  (#(  (#(    main finished      main finished      main finished      main finished  ");

	}

}
