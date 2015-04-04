package org.messageweb.testmessages;

import org.apache.log4j.Logger;
import org.gwems.agents.Agent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/** Used in a demo in the test folder
 * 
 * @author awootton
 *
 */
public class AgentEcho implements Runnable {

	public static Logger logger = Logger.getLogger(AgentEcho.class);

	// Is also the random id of caller agent. See AgentFinder.
	public String replyChannel;

	public AgentEcho(String replyChannel) {
		this.replyChannel = replyChannel;
	}

	public AgentEcho() {
	}

	@Override
	public void run() {

		AgentReply reply = new AgentReply(replyChannel);

		// things we know when arriving at the end of publish
		// and making our way through an Agent messageQ
		ExecutionContext ec = Global.getContext();
		Global global = ec.global;

		Agent agent = ec.agent.get();
		String incoming = ec.subscribedChannel.get();

		if (logger.isTraceEnabled())
			logger.trace("found agent. does " + incoming + " == " + agent.key + " ?  with agent=" + agent);

		reply.agentInfo = "" + agent;
		reply.globalInfo = global.id;

		if (logger.isTraceEnabled())
			logger.trace("Sending reply on channel " + replyChannel);
		// publish it again, reply, on a different channel
		global.publish(replyChannel, reply);

	}

}
