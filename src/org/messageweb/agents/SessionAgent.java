package org.messageweb.agents;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.messageweb.ExecutionContext;
import org.messageweb.Global;
import org.messageweb.Util;
import org.messageweb.util.AgentRunnablesQueue;
import org.messageweb.util.SessionRunnablesQueue;

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
 * Let's not be multithreaded - don't block the two incoming q's against each other.
 * 
 * Do we really want to do it this way?
 * 
 * 1) If this is the first message then we need to know a handler type to replace it with or we need to know a channel
 * to publish to. And we need to validate creds, maybe, depends on type I guess.
 * 
 * 2) Subscriptions pass through iff validated.
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
	 * The subscribe channel (which is never used here because it doesn't make sense to publish from a session) is also
	 * used as the session id and also the key for the timeoutQ. Constructed by Global
	 * 
	 * @param global
	 * @param sub
	 */
	public SessionAgent(Global global, String sub, ChannelHandlerContext ctx) {
		super(sub);
		this.ctx = ctx;
		this.global = global;
		messageQ = new AgentRunnablesQueue(global, this);
		socketMessageQ = new SessionRunnablesQueue(global, this);
	}

	public void writeAndFlush(String message) {
		ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
	}

	@Override
	public String toString() {
		return "SessionAgent:" + key;
	}

	/**
	 * Set ourselves so that we keep alive every 10 sec. In some cases there will be a stream as fast as 60 per sec and
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
				global.timeoutCache.setTtl(this.key, Util.twoMinutes);
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

			ExecutionContext ec = Global.getContext();
			ec.ctx = Optional.of(ctx);
			message.run();
			ec.ctx = Optional.empty();

			// ExecutionContext ec = Global.getContext();
			// Global global = ec.global;
			// assert global == socketMessageQ.getGlobal() : "should match";
			// assert global == messageQ.getGlobal() : "should match";
			// ec.agent = Optional.of(this);
			// // Note that there is no ctx here.
			// assert !ec.ctx.isPresent() : "There is no socket context for message subscriptions";
			//
			// // can't we always just forward them to the client?
			// // do we need to look at them?
			// // CAN we look at them? They might be wrapped.
			// // static public void reply(Runnable message) {
			// ChannelHandlerContext ctx = this.ctx;
			// try {
			// String sendme = Global.serialize(message);
			// ctx.channel().writeAndFlush(new TextWebSocketFrame(sendme));
			// } catch (JsonProcessingException e) {
			// logger.error("bad message " + message, e);
			// }
			// }

			// Optional<String> channel = getChannelSubscriptionString();
			// logger.trace("message " + message + " on " + channel );
			// if (channel.isPresent()) {
			// handleSubscriptions.handle(message);
			// } else if (getCtxSessionAgent().isPresent()) {
			// handleSocket.handle(message);
			// }
		}
	}

	// Handler handleSock2 = (message) -> {
	//
	// logger.info("have handleSock2 message " + message);
	// };
	//
	// Handler handleSocket = (message) -> {
	// // is there an application?
	// if (message instanceof LogonMessage) {
	// String userid = ((LogonMessage) message).user;
	// String appid = ((LogonMessage) message).applicationId;
	// MyUser user = (MyUser) getGlobal().dynamoHelper.read(new MyUser(userid));// blocks
	//
	// logger.trace("socket " + userid + " " + appid + " " + user);
	//
	// handleSocket = handleSock2;
	//
	// } else
	// logger.trace("handleSocket  " + message);
	// };
	//
	// Handler handleSubscriptions = (message) -> {
	//
	// logger.info("have sub message " + message);
	// };

}
