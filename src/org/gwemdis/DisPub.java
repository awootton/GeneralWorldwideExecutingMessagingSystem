package org.gwemdis;

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
public class DisPub implements Runnable {

	public static Logger logger = Logger.getLogger(DisPub.class);

	public String channel = "na";
	public String msg;

	public DisPub(String channel, String msg) {
		super();
		this.channel = channel;
		this.msg = msg;
	}
	
	public DisPub(){
	}

	@Override
	public void run() {
		ExecutionContext ec = Global.getContext();
		Runnable wrapper = new DisPub(channel, msg);
		ec.global.publish(channel, wrapper, true, ec.agent.get());
	}

}
