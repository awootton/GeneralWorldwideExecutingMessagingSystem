package d;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/** A message from a GwemsPubSub client to a server (Global) that is acting as a pub sub 
 * system (instead of handling public/client/sessionAgent connections).
 * 
 * @author awootton
 *
 */
public class Sub implements Runnable {
	
	public static Logger logger = Logger.getLogger(Sub.class);

	String c; 
	
	public Sub(String channel) {
		super();
		this.c = channel;
	}
	
	public Sub(){
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		SessionAgent agent = (SessionAgent)ec.agent.get();
		// is this just a normal sub for this agent even though this agent is
		// fronting permanently for a whole subtree?
		if (logger.isTraceEnabled()) {
			logger.trace("global.subscribe  agent=" + agent.getKey() + " channel=" + c);
		}
		ec.global.subscribe(agent, c);
		// seems too easy. 
	}

}
