package org.gwems.util;

import org.apache.log4j.Level;
import org.gwems.servers.WsClientImpl;

public class StartOneClientCommandLine {

	
	public static void main(String[] args) {
		
		WsClientImpl.logger.setLevel(Level.TRACE);
		
		try {
			WsClientImpl.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
