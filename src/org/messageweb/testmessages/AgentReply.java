package org.messageweb.testmessages;

import org.apache.log4j.Logger;
import org.gwems.AgentFinder;
import org.gwems.agents.SimpleAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/** Used in test. Not for production. antique - delete me
 * 
 * What's goofy is that the reply is as a SASE and so the agent running it
 * is the same one being looked up in the timeoutCache. 
 * 
 * @author awootton
 *
 */
public class AgentReply implements Runnable {

	public static Logger logger = Logger.getLogger(AgentReply.class);

	public String agentInfo = "";
	public String globalInfo = "";

	public String replyChannel;

	public AgentReply(String replyChannel) {
		this.replyChannel = replyChannel;
	}

	public AgentReply() {
	}

	@Override
	public void run() {

		// things we know when arriving at the end of publish
		// and making our way through an Agent messageQ
		ExecutionContext ec = Global.getContext();
		Global global = ec.global;

		Object obj = global.timeoutCache.get(replyChannel);
		if (obj != null) {
			// we've arrived home
			SimpleAgent simpleAgent = (SimpleAgent) obj;
			AgentFinder finder = (AgentFinder) simpleAgent.object;
			AgentFinder.Response response = finder.getResponse();
			response.agentInfo = agentInfo;
			response.globalInfo = globalInfo;
			response.success = true;
			if (logger.isTraceEnabled())
				logger.trace(" localWatcher called from " + Thread.currentThread());

		} else {
			logger.error("expected to find home object at " + replyChannel);
		}

	}

}
