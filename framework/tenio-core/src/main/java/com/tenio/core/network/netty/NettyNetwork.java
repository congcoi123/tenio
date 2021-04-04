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
package com.tenio.core.network.netty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.CommonObject;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.pool.IElementPool;
import com.tenio.core.configuration.constant.CoreConstants;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.entity.SocketConfig;
import com.tenio.core.event.IEventManager;
import com.tenio.core.monitoring.traffic.GlobalTrafficShapingHandlerCustomize;
import com.tenio.core.network.INetwork;
import com.tenio.core.network.netty.datagram.NettyDatagramInitializer;
import com.tenio.core.network.netty.socket.NettySocketInitializer;
import com.tenio.core.network.netty.ws.NettyWSInitializer;

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
@ThreadSafe
public final class NettyNetwork extends AbstractLogger implements INetwork {

	@GuardedBy("this")
	private EventLoopGroup __producer;
	@GuardedBy("this")
	private EventLoopGroup __consumer;
	private GlobalTrafficShapingHandlerCustomize __traficCounter;

	@GuardedBy("this")
	private List<Channel> __serverSockets;
	@GuardedBy("this")
	private List<Channel> __serverWebsockets;

	@SuppressWarnings("unchecked")
	@Override
	public void start(IEventManager eventManager, IConfiguration configuration,
			IElementPool<CommonObject> commonObjectPool,
			IElementPool<ByteArrayInputStream> byteArrayInputPool) throws IOException, InterruptedException {
		__producer = new NioEventLoopGroup(configuration.getInt(CoreConfigurationType.PRODUCER_THREAD_POOL_SIZE));
		__consumer = new NioEventLoopGroup(configuration.getInt(CoreConfigurationType.CONSUMER_THREAD_POOL_SIZE));

		__traficCounter = new GlobalTrafficShapingHandlerCustomize(eventManager, __consumer,
				CoreConstants.TRAFFIC_COUNTER_WRITE_LIMIT, CoreConstants.TRAFFIC_COUNTER_READ_LIMIT,
				configuration.getInt(CoreConfigurationType.TRAFFIC_COUNTER_CHECK_INTERVAL) * 1000);

		__serverSockets = new ArrayList<Channel>();
		__serverWebsockets = new ArrayList<Channel>();

		var socketPorts = (List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_PORTS);
		for (int connectionIndex = 0; connectionIndex < socketPorts.size(); connectionIndex++) {
			var socket = socketPorts.get(connectionIndex);
			switch (socket.getType()) {
			case TCP:
				__serverSockets.add(__bindTCP(connectionIndex, eventManager, configuration, commonObjectPool,
						byteArrayInputPool, socket));
				break;
			case UDP:
				__serverSockets.add(__bindUDP(connectionIndex, eventManager, configuration, commonObjectPool,
						byteArrayInputPool, socket));
				break;
			default:
				break;
			}
		}

		var webSocketPorts = (List<SocketConfig>) configuration.get(CoreConfigurationType.WEBSOCKET_PORTS);
		for (int connectionIndex = 0; connectionIndex < webSocketPorts.size(); connectionIndex++) {
			var socket = webSocketPorts.get(connectionIndex);
			switch (socket.getType()) {
			case WEB_SOCKET:
				__serverWebsockets.add(
						__bindWS(connectionIndex, eventManager, configuration, commonObjectPool, byteArrayInputPool, socket));
				break;
			default:
				break;
			}
		}

	}

	/**
	 * Constructs a Datagram socket and binds it to the specified port on the local
	 * host machine.
	 * 
	 * @param connectionIndex    the order of socket
	 * @param eventManager       the system event management
	 * @param configuration      your own configuration, see {@link IConfiguration}
	 * @param commonObjectPool   the pool of message objects
	 * @param byteArrayInputPool the pool of byte array input stream objects
	 * @param socketConfig       the socket information
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the channel, see {@link Channel}
	 */
	private Channel __bindUDP(int connectionIndex, IEventManager eventManager,
			IConfiguration configuration, IElementPool<CommonObject> commonObjectPool,
			IElementPool<ByteArrayInputStream> byteArrayInputPool, SocketConfig socketConfig)
			throws IOException, InterruptedException {
		var bootstrap = new Bootstrap();
		bootstrap.group(__consumer).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, false)
				.option(ChannelOption.SO_RCVBUF, 1024).option(ChannelOption.SO_SNDBUF, 1024)
				.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
						byteArrayInputPool, __traficCounter, configuration));

		_info("DATAGRAM", _buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Started at port: ", socketConfig.getPort()));

		return bootstrap.bind(socketConfig.getPort()).sync().channel();
	}

	/**
	 * Constructs a socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param connectionIndex    the order of socket
	 * @param eventManager       the system event management
	 * @param configuration      your own configuration, see {@link IConfiguration}
	 * @param commonObjectPool   the pool of message objects
	 * @param byteArrayInputPool the pool of byte array input stream objects
	 * @param socketConfig       the socket information
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the channel, see {@link Channel}
	 */
	private Channel __bindTCP(int connectionIndex, IEventManager eventManager,
			IConfiguration configuration, IElementPool<CommonObject> commonObjectPool,
			IElementPool<ByteArrayInputStream> byteArrayInputPool, SocketConfig socketConfig)
			throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__producer, __consumer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, 10240)
				.childOption(ChannelOption.SO_RCVBUF, 10240).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new NettySocketInitializer(connectionIndex, eventManager, commonObjectPool,
						byteArrayInputPool, __traficCounter, configuration));

		_info("SOCKET", _buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Started at port: ", socketConfig.getPort()));

		return bootstrap.bind(socketConfig.getPort()).sync().channel();
	}

	/**
	 * Constructs a web socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param connectionIndex    the order of socket
	 * @param eventManager       the system event management
	 * @param configuration      configuration your own configuration, see
	 *                           {@link IConfiguration}
	 * @param commonObjectPool   the pool of message objects
	 * @param byteArrayInputPool the pool of byte array input stream objects
	 * @param socketConfig       the socket information
	 * @throws IOException
	 * @throws InterruptedException
	 * @return the channel, see {@link Channel}
	 */
	private Channel __bindWS(int connectionIndex, IEventManager eventManager,
			IConfiguration configuration, IElementPool<CommonObject> commonObjectPool,
			IElementPool<ByteArrayInputStream> byteArrayInputPool, SocketConfig socketConfig)
			throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__producer, __consumer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, 1024)
				.childOption(ChannelOption.SO_RCVBUF, 1024).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new NettyWSInitializer(connectionIndex, eventManager, commonObjectPool,
						byteArrayInputPool, __traficCounter, configuration));

		_info("WEB SOCKET", _buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Started at port: ", socketConfig.getPort()));

		return bootstrap.bind(socketConfig.getPort()).sync().channel();
	}

	@Override
	public synchronized void shutdown() {
		for (var socket : __serverSockets) {
			__close(socket);
		}
		for (var socket : __serverWebsockets) {
			__close(socket);
		}

		if (__producer != null) {
			__producer.shutdownGracefully();
		}
		if (__consumer != null) {
			__consumer.shutdownGracefully();
		}

		__cleanup();
	}

	private void __cleanup() {
		__serverSockets.clear();
		__serverSockets = null;
		__serverWebsockets.clear();
		__serverWebsockets = null;

		__producer = null;
		__consumer = null;
		__traficCounter = null;
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
			_error(e);
			return false;
		}
	}

}
