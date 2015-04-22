package org.gwems;

import org.apache.log4j.Logger;
import org.gwems.agents.SimpleAgent;
import org.gwems.servers.Global;

/**
 * Start up a subscription, send a quick ping that will collect the existence of an agent somewhere in the network, and
 * then return the answer. Clean up after.
 * 
 * This is a blocking operation and is not recommended for general use.
 * 
 * Don't use this in prod. It's antique. 
 * 
 * @author awootton
 *
 */
public class AgentFinder {

	public static Logger logger = Logger.getLogger(AgentFinder.class);

	/**
	 * This only works because the agent is subscribing on some channel.
	 * 
	 */
	String agentSubscribeChannel;

	/// this is also how long we wait for a fail.
	int ttl = 100;// 100ms = how long to wait for reply from agent.

	public String localAgentRandomId;//  our local, tmp, agent will need an id	
	//public String listenHere; // we will listen to this.

	private Response response;

	private final Global global;

	public AgentFinder(Global global, String agentSubscribeChannel) {
		this.global = global;
		this.agentSubscribeChannel = agentSubscribeChannel;
		this.localAgentRandomId = Global.getRandom();
	}

	public class Response {
		public boolean success = false;
		public boolean failed = false;
		public String agentInfo = "none";// use Optional?
		public String globalInfo = "none";
	}
	
	public Response getResponse(){
		return response;
	}

	// TODO: return a future.
	/** There's supposed to be something at agentSubscribeChannel
	 * 
	 * @return
	 */
	public Response goAndWait() {
		response = new Response();

		SimpleAgent localWatcher = new SimpleAgent(global, localAgentRandomId);
		localWatcher.object = this;
		
		global.subscribe(localWatcher, localAgentRandomId);
	
		global.timeoutCache.put(localAgentRandomId, localWatcher, ttl, () -> {
			response.success = false;
			response.failed = true;
			System.out.println(" timeoutCache called from " + Thread.currentThread());
		});
		
		AgentEcho echo = new AgentEcho(localAgentRandomId);
		global.publish(agentSubscribeChannel, echo);
		
		// Wait (TODO: use wait()) for response, or timeout.
		while (response.failed == false && response.success == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		// remove global1 agent
		global.timeoutCache.remove(localAgentRandomId);
		global.unsubscribe(localWatcher, localAgentRandomId);
		
		return response;
	}

}
