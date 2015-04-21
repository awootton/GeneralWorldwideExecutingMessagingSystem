package d;

import org.apache.log4j.Logger;
import org.gwems.agents.SessionAgent;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/** Push 2 client
 * 
 * @author awootton
 *
 */
public class P2C implements Runnable {

	public static Logger logger = Logger.getLogger(P2C.class);

	Object msg = "none";

	/** Object will need to be serializable by jackson.
	 * 
	 * @param message
	 */
	public P2C(Object message) {
		super();
		this.msg = message;
	}

	public P2C() {// for jackson
	}

	@Override
	public void run() {
		// meant to run in a session agent.
		ExecutionContext ec = Global.getContext();
		if (ec.agent.isPresent() && ec.agent.get() instanceof SessionAgent) {
			SessionAgent session = (SessionAgent) ec.agent.get();
			if (logger.isTraceEnabled()) {
				logger.trace("Sending message2client " + msg + " to " + session);
			}
			try {
				session.writeAndFlush(Global.serialize(msg));
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
		} else {
			// what?
			logger.debug("non session message? " + msg + " agent = " + ec.agent);
		}
	}

	 

}
