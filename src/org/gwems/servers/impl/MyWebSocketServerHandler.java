package org.gwems.servers.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;

/**
 * This was copied from a netty example. Hope it's ok - atw.
 * 
 * @author awootton
 *
 */
public class MyWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	public static Logger logger = Logger.getLogger(MyWebSocketServerHandler.class);

	private static final String WEBSOCKET_PATH = "/websocket";

	private WebSocketServerHandshaker handshaker;

	private final Global global;

	GwemsMainHttpHandler httpHandler;

	boolean isSSH = false;

	public MyWebSocketServerHandler(Global global, GwemsMainHttpHandler httpHandler) {
		super();
		this.global = global;
		this.httpHandler = httpHandler;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) {
		
		boolean upgrade = false;
		boolean isHttp = false;
		if (msg instanceof FullHttpRequest) {
			isHttp = true;
			String upgradeStr = ((FullHttpRequest) msg).headers().get("Connection");
			if (upgradeStr != null && ("Upgrade".equals(upgradeStr))) {
				upgrade = true;
			}
		}
		if (isHttp) {
			if (upgrade) {
				// Handshake
				FullHttpRequest req = (FullHttpRequest) msg;
				WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
				handshaker = wsFactory.newHandshaker(req);
				if (handshaker == null) {
					WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
				} else {
					handshaker.handshake(ctx.channel(), req);
				}
			} else {
				// handleHttpRequest(ctx, (FullHttpRequest) msg);
				httpHandler.channelRead(ctx, msg);
			}
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		} else {
			// logger.error("unknown request type " + msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	// @SuppressWarnings("unused")
	// private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
	// // Handle a bad request.
	// if (!req.getDecoderResult().isSuccess()) {
	// sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
	// return;
	// }
	//
	// // Allow only GET methods.
	// if (req.getMethod() != GET) {
	// sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
	// return;
	// }
	// boolean doHttpStuff = false || req.getMethod() != GET;// NEVER
	// // Send the demo page and favicon.ico
	// if (doHttpStuff && "/".equals(req.getUri())) {
	// ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
	// FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
	//
	// res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	// HttpHeaders.setContentLength(res, content.readableBytes());
	//
	// sendHttpResponse(ctx, req, res);
	// return;
	// }
	// if (doHttpStuff && "/favicon.ico".equals(req.getUri())) {
	// FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
	// sendHttpResponse(ctx, req, res);
	// return;
	// }
	//
	// // Handshake
	// WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req),
	// null, true);
	// handshaker = wsFactory.newHandshaker(req);
	// if (handshaker == null) {
	// WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
	// } else {
	// handshaker.handshake(ctx.channel(), req);
	// }
	// }

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			if (logger.isDebugEnabled()) {
				logger.debug("closing socket with CloseWebSocketFrame ");
			}
			// can we just do this here, or do we need to call global?
			AttributeKey<String> key = AttributeKey.<String> valueOf("session");
			Attribute<String> sessionStringAttribute = ctx.attr(key);
			if (sessionStringAttribute.get() == null) {
			} else {
				// and run all the closing routines asap.
				global.timeoutCache.setTtl(sessionStringAttribute.get(), 0);
			}
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			if (logger.isDebugEnabled()) {
				logger.debug("Socket has PingWebSocketFrame");
			}
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Socket unknown frame type " + frame.getClass().getName());
			}
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}

		String request = ((TextWebSocketFrame) frame).text();

		if (logger.isTraceEnabled()) {
			logger.trace("have TextWebSocketFrame " + request + " on " + ctx);
		}
		Exception ex = null;
		try {
			global.executeChannelMessage(ctx, request);
			return;
		} catch (Exception e) {
			ex = e;
		}
		ctx.channel().write(new TextWebSocketFrame("got " + request + " An Error Happened " + ex));// .toUpperCase()));
	}

//	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
//		// Generate an error page if response getStatus code is not OK (200).
//		if (res.getStatus().code() != 200) {
//			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
//			res.content().writeBytes(buf);
//			buf.release();
//			HttpHeaders.setContentLength(res, res.content().readableBytes());
//		}
//
//		// Send the response and close the connection if necessary.
//		ChannelFuture f = ctx.channel().writeAndFlush(res);
//		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
//			f.addListener(ChannelFutureListener.CLOSE);
//		}
//	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();// closes the server?? atw
	}

	// What was this for?
	private String getWebSocketLocation(FullHttpRequest req) {
		String location = req.headers().get("Host") + WEBSOCKET_PATH;
		if (this.isSSH) {
			return "wss://" + location;
		} else {
			return "ws://" + location;
		}
	}
}
