/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.network.netty;

import java.io.IOException;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.INetwork;
import com.tenio.network.netty.datagram.NettyDatagramInitializer;
import com.tenio.network.netty.socket.NettySocketInitializer;
import com.tenio.network.netty.ws.NettyWSInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Use <a href="https://netty.io/">Netty</a> to handle a network instance @see
 * {@link INetwork}
 * 
 * @author kong
 * 
 */
public final class NettyNetwork extends AbstractLogger implements INetwork {

	private EventLoopGroup __producer;
	private EventLoopGroup __consumer;

	private Channel __tcp;
	private Channel __udp;
	private Channel __ws;

	@Override
	public void start(BaseConfiguration configuration) throws IOException, InterruptedException {
		__producer = new NioEventLoopGroup();
		__consumer = new NioEventLoopGroup();

		if (configuration.isDefined(BaseConfiguration.SOCKET_PORT)) {
			__bindTCP(configuration);
		}
		if (configuration.isDefined(BaseConfiguration.DATAGRAM_PORT)) {
			__bindUDP(configuration);
		}
		if (configuration.isDefined(BaseConfiguration.WEBSOCKET_PORT)) {
			__bindWS(configuration);
		}
	}

	/**
	 * Constructs a Datagram socket and binds it to the specified port on the local
	 * host machine.
	 * 
	 * @param configuration your own configuration @see {@link BaseConfiguration}
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void __bindUDP(BaseConfiguration configuration) throws IOException, InterruptedException {
		var bootstrap = new Bootstrap();
		bootstrap.group(__producer).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, false)
				.option(ChannelOption.SO_RCVBUF, 1024).option(ChannelOption.SO_SNDBUF, 1024)
				.handler(new NettyDatagramInitializer(configuration));

		__udp = bootstrap.bind(configuration.getInt(BaseConfiguration.DATAGRAM_PORT)).sync().channel();

		info("DATAGRAM", buildgen("Start at port: ", configuration.getInt(BaseConfiguration.DATAGRAM_PORT)));
	}

	/**
	 * Constructs a socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param configuration your own configuration @see {@link BaseConfiguration}
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void __bindTCP(BaseConfiguration configuration) throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__producer, __consumer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, 10240)
				.childOption(ChannelOption.SO_RCVBUF, 10240).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new NettySocketInitializer(configuration));

		__tcp = bootstrap.bind(configuration.getInt(BaseConfiguration.SOCKET_PORT)).sync().channel();

		info("SOCKET", buildgen("Start at port: ", configuration.getInt(BaseConfiguration.SOCKET_PORT)));
	}

	/**
	 * Constructs a web socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param configuration configuration your own configuration @see
	 *                      {@link BaseConfiguration}
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void __bindWS(BaseConfiguration configuration) throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__producer, __consumer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, 1024)
				.childOption(ChannelOption.SO_RCVBUF, 1024).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new NettyWSInitializer(configuration));

		__ws = bootstrap.bind(configuration.getInt(BaseConfiguration.WEBSOCKET_PORT)).sync().channel();

		info("WEB SOCKET", buildgen("Start at port: ", configuration.getInt(BaseConfiguration.WEBSOCKET_PORT)));
	}

	@Override
	public void shutdown() {
		__close(__tcp);
		__close(__udp);
		__close(__ws);

		if (__producer != null) {
			__producer.shutdownGracefully();
		}
		if (__consumer != null) {
			__consumer.shutdownGracefully();
		}
	}

	/**
	 * Close a channel @see {@link Channel}
	 * 
	 * @param channel the closed channel
	 * @return Returns <code>true</code> if the channel is closed without any
	 *         exceptions
	 */
	private boolean __close(Channel channel) {
		if (channel == null) {
			return false;
		}

		try {
			channel.close().sync();
			return true;
		} catch (InterruptedException e) {
			error("EXCEPTION CLOSE", "network", e);
			return false;
		}
	}

}
