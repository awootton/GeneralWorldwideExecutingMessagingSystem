package org.messageweb.messages;

import org.apache.log4j.Logger;
import org.messageweb.ExecutionContext;
import org.messageweb.ServerGlobalState;
import org.messageweb.agents.Agent;

public class AgentEcho implements Runnable {

	public static Logger logger = Logger.getLogger(AgentEcho.class);

	public String bringBack = "";

	@Override
	public void run() {

		// things we know when arriving at the end of publish
		// and making our way through an Agent messageQ
		ExecutionContext ec = ServerGlobalState.getContext();
		ServerGlobalState global = ec.global;
		Agent agent = ec.agent.get();
		String incoming = ec.subscribedChannel.get();
		String agentPubChannel = agent.pub;// aka outgoing

		System.out.println("does " + incoming + " == " + agent.sub + " ?  with agent=" + agent);

		bringBack = "" + agent;

		// publish it again on a different channel
		global.publish(agentPubChannel, this);

	}

}
