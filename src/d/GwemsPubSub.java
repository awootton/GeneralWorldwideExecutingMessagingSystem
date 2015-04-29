package d;

import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;
import org.gwems.servers.impl.WsClientImpl;
import org.gwems.util.PubSub;

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
		 * Incoming messages to this client, or incoming in general, come directly through here. We are in the polling
		 * thread of the server. The message has just arrived from
		 * 
		 * @param ctx
		 * @param child
		 */
		@Override
		public void executeChannelMessage(ChannelHandlerContext ctx, String message) {
			// what comes back from GwemsPubSub is the message. Wrapped in a P2C
			// we don't want to deserialize in this nio thread.
			global.execute(() -> {
				try {
					Object obj = Global.deserialize(message);
					if ( obj instanceof P2C ){
						P2C p2c = (P2C)obj;
						handler.handle(p2c.c, p2c.m);
					}else {
						logger.warn("didn't expect type="+obj);
						((Runnable)obj).run();
					}
				} catch (Exception e) {
					logger.error(e);
				}
			});
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
	public void publish(String channel, Runnable message) {
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