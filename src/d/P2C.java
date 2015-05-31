package d;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A publish response flowing down the pubsub tree.
 * Not the same as Push2Client?
 * 
 * @author awootton
 *
 */
public class P2C implements Runnable {

	public static Logger logger = Logger.getLogger(P2C.class);

	public String c = "none";
	public String m = "none";

	/**
	 * Object will need to be serializable by jackson.
	 * 
	 * @param message
	 */
	public P2C(String message, String channel) {
		super();
		this.m = message;
		this.c = channel;
	}

	public P2C() {// for jackson
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if (logger.isTraceEnabled()) {
				logger.trace("writeAndFlush " + m + " session=" + session);
			}
			try {
				session.writeAndFlush(Global.serialize(m));
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
		} else {
			// what?
			logger.debug("non session message? " + m + " session= " + ec.agent);
		}
	}

}
