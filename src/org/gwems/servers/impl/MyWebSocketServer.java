package org.gwems.servers.impl;

/** Copyright Alan Wootton 2015
 * 
 */

/**
 * @author awootton
 *
 */
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//package io.netty.example.http.websocketx.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gwems.servers.Global;

/**
 * A HTTP server which serves Web Socket requests at:
 *
 * http://localhost:8080/websocket
 *
 * Open your browser at http://localhost:8080/, then the demo page will be loaded and a Web Socket connection will be made automatically.
 *
 * This server illustrates support for the different web socket specification versions and will work with:
 *
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public final class MyWebSocketServer {

	public static Logger logger = Logger.getLogger(MyWebSocketServer.class);

	final boolean SSL; 

	static Set<Integer> portsStarted = new HashSet<Integer>();// doesn't work in reload

	Global global;

	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;

	public MyWebSocketServer(Global global, boolean SSL ) {
		super();
		this.global = global;
		this.SSL = SSL;
	}

	public void stop() {
		while (workerGroup == null || bossGroup == null) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public class Starter implements Runnable {

		int port;
		Global global;

		public Starter(int port, Global global) {

			this.port = port;
			this.global = global;
		}

		public void run() {
			MyWebSocketServer server = MyWebSocketServer.this;
			try {
				server.start(port, global);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private void start(int port, Global global) throws Exception {

		synchronized (portsStarted) {// does this even work?? 
			if (portsStarted.contains(port))
				return;
			portsStarted.add(port);
		}

		logger.info("Starting " + global + " on " + port);
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		} else {
			sslCtx = null;
		}

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new WsLoggingHandler(global))
					.childHandler(new MyWebSocketServerInitializer(sslCtx, global));

			Channel ch = b.bind(port).sync().channel();

			System.out.println("Open your web browser and navigate to " + (SSL ? "https" : "http") + "://127.0.0.1:" + port + '/');
			
			startedChannel = true;

			ch.closeFuture().sync();// and, the thread starts to wait.
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			portsStarted.remove(port);
		}
	}
	
	public boolean startedChannel = false;

	static class MyWebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

		private final SslContext sslCtx;
		Global global;

		public MyWebSocketServerInitializer(SslContext sslCtx, Global global) {
			this.sslCtx = sslCtx;
			this.global = global;
		}

		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			if (sslCtx != null) {
				pipeline.addLast(sslCtx.newHandler(ch.alloc()));
			}
			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(65536));
			
			pipeline.addLast(new ChunkedWriteHandler());
			
			GwemsMainHttpHandler httpHandler = new GwemsMainHttpHandler(global);
			
			pipeline.addLast(new MyWebSocketServerHandler(global,httpHandler));
		}
	}

}