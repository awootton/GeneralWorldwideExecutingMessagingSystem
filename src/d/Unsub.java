package d;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

public class Unsub implements Runnable {

	public static Logger logger = Logger.getLogger(Unsub.class);

	String channel;

	public Unsub(String channel) {
		super();
		this.channel = channel;
	}

	public Unsub() {
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		SessionAgent agent = (SessionAgent) ec.agent.get();
		// is this just a normal sub for this agent even though this agent is
		// fronting permanently for a whole subtree?
		if (logger.isTraceEnabled()) {
			logger.trace("global.unsubscribe  agent=" + agent.getKey() + " channel=" + channel);
		}
		ec.global.unsubscribe(agent, channel);
		// seems too easy.
	}

}
