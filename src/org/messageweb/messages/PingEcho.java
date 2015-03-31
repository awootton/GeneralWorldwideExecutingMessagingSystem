package org.messageweb.messages;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.messageweb.ServerGlobalState;
import org.messageweb.WsClientImpl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

// Send this: 
// {"@Class":"org.messageweb.messages.PingEcho"}
// on a socket. 

@JsonAutoDetect
public class PingEcho implements Runnable {
	
	public static Logger logger = Logger.getLogger(PingEcho.class);
	
	@JsonProperty
	private String key = "someRandomKeyToDoTricksWith";

	@Override
	public void run() {
		// Here's the thing: we can know the ctx because it's set in a global!!
		// this happened on the arrival of the message.
		

		ChannelHandlerContext ctx = ServerGlobalState.getCtx().get();
		if ( ctx == null ){
			logger.info(" PingEcho null context ");
			// this would mean that we are inside of the client
			Object got = WsClientImpl.cache.get(key);
			if ( got == null ){
				got = new AtomicInteger(0);
			}
			((AtomicInteger)got).addAndGet(1);
			WsClientImpl.cache.put(key, got, 100);
			return ;
		}
		
		logger.info(" PingEcho running -- have ctx name = " + ctx.name());

		try {
			String sendme = ServerGlobalState.serialize(this);
			// TODO: just call ServerGlobalState.reply(this); instead of 
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
