package org.messagewseb.chat;

import org.messageweb.ServerGlobalState;

/** make one of these and just send it to any server
 * 
 * @author awootton
 *
 */
public class CreateAgentMessage implements Runnable {
	
	String channel;// also id of object
	
	public void run() {
		// we're here.
		System.out.println("CreateAgentMessage CreateAgentMessage CreateAgentMessage on server " + ServerGlobalState.getGlobal().id);
		
		// check for channel existing. 
		
		
		// else, make it here!! 
		ServerGlobalState global = ServerGlobalState.getGlobal();
		
		
	}
 

}
