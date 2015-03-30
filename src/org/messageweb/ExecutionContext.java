package org.messageweb;

import io.netty.channel.ChannelHandlerContext;

/** This will be available as ThreadLocal storage.
 *  
 * These could be Optional. Note that null means 'absent', or na. 
 * @author awootton
 *
 */
public class ExecutionContext {

	public ServerGlobalState global = null;
	
	public ChannelHandlerContext ctx = null;
	
	public String subscribedChannel = "none";
	
	public Agent agent = null;
	
	public String timedObject = null;
	
}
