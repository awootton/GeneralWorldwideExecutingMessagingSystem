/**
 * Making your server support SSL/TLS
 *  // In your ChannelInitializer:
 *  
 ChannelPipeline p = channel.pipeline();
 SslContext sslCtx = SslContextBuilder.forServer(...).build();
 p.addLast("ssl", sslCtx.newEngine(channel.alloc()));
 ...

All valid Netty transport keys are defined in org.hornetq.core.remoting.impl.netty.TransportConstants. Most parameters can be used with acceptors and connectors. Some only work with acceptors. The following parameters can be used to configure Netty for simple TCP





 */
/**
 * @author awootton
 *
 */
package org.gwems.sslSocket;