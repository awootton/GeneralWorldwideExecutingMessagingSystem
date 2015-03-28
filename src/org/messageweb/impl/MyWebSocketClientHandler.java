package org.messageweb.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.messageweb.ServerGlobalState;
import org.messageweb.WsClientImpl;

public class MyWebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

	public static Logger logger = Logger.getLogger(MyWebSocketClientHandler.class);

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;

	WsClientImpl client;

	public MyWebSocketClientHandler(WebSocketClientHandshaker handshaker, WsClientImpl client) {
		this.handshaker = handshaker;
		this.client = client;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		handshakeFuture = ctx.newPromise();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		handshaker.handshake(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		logger.debug("WebSocket Client disconnected!");
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			logger.debug("WebSocket Client connected!");
			handshakeFuture.setSuccess();
			return;
		}

		if (msg instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) msg;
			throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content="
					+ response.content().toString(CharsetUtil.UTF_8) + ')');
		}

		WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof TextWebSocketFrame) {
			TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

			logger.trace("WebSocket Client atw received textFrame message: " + textFrame.text());

			// change into a runnable.
			Runnable r;
			try {
				// deserialize in a new thread - note here!
				// r = ServerGlobalState.deserialize(textFrame.text());
				client.executeChannelMessage(ctx, textFrame.text());
			} catch (Exception e) {
				logger.error(e);
			}

		} else if (frame instanceof PongWebSocketFrame) {
			logger.info("WebSocket Client received pong - not handled");
		} else if (frame instanceof CloseWebSocketFrame) {
			logger.info("WebSocket Client received closing");
			ch.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// cause.printStackTrace();
		logger.error(cause);
		if (!handshakeFuture.isDone()) {
			handshakeFuture.setFailure(cause);
		}
		ctx.close();
	}
}
