package org.gwems.agents;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.gwems.servers.ExecutionContext;
import org.gwems.servers.Global;
import org.gwems.servers.Util;
import org.gwems.util.AgentRunnablesQueue;
import org.gwems.util.SessionRunnablesQueue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
	public String ipAddress = "none";// set by Global

	long next10sec = System.currentTimeMillis() + 10 * 1000;
	long startTime = System.currentTimeMillis();
	int messageCount = 0;
	public AtomicLong byteCount = new AtomicLong();

	// I even know that we would ever serialize this.
	@DynamoDBIgnore
	@JsonIgnore
	public final SessionRunnablesQueue socketMessageQ;

	private ChannelHandlerContext ctx;

	public final Global global;

	/**
	 * The session id and also the key for the timeoutQ. Constructed by Global
	 * 
	 * @param global
	 * @param sessionKey
	 */
	public SessionAgent(Global global, String sessionKey, ChannelHandlerContext ctx) {
		super(sessionKey);
		this.ctx = ctx;
		this.global = global;
		messageQ = new AgentRunnablesQueue(global, this);
		socketMessageQ = new SessionRunnablesQueue(global, this);
	}

	public void writeAndFlush(String message) {
		ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
	}


	/**
	 * Set ourselves so that we keep alive every 10 sec. In some cases there will be a stream as fast as 60 hz and
	 * that would stress the timeoutCache.
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
				global.timeoutCache.setTtl(this.getKey(), Util.twoMinutes);
			}
			messageCount++;
			if (validated) {
				message.run();
			} else {
				message.run();
			}
		}
	}

	/**
	 * The messages coming from the subscriptions
	 * 
	 */
	@Override
	public void run(Runnable message) {

		synchronized (this) {
			// if they are supposed to go down then they will be wrapped with a Push2Client
			// don't just blindly forward them because then there's no option to run them here.
			message.run();
		}
	}
}
