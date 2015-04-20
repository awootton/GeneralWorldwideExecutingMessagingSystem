package org.gwemdis;

import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

public class DisUnsub  implements Runnable {
	
	String channel; 
	
	public DisUnsub(String channel) {
		super();
		this.channel = channel;
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
