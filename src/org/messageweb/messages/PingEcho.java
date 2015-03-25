package org.messageweb.messages;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;

import org.messageweb.ServerGlobalState;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

// want:
// @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)

// Send this: 
// {"@Class":"org.messageweb.messages.PingEcho"}
// on a socket. 

@JsonAutoDetect
public class PingEcho implements Runnable {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {

		MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "@Class");
	}

	public static Runnable deser(String s) throws JsonParseException,
			JsonMappingException, IOException {
		Runnable obj = MAPPER.readValue(s, Runnable.class);
		return obj;
	}

	@Override
	public void run() {
		// Here's the thing: we can know the ctx because it's set in a global!!
		// this happened on the arrival of the message.

		ChannelHandlerContext ctx = ServerGlobalState.getCtx();
		//System.out.println(" have ctx name = " + ctx.name());

		try {
			String sendme = MAPPER.writeValueAsString(this);
			//System.out.println("sendinfg " + sendme);
			ctx.channel().write(new TextWebSocketFrame(sendme));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		PingEcho p = new PingEcho();

		try {

			String str = MAPPER.writerWithDefaultPrettyPrinter()
					.writeValueAsString(p);

			System.out.println(str);

			String s = MAPPER.writeValueAsString(p);
			System.out.println(s);

			Object obj = MAPPER.readValue(s, Object.class);

			System.out.println("deser = " + obj);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
