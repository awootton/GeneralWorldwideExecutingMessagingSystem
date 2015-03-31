package org.messagewseb.temperature;

import org.apache.log4j.Logger;
import org.messageweb.Global;
import org.messageweb.WsClientImpl;
import org.messageweb.agents.SimpleAgent;
import org.messageweb.messages.AgentEcho;

/**
 * Start up a subscription, send a quick ping that will collect the existence of an agent somewhere in the network, and
 * then return the answer. Clean up after.
 * 
 * This is a blocking operation and is not recommended for general use.
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

	int ttl = 100;// 100ms = how long to wait for reply from agent.

	public String localAgentId;//  our local, tmp, agent will need an id	
	public String listenHere; // we will listen to this.

	private Response response;

	private final Global global;

	public AgentFinder(Global global, String agentSubscribeChannel) {
		this.global = global;
		this.agentSubscribeChannel = agentSubscribeChannel;
		this.localAgentId = Global.getRandom();
		listenHere = localAgentId;
		
	}

	public class Response {
		boolean success = false;
		boolean failed = false;
		public String agentInfo = "none";// use Optional?
		public String globalInfo = "none";
	}

	// TODO: return a future.
	
	public Response goAndWait() {
		response = new Response();

		SimpleAgent localWatcher = new SimpleAgent(agentSubscribeChannel, global) {
			public void run(Runnable message) {
				if (message instanceof AgentEcho) {
					AgentEcho ae = (AgentEcho)message;
					response.agentInfo = ae.agentInfo;
					response.globalInfo = ae.globalInfo;
					response.success = true;
					if (logger.isTraceEnabled())
						logger.trace(" localWatcher called from " + Thread.currentThread());
				} else {
					logger.error("Have unexpected message " + message);
				}
			}
		};
		global.subscribe(localWatcher, listenHere);
		global.timeoutCache.put(localAgentId, localWatcher, ttl, () -> {
			response.success = false;
			response.failed = true;
			System.out.println(" timeoutCache called from " + Thread.currentThread());
		});
		
		AgentEcho echo = new AgentEcho(listenHere);
		global.publish(agentSubscribeChannel, echo);
		
		// Wait (TODO: use wait()) for response, or timeout.
		while (response.failed == false && response.success == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		// remove global1 agent
		global.timeoutCache.remove(localAgentId);
		global.unsubscribe(localWatcher, listenHere);
		
		return response;
	}

}
