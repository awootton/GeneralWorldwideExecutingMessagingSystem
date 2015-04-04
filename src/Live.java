import org.apache.log4j.Logger;
import org.messageweb.Global;
import org.messageweb.util.StartOneGlobalServer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The smallest possible message. Used to keep sockets, and their agents, alive.
 * 
 * Send this string: {"@C":"Live"}
 * 
 * @author awootton
 * 
 * Copyright 2015 Alan Wootton see included license.
 *
 */
public class Live implements Runnable {
	
	public static Logger logger = Logger.getLogger(Live.class);

	@Override
	public void run() {
		if ( logger.isTraceEnabled()){
			logger.trace("have alive from session " + Global.getContext().agent);
		}
	}

	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Live()));
	}

}
