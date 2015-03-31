package org.messageweb.agents;

import org.apache.log4j.Logger;
import org.messageweb.Global;
import org.messageweb.util.AgentRunnablesQueue;

/** An Agent that will live on a server.
 * 
 * @author awootton
 *
 */
public class SimpleAgent extends Agent {

	public static Logger logger = Logger.getLogger(SimpleAgent.class);

	public SimpleAgent(String subChannel, Global global) {
		super(subChannel);
		messageQ = new AgentRunnablesQueue(global, this);
	}

}
