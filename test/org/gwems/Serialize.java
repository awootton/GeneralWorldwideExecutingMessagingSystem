package org.gwems;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.testmessages.PingEcho;

public class Serialize {

	private static Logger logger = Logger.getLogger(Serialize.class);

	@Test
	public void t1() throws IOException {
		
		System.out.println(Global.getRandom());
		System.out.println(Global.getRandom());
		System.out.println(Global.getRandom());
		
//		ObjectNode tmp = Global.serialize2node(new PingEcho());
//		System.out.println("node = " + tmp);

		
		PingEcho p = new PingEcho();
		p.setKey("something11");
		String s = Global.serialize(p);
		System.out.println(s);

		Object o = Global.deserialize(s);

		// o and p must match.
		Assert.assertEquals(o, p);

		String s2 = Global.serialize((Runnable) o);

		// s and s2 must match
		Assert.assertEquals(s, s2);

		System.out.println("passed");
		logger.info("passed " + s2);
	}

	public static void main(String[] args) throws IOException {

		Serialize test = new Serialize();
		test.t1();
	}
}
