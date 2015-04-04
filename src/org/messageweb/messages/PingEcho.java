package org.messageweb.messages;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.messageweb.Global;
import org.messageweb.WsClientImpl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * When sent from a client, and executed on a server, it simply writs back to the client. When the client runs it an
 * integer in the timeoutQ is incremented.
 * 
 * @author awootton
 *
 */
// Send this:
// {"@Class":"org.messageweb.messages.PingEcho"}
//
//  {"@Class":"org.messageweb.messages.PingEcho","key":"someRandomKeyToDoTricksWith"}
//
// on a socket.

@JsonAutoDetect
public class PingEcho implements Runnable {

	public static Logger logger = Logger.getLogger(PingEcho.class);

	@JsonProperty
	private String key = "someRandomKeyToDoTricksWith";

	@JsonProperty
	private String info = "none";

	@Override
	public void run() {

		logger.info("PingEcho running");

		// Here's the thing: we can know the ctx because it's set in a global!!
		// this happened on the arrival of the message.

		if (!Global.getCtx().isPresent()) {
			WsClientImpl client = WsClientImpl.getClient();
			logger.info("PingEcho null context , back on client " + client);

			// this would mean that we are inside of the client
			Object got = client.cache.get(key);
			if (got == null) {
				got = new AtomicInteger(0);
			}
			((AtomicInteger) got).addAndGet(1);
			client.cache.put(key, got, 100);
			return;
		}
		ChannelHandlerContext ctx = Global.getCtx().get();
		logger.info("PingEcho running on server -- have ctx name = " + ctx.name());
		this.info = "from server:" + Global.getGlobal().id;
		try {
			String sendme = Global.serialize(this);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PingEcho other = (PingEcho) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
