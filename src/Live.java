import org.apache.log4j.Logger;
import org.gwems.servers.Global;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The smallest possible message. Used to keep sockets, and their agents, alive.
 * 
 * Send this string: {"@":"Live"}
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
			logger.trace("have Live at session " + Global.getContext().agent);
		}
	}

	public static void main(String[] args) throws JsonProcessingException {
		System.out.println(Global.serialize(new Live()));
	}

}
