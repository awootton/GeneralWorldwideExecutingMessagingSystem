package org.messageweb.socketimpl;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

import org.apache.log4j.Logger;

/** a logger in the pipe
 * 
 * @author awootton
 *
 */
public class WsLoggingHandler extends ChannelDuplexHandler {

	private static Logger logger = Logger.getLogger(WsLoggingHandler.class);

	
	public WsLoggingHandler(  ){
	}

	String format(ChannelHandlerContext ctx, String message) {
		String s = "Handler " + ctx.name() +":" + message;
		System.out.println(s);
		return s;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "REGISTERED"));
		}
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "UNREGISTERED"));
		}
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "ACTIVE"));
		}
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "INACTIVE"));
		}
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "EXCEPTION: " + cause), cause);
		}
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "USER_EVENT: " + evt));
		}
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
			ChannelPromise promise) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "BIND(" + localAddress + ')'));
		}
		super.bind(ctx, localAddress, promise);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(
					format(ctx, "CONNECT(" + remoteAddress + ", "
							+ localAddress + ')'));
		}
		super.connect(ctx, remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "DISCONNECT()"));
		}
		super.disconnect(ctx, promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "CLOSE()"));
		}
		super.close(ctx, promise);
	}

	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "DEREGISTER()"));
		}
		super.deregister(ctx, promise);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		logger.debug(format(ctx," RECEIVED") + msg);
		ctx.fireChannelRead(msg);
	}

//	@Override
//	public void read(ChannelHandlerContext ctx   )
//			throws Exception {
//		logger.debug(format(ctx," READ ") );
//		super.read(ctx);
//	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		logger.debug( "WRITE" +  msg);
		ctx.write(msg, promise);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(format(ctx, "FLUSH"));
		}
		ctx.flush();
	}

}
