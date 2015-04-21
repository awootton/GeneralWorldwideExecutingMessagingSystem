package d;

import gwems.Push2Client;
import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;
import org.gwems.servers.WsClientImpl;
import org.gwems.util.PubSub;

import com.fasterxml.jackson.core.JsonProcessingException;

public class GwemsPubSub extends PubSub {

	public static Logger logger = Logger.getLogger(GwemsPubSub.class);

	/**
	 * We open a WebSocket client to a Global that does it
	 * 
	 */
	PubSubWsClientImpl client;

	Handler handler = null;

	public GwemsPubSub(String host, int port) {
		client = new PubSubWsClientImpl(host, port);
	}

	public GwemsPubSub(String host, int port, Handler handler) {
		client = new PubSubWsClientImpl(host, port);
		this.handler = handler;
	}

	private class PubSubWsClientImpl extends WsClientImpl {

		public PubSubWsClientImpl(String host, int port) {
			super(host, port);
		}

		/**
		 * Incoming messages to this client, or incoming in general, come directly through here.
		 * 
		 * THe message has just arrived from
		 * 
		 * @param ctx
		 * @param child
		 */
		@Override
		public void executeChannelMessage(ChannelHandlerContext ctx, String message) {

			String channel = "need one";
			String newMessage = "";
			if (global.isPubSub) {
				// it's going to get Q'd for execution on all the agents
				// and they need to just send the string to their sockets.
				// So, wrap it with a p2c
				P2C p2c = new P2C(message);
				try {
					newMessage = Global.serialize(p2c);
					handler.handle(channel, newMessage);
				} catch (JsonProcessingException e) {
					logger.error(e);
				}
			} else {
				handler.handle(channel, message);
			}
		}
	}

	@Override
	public void subcribe(String... channels) {
		for (String string : channels) {
			Sub msg = new Sub(string);
			client.enqueueRunnable(msg);
		}
	}

	@Override
	public void publish(String channel, String message) {
		Pub pub = new Pub(channel, message);
		client.enqueueRunnable(pub);
	}

	@Override
	public void unsubcribe(String... channels) {
		for (String string : channels) {
			Unsub msg = new Unsub(string);
			client.enqueueRunnable(msg);
		}
	}

	@Override
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void stop() {
		client.stop();
	}

}