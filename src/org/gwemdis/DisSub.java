package org.gwemdis;

import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/** A message from a GwemsPubSub client to a server (Global) that is acting as a pub sub 
 * system (instead of handling public/client/sessionAgent connections).
 * 
 * @author awootton
 *
 */
public class DisSub implements Runnable {
	
	String channel; 
	
	public DisSub(String channel) {
		super();
		this.channel = channel;
	}
	
	public DisSub(){
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		SessionAgent agent = (SessionAgent)ec.agent.get();
		// is this just a normal sub for this agent even though this agent is
		// fronting permanently for a whole subtree?
		ec.global.subscribe(agent, channel);
		// seems too easy. 
	}

}
