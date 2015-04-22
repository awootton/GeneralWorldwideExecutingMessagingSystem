package d;

import org.apache.log4j.Logger;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;

/**
 * A publish message from a GwemsPubSub client to a server (Global) that is acting as a pub sub system (instead of
 * handling public/client/sessionAgent connections).
 * 
 * @author awootton
 *
 */
public class Pub implements Runnable {

	public static Logger logger = Logger.getLogger(Pub.class);

	public String c = "na";
	public Runnable m;

	public Pub(String channel, Runnable msg) {
		super();
		this.c = channel;
		this.m = msg;
	}
	
	public Pub(){
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		if (logger.isTraceEnabled()) {
			logger.trace("global.publish " + Global.serialize4log(m));
		}
		ec.global.publish(c, m, true, ec.agent.get());
	}

}
