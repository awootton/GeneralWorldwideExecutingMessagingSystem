package org.gwems.servers;

import io.netty.channel.ChannelHandlerContext;

import org.gwems.agents.Agent;

public abstract class WsClient extends Agent {

	public WsClient(Global global, String key) {
		super(global, key);
	}

	/** Send a message to the server.
	 * 
	 * @param message
	 */
	public abstract void enqueueRunnable(Runnable message); 
	
	/** A handler for what comes back from the server.
	 * 
	 * @param ctx
	 * @param message
	 */
	public abstract void executeChannelMessage(ChannelHandlerContext ctx, String message);
	
	public abstract void stop();

}
