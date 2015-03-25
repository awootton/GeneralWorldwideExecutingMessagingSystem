package org.messageweb.messages;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.messageweb.ServerGlobalState;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;

// Send this: 
// {"@Class":"org.messageweb.messages.PingEcho"}
// on a socket. 

@JsonAutoDetect
public class PingEcho implements Runnable {

	@Override
	public void run() {
		// Here's the thing: we can know the ctx because it's set in a global!!
		// this happened on the arrival of the message.
		
		// TODO: just call ServerGlobalState.reply(this);

		ChannelHandlerContext ctx = ServerGlobalState.getCtx();
		//System.out.println(" have ctx name = " + ctx.name());

		try {
			String sendme = ServerGlobalState.serialize(this);
			ctx.channel().write(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

}
