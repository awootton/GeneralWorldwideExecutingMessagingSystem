package org.gwems.js;

import gwems.Js;

import org.gwems.agents.SimpleAgent;
import org.gwems.servers.Global;
import org.gwems.servers.WsClient;
import org.gwems.util.Stopwatch;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Figure out how to run the js timers on agents.
 * 
 * I'm not sure yet if these are unit tests because timers need, you know, time.
 * 
 * @author awootton
 *
 */
public class TimerSystemTestJsInterval {

	// var intervalVariable = window.setInterval( afunction, atime );
	// window.clearInterval(intervalVariable);

	// var timeoutVariable = window.setTimeout("javascript function", milliseconds);
	// window.clearTimeout(timeoutVariable)

	// We'll leave out the window part. \

	static Global global;
	static WsClient client;

	@BeforeClass
	public static void setup() {

		int port = 0;// 8081
		global = new Global(port, null);// starts a ws server wo/ws

		// Stopwatch.tryAwhile(1.1, () -> true);
		// client = new WsClientImpl("localhost", 8088);// start a client
	}

	@AfterClass
	public static void closeAll() {
		// how do I do this??
		global.stop();
		if (client != null)
			client.stop();
	}

	@Test
	public void t1() {
		// make an agent
		SimpleAgent agent = new SimpleAgent(global, "ADemoChannel");
		// Don't forget that he subscribes to hims
		global.timeoutCache.put("ADemoChannel", agent, 100 * 1000, () -> System.out.println("timed out!"));

		Js mmm = new Js();
		String script = "";
		script += "var aVar = 1; \n";
		script += "var aFunct = function() { \n";
		script += "    console.log(' we are in js timer callback' ); \n";
		script += "    aVar = 2; \n";
		script += "};\n";
		script += " console.log('defined aFunct');\n";
		script += "var timeoutVariable = window.setTimeout(aFunct, 1);\n";
		script += " console.log('set timer ' + timeoutVariable);\n";

		mmm.js = script;
		agent.messageQ.run(mmm);

		boolean ok = Stopwatch.tryAwhile(0.1, () -> agent.bindings == null);
		Assert.assertTrue(ok);
		ok = Stopwatch.tryAwhile(0.1, () -> !(agent.bindings.get("aVar") + "").equals("1"));
		Assert.assertTrue(ok);
		// now it's 1
		// wait for timer - 1 ms
		ok = Stopwatch.tryAwhile(0.1, () -> !(agent.bindings.get("aVar") + "").equals("2"));
		Assert.assertTrue(ok);

	}

	// Without the 'window.'
	@Test
	public void t11() {
		// make an agent
		SimpleAgent agent = new SimpleAgent(global, "ADemoChannel");
		// Don't forget that he subscribes to hims
		global.timeoutCache.put("ADemoChannel", agent, 100 * 1000, () -> System.out.println("timed out!"));

		Js mmm = new Js();
		String script = "";
		script += "var aVar = 1; \n";
		script += "console.log('setTimeout =' + setTimeout); \n";
		script += "var aFunct = function() { \n";
		script += "    console.log(' we are in js timer callback' ); \n";
		script += "    aVar = 2; \n";
		script += "};\n";
		script += " console.log('defined aFunct');\n";
		script += "var timeoutVariable =  setTimeout(aFunct, 1);\n";
		script += " console.log('set timer ' + timeoutVariable);\n";

		mmm.js = script;
		agent.messageQ.run(mmm);

		boolean ok = Stopwatch.tryAwhile(0.1, () -> agent.bindings == null);
		Assert.assertTrue(ok);
		ok = Stopwatch.tryAwhile(0.1, () -> !(agent.bindings.get("aVar") + "").equals("1"));
		Assert.assertTrue(ok);
		// now it's 1
		// wait for timer - 1 ms
		ok = Stopwatch.tryAwhile(0.1, () -> !(agent.bindings.get("aVar") + "").equals("2"));
		Assert.assertTrue(ok);

	}

	/**
	 * A longer timer and we cancel it before it it's done
	 *
	 */
	@Test
	public void t2() {
		// make an agent
		SimpleAgent agent = new SimpleAgent(global, "ADemoChannel2");
		// Don't forget that he subscribes to hims
		global.timeoutCache.put("ADemoChannel2", agent, 100 * 1000, () -> System.out.println("timed out!"));

		Js mmm = new Js();
		String script = "";
		script += "var aVar = 1; \n";
		script += "var aFunct = function() { \n";
		script += "    console.log(' we are in js timer callback' ); \n";
		script += "    aVar = 2; \n";
		script += "};\n";
		script += " console.log('defined aFunct');\n";
		script += "var timeoutVariable = window.setTimeout(aFunct, 1);\n";
		script += "console.log('timeout var is ' + timeoutVariable);\n";
		script += "var afterSetTimeout = 1;\n";

		mmm.js = script;
		agent.messageQ.run(mmm);

		boolean ok = Stopwatch.tryAwhile(0.1, () -> agent.bindings == null);
		Assert.assertTrue(ok);
		ok = Stopwatch.tryAwhile(0.1, () -> {
			//System.out.println(agent.bindings.get("afterSetTimeout"));
			return !("" + agent.bindings.get("afterSetTimeout")).equals("1");
		});
		Assert.assertTrue(ok);

		System.out.println(" after script " + agent.bindings.get("afterSetTimeout"));

		Object timerStr = agent.bindings.get("timeoutVariable");

		mmm = new Js();
		script = "";
		script += "var aVar = '" + timerStr + "'; \n";
		script += "window.clearTimeout(aVar);\n";
		script += "var afterSetTimeout = 222;\n";

		System.out.println("script is " + script);
		System.out.println("aVar starts as " + agent.bindings.get("aVar"));

		mmm.js = script;
		agent.messageQ.run(mmm);

		ok = Stopwatch.tryAwhile(0.1, () -> !(agent.bindings.get("afterSetTimeout") + "").equals("222"));
		Assert.assertTrue(ok);// the script executed in under 100 ms
		// wait for 100 ms and be sure the timer didn't run.
		ok = Stopwatch.tryAwhile(0.1, () -> {
			// System.out.println(agent.bindings.get("aVar"));
				return agent.bindings.get("aVar") + "" != "2";
			});
		// it's NOT supposed to change into a 2 because the timer never fires.
		Assert.assertTrue(ok == false);

	}

	public static void main(String[] args) {

		setup();

		TimerSystemTestJsInterval test = new TimerSystemTestJsInterval();

		test.t2();

		closeAll();

	}
}
