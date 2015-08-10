package org.gwems.servers.impl;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.http.helloworld.HttpHelloWorldServerHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;

import org.gwems.servers.Global;

public class HttpPostHandler extends HttpHelloWorldServerHandler {

	final Global global;

	public HttpPostHandler(Global global) {
		super();
		this.global = global;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		FullHttpRequest request = (FullHttpRequest) msg;
		if (request.getMethod() != HttpMethod.POST) {
			GwemsMainHttpHandler.sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}
		String uri = request.getUri();
		System.out.println("HttpPostHandler uri = " + uri);
		if ("/gwems".equals(uri)) {

			// do gwems post of json to temp session object
			ByteBuf buf = request.content();
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);

			String message;
			try {
				message = new String(bytes, "utf8");

				System.out.println("content = " + message);// {"key":"val"}

				global.executeHttpMessage(message);

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//ctx.write(new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK));
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("Success: " + HttpResponseStatus.OK + "\r\n", CharsetUtil.UTF_8));
			response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
			// Close the connection as soon as the 200 message is sent.
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			System.out.println("sent response " + response);
			
		} else {
			GwemsMainHttpHandler.sendError(ctx, NOT_FOUND);
		}

	}
}
