package org.gwems.agents;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;
import org.gwems.util.AgentRunnablesQueue;

/**
 * An Agent that will live on a server.
 * 
 * @author awootton
 *
 */
public class SimpleAgent extends Agent {

	public static Logger logger = Logger.getLogger(SimpleAgent.class);

	public Object object;// what is this for?? 

	public SimpleAgent(String subChannel, Global global) {
		super(global, subChannel);
		messageQ = new AgentRunnablesQueue(global, this);
	}

}
