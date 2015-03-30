package org.messageweb.messages;

import org.messageweb.ServerGlobalState;

public class LogonMessage implements Runnable {
	
	public String user;
	public String applicationId;
	public String encrypted;
	
	
	
	@Override
	public void run() {
		ServerGlobalState global = ServerGlobalState.getGlobal();
		
	//	global.
		
	}
	
	

}
