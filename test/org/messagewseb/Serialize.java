package org.messagewseb;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.messageweb.ServerGlobalState;
import org.messageweb.messages.PingEcho;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Serialize {

	private static Logger logger = Logger.getLogger(Serialize.class);

	@Test
	public void t1() throws IOException {
		
		ObjectNode tmp = ServerGlobalState.serialize2node(new PingEcho());
		System.out.println(tmp);

		
		PingEcho p = new PingEcho();
		p.setKey("something11");
		String s = ServerGlobalState.serialize(p);
		System.out.println(s);

		Object o = ServerGlobalState.deserialize(s);

		// o and p must match.
		Assert.assertEquals(o, p);

		String s2 = ServerGlobalState.serialize((Runnable) o);

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
