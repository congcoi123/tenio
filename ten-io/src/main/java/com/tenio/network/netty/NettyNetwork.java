/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
import java.util.ArrayList;
import java.util.List;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.Constants;
import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.event.IEventManager;
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
	private GlobalTrafficShapingHandlerCustomize __traficCounter;

	private List<Channel> __sockets;
	private List<Channel> __websockets;

	@Override
	public String start(IEventManager eventManager, BaseConfiguration configuration) {
		__producer = new NioEventLoopGroup();
		__consumer = new NioEventLoopGroup();

		__traficCounter = new GlobalTrafficShapingHandlerCustomize(eventManager, __consumer,
				Constants.TRAFFIC_COUNTER_WRITE_LIMIT, Constants.TRAFFIC_COUNTER_READ_LIMIT,
				Constants.TRAFFIC_COUNTER_CHECK_INTERVAL);

		__sockets = new ArrayList<Channel>();
		__websockets = new ArrayList<Channel>();

		var socketPorts = configuration.getSocketPorts();
		for (int index = 0; index < socketPorts.size(); index++) {
			var socket = socketPorts.get(index);
			try {
				switch (socket.getType()) {
				case SOCKET:
					__sockets.add(__bindTCP(index, eventManager, configuration, socket.getPort()));
					break;
				case DATAGRAM:
					__sockets.add(__bindUDP(index, eventManager, configuration, socket.getPort()));
					break;
				default:
					break;
				}
			} catch (IOException e) {
				error(e, "port: ", socket.getPort());
				return ErrorMsg.IO_EXCEPTION;
			} catch (InterruptedException e) {
				error(e, "port: ", socket.getPort());
				return ErrorMsg.INTERRUPTED_EXCEPTION;
			}
		}

		var webSocketPorts = configuration.getWebSocketPorts();
		for (int index = 0; index < webSocketPorts.size(); index++) {
			var socket = webSocketPorts.get(index);
			try {
				switch (socket.getType()) {
				case WEB_SOCKET:
					__websockets.add(__bindWS(index, eventManager, configuration, socket.getPort()));
					break;
				default:
					break;
				}
			} catch (IOException e) {
				error(e, "port: ", socket.getPort());
				return ErrorMsg.IO_EXCEPTION;
			} catch (InterruptedException e) {
				error(e, "port: ", socket.getPort());
				return ErrorMsg.INTERRUPTED_EXCEPTION;
			}
		}

		return null;
	}

	/**
	 * Constructs a Datagram socket and binds it to the specified port on the local
	 * host machine.
	 * 
	 * @param index         the order of socket
	 * @param eventManager  the system event management
	 * @param configuration your own configuration, see {@link BaseConfiguration}
	 * @param port          the UDP port
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the channel, see {@link Channel}
	 */
	private Channel __bindUDP(int index, IEventManager eventManager, BaseConfiguration configuration, String port)
			throws IOException, InterruptedException {
		var bootstrap = new Bootstrap();
		bootstrap.group(__consumer).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, false)
				.option(ChannelOption.SO_RCVBUF, 1024).option(ChannelOption.SO_SNDBUF, 1024)
				.handler(new NettyDatagramInitializer(index, eventManager, __traficCounter, configuration));

		info("DATAGRAM", buildgen("Start at port: ", port));

		return bootstrap.bind(configuration.getInt(port)).sync().channel();
	}

	/**
	 * Constructs a socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param index         the order of socket
	 * @param eventManager  the system event management
	 * @param configuration your own configuration, see {@link BaseConfiguration}
	 * @param port          the TCP port
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the channel, see {@link Channel}
	 */
	private Channel __bindTCP(int index, IEventManager eventManager, BaseConfiguration configuration, String port)
			throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__producer, __consumer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, 10240)
				.childOption(ChannelOption.SO_RCVBUF, 10240).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new NettySocketInitializer(index, eventManager, __traficCounter, configuration));

		info("SOCKET", buildgen("Start at port: ", port));

		return bootstrap.bind(configuration.getInt(port)).sync().channel();
	}

	/**
	 * Constructs a web socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param index         the order of socket
	 * @param eventManager  the system event management
	 * @param configuration configuration your own configuration, see
	 *                      {@link BaseConfiguration}
	 * @param port          the Web Socket port
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the channel, see {@link Channel}
	 */
	private Channel __bindWS(int index, IEventManager eventManager, BaseConfiguration configuration, String port)
			throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__producer, __consumer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, 1024)
				.childOption(ChannelOption.SO_RCVBUF, 1024).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new NettyWSInitializer(index, eventManager, __traficCounter, configuration));

		info("WEB SOCKET", buildgen("Start at port: ", port));

		return bootstrap.bind(configuration.getInt(port)).sync().channel();
	}

	@Override
	public void shutdown() {
		for (var socket : __sockets) {
			__close(socket);
		}
		for (var socket : __websockets) {
			__close(socket);
		}

		if (__producer != null) {
			__producer.shutdownGracefully();
		}
		if (__consumer != null) {
			__consumer.shutdownGracefully();
		}
	}

	/**
	 * Close a channel, see {@link Channel}
	 * 
	 * @param channel the closed channel
	 * @return <b>true</b> if the channel is closed without any exceptions
	 */
	private boolean __close(Channel channel) {
		if (channel == null) {
			return false;
		}

		try {
			channel.close().sync();
			return true;
		} catch (InterruptedException e) {
			error(e);
			return false;
		}
	}

}
