package org.gwems.agents;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;
import org.gwems.util.AgentRunnablesQueue;
import org.gwems.util.SessionRunnablesQueue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * There's some confusion about what this does. We're going to need for the clients to subscribe to things but when they
 * die, or go away, then the subs have to unsub. So, that would be something the SessionAgent does.
 * 
 * See Global.executeChannelMessage. It sets a socket close on timeout. It sends the messages to socketMessageQ.
 * 
 * All the messages that the client sends pass through here. And, all the messages that are received for the client also
 * pass through here.
 * 
 * Let's not be multithreaded - block the two incoming q's against each other.
 * 
 * Do we really want to do it this way?
 * 
 * @author awootton
 *
 */
public class SessionAgent extends Agent {

	public static Logger logger = Logger.getLogger(SessionAgent.class);

	boolean validated = false;// TODO: implement.

	String userName = "none";// TODO: implement
	String credentials = "none";// TODO: implement
	// MyUser user = ?? TODO: what is user allowed to do ?
	public String ipAddress = null;// set by Global

	long next10sec = System.currentTimeMillis() + 10 * 1000;
	long startTime =  new Date().getTime();//System.currentTimeMillis();
	
	int messageCount = 0;
	public AtomicLong byteCount = new AtomicLong();

	// I even know that we would ever serialize this.
	@DynamoDBIgnore
	@JsonIgnore
	public final SessionRunnablesQueue socketMessageQ;

	private ChannelHandlerContext ctx;
	
	/**
	 * The session id and also the key for the timeoutQ. Constructed by Global
	 * 
	 * @param global
	 * @param sessionKey
	 */
	public SessionAgent(Global global, String sessionKey, ChannelHandlerContext ctx) {
		super(global,sessionKey);
		this.ctx = ctx;
		//this.global = global;
		messageQ = new AgentRunnablesQueue(global, this);
		socketMessageQ = new SessionRunnablesQueue(global, this);
		logger.info("started " + getKey());
	}

	public void writeAndFlush(String message) {
		ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
	}

	public void writeAndFlush(Runnable message) {
		String str;
		try {
			str = Global.serialize(message);
			if ( logger.isTraceEnabled()){
				logger.trace("Session sending " + message + " from " + getKey());
			}
			ctx.channel().writeAndFlush(new TextWebSocketFrame(str));
		} catch (JsonProcessingException e) {
			logger.error("bad message " + message, e);
		}
	}

	/**
	 * Set ourselves so that we keep alive every 10 sec. In some cases there will be a stream as fast as 60 hz and that
	 * would stress the timeoutCache.
	 * 
	 * Then, run the message.
	 * 
	 * @param message
	 */
	public void runSocketMessage(Runnable message) {
		synchronized (this) {
			long current = System.currentTimeMillis();
			if (current > next10sec) {
				next10sec = current + 10 * 1000;
				if (ctx.channel() != null && ctx.channel().isOpen()) {
					global.timeoutCache.setTtl(this.getKey(), global.sessionTtl);
				} else {
					// is closed? This should have been handled somewhere else.
					logger.error("Attempted ttl on closed socket " + this.toString());
				}
			}
			messageCount++;
			if ( logger.isTraceEnabled()){
				logger.trace("socketQ executing " + Global.serialize4log(message) + " from " + getKey());
			}
			if (validated) {
				message.run();
			} else {
				message.run();
			}
		}
	}

	/**
	 * The messages coming from the subscriptions
	 * TODO: write special Agent for isPubSub sessions. 
	 */
	@Override
	public void run(Runnable message) {

		if ( logger.isTraceEnabled()){
			logger.trace("messageQ executing " + Global.serialize4log(message) + " from " + getKey());
		}
		synchronized (this) {
			// if they are supposed to go down then they will be wrapped with a Push2Client
			// don't just blindly forward them because then there's no option to run them here.
			if ( global.isPubSub ){
				// unless we never want to run them here
				writeAndFlush(message);
				
			}else {
				message.run();
			}
		}
	}
	
	public long getStartTime() {
		return startTime;
	}

}
