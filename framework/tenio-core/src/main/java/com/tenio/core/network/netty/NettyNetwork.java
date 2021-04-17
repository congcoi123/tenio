/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.CommonObject;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.common.pool.IElementsPool;
import com.tenio.common.utility.OsUtility;
import com.tenio.common.utility.OsUtility.OSType;
import com.tenio.core.configuration.constant.CoreConstants;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.entity.BroadcastConfig;
import com.tenio.core.configuration.entity.SocketConfig;
import com.tenio.core.event.IEventManager;
import com.tenio.core.monitoring.traffic.GlobalTrafficShapingHandlerCustomize;
import com.tenio.core.network.IBroadcast;
import com.tenio.core.network.INetwork;
import com.tenio.core.network.netty.datagram.NettyBroadcastInitializer;
import com.tenio.core.network.netty.datagram.NettyDatagramInitializer;
import com.tenio.core.network.netty.socket.NettySocketInitializer;
import com.tenio.core.network.netty.ws.NettyWSInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueChannelOption;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
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
public final class NettyNetwork extends AbstractLogger implements INetwork, IBroadcast {

	// private static final int DEFAULT_NUMBER_THREADS_POOL =
	// Runtime.getRuntime().availableProcessors() * 2;

	private static final int DEFAULT_BROADCAST_PORT = 9999;
	private static final String BROADCAST_ADDRESS = "255.255.255.255";
	private static final int DATAGRAM_RECEIVE_BUFFER = 1024;
	private static final int DATAGRAM_SEND_BUFFER = 1024 * 1024;

	@GuardedBy("this")
	private EventLoopGroup __socketProducer;
	@GuardedBy("this")
	private EventLoopGroup __socketConsumer;
	@GuardedBy("this")
	private EventLoopGroup __datagramConsumer;
	@GuardedBy("this")
	private EventLoopGroup __broadcastConsumer;

	private GlobalTrafficShapingHandlerCustomize __traficCounter;

	@GuardedBy("this")
	private List<Channel> __serverSockets;
	@GuardedBy("this")
	private List<Channel> __serverWebsockets;
	@GuardedBy("this")
	private Channel __broadcastChannel;
	@GuardedBy("this")
	private Map<String, InetSocketAddress> __broadcastInets;

	private volatile boolean __broadcastActive;

	public NettyNetwork() {
		__broadcastActive = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(IEventManager eventManager, IConfiguration configuration,
			IElementsPool<CommonObject> commonObjectPool, IElementsPool<ByteArrayInputStream> byteArrayInputPool)
			throws IOException, InterruptedException {
		__socketProducer = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.PRODUCER_THREADS_POOL_SIZE));
		__socketConsumer = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.CONSUMER_THREADS_POOL_SIZE));

		__traficCounter = new GlobalTrafficShapingHandlerCustomize(eventManager, __socketConsumer,
				CoreConstants.TRAFFIC_COUNTER_WRITE_LIMIT, CoreConstants.TRAFFIC_COUNTER_READ_LIMIT,
				configuration.getInt(CoreConfigurationType.TRAFFIC_COUNTER_CHECK_INTERVAL) * 1000);

		__serverSockets = new ArrayList<Channel>();
		__serverWebsockets = new ArrayList<Channel>();

		var socketPorts = (List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_PORTS);
		for (int connectionIndex = 0; connectionIndex < socketPorts.size(); connectionIndex++) {
			var socketConfig = socketPorts.get(connectionIndex);
			switch (socketConfig.getType()) {
			case TCP:
				__serverSockets.add(__bindTCP(connectionIndex, eventManager, configuration, commonObjectPool,
						byteArrayInputPool, socketConfig));
				break;
			case UDP:
				__bindUDP(connectionIndex, eventManager, configuration, commonObjectPool, byteArrayInputPool,
						socketConfig);
				break;
			default:
				break;
			}
		}

		var broadcastPorts = (List<BroadcastConfig>) configuration.get(CoreConfigurationType.BROADCAST_PORTS);
		__bindBroadcast(broadcastPorts);

		var webSocketPorts = (List<SocketConfig>) configuration.get(CoreConfigurationType.WEBSOCKET_PORTS);
		for (int connectionIndex = 0; connectionIndex < webSocketPorts.size(); connectionIndex++) {
			var socket = webSocketPorts.get(connectionIndex);
			switch (socket.getType()) {
			case WEB_SOCKET:
				__serverWebsockets.add(__bindWS(connectionIndex, eventManager, configuration, commonObjectPool,
						byteArrayInputPool, socket));
				break;
			default:
				break;
			}
		}

	}

	/**
	 * Constructs a Datagram socket for broadcasting and binds it to the specified
	 * port on the local host machine.
	 * 
	 * @param broadcastPorts the list of broadcast ports
	 * @throws InterruptedException
	 */
	private void __bindBroadcast(List<BroadcastConfig> broadcastPorts) throws InterruptedException {
		if (broadcastPorts == null || broadcastPorts.isEmpty()) {
			return;
		}

		var bootstrap = new Bootstrap();
		__broadcastConsumer = new NioEventLoopGroup();
		bootstrap.group(__broadcastConsumer).channel(NioDatagramChannel.class).option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_RCVBUF, DATAGRAM_RECEIVE_BUFFER).option(ChannelOption.SO_BROADCAST, true)
				.option(ChannelOption.SO_SNDBUF, DATAGRAM_SEND_BUFFER)
				.handler(new NettyBroadcastInitializer(__traficCounter));
		var channelFuture = bootstrap.bind(DEFAULT_BROADCAST_PORT).sync();
		__broadcastChannel = channelFuture.channel();

		__broadcastInets = new HashMap<String, InetSocketAddress>();
		broadcastPorts.forEach(broadcastConfig -> {
			__broadcastInets.put(broadcastConfig.getId(),
					new InetSocketAddress(BROADCAST_ADDRESS, broadcastConfig.getPort()));
			_info("BROADCAST", _buildgen("Name: ", broadcastConfig.getName(), " > Id: ", broadcastConfig.getId(),
					" > Started at port: ", broadcastConfig.getPort()));
		});

		__broadcastActive = true;
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
	private void __bindUDP(int connectionIndex, IEventManager eventManager, IConfiguration configuration,
			IElementsPool<CommonObject> commonObjectPool, IElementsPool<ByteArrayInputStream> byteArrayInputPool,
			SocketConfig socketConfig) throws IOException, InterruptedException {
		var bootstrap = new Bootstrap();
		var threadsPool = configuration.getInt(CoreConfigurationType.CONSUMER_THREADS_POOL_SIZE);

		switch (OsUtility.getOperatingSystemType()) {
		case MacOS:
			__datagramConsumer = new KQueueEventLoopGroup(threadsPool);

			bootstrap.group(__datagramConsumer).channel(KQueueDatagramChannel.class)
					.option(ChannelOption.SO_RCVBUF, DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, DATAGRAM_SEND_BUFFER)
					.option(KQueueChannelOption.SO_REUSEPORT, true)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounter, configuration));
			break;

		case Linux:
			__datagramConsumer = new EpollEventLoopGroup(threadsPool);

			bootstrap.group(__datagramConsumer).channel(EpollDatagramChannel.class)
					.option(ChannelOption.SO_RCVBUF, DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, DATAGRAM_SEND_BUFFER).option(EpollChannelOption.SO_REUSEPORT, true)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounter, configuration));
			break;

		default:
			// includes Window
			__datagramConsumer = new NioEventLoopGroup();

			bootstrap.group(__datagramConsumer).channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_RCVBUF, DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, DATAGRAM_SEND_BUFFER)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounter, configuration));
			break;
		}

		if (OsUtility.getOperatingSystemType() == OSType.MacOS || OsUtility.getOperatingSystemType() == OSType.Linux) {
			for (int i = 0; i < threadsPool; i++) {
				var channelFuture = bootstrap.bind(socketConfig.getPort()).sync();
				__serverSockets.add(channelFuture.channel());
			}
		} else {
			var channelFuture = bootstrap.bind(socketConfig.getPort()).sync();
			__serverSockets.add(channelFuture.channel());
		}

		_info("DATAGRAM", _buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Started at port: ", socketConfig.getPort()));
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
	private Channel __bindTCP(int connectionIndex, IEventManager eventManager, IConfiguration configuration,
			IElementsPool<CommonObject> commonObjectPool, IElementsPool<ByteArrayInputStream> byteArrayInputPool,
			SocketConfig socketConfig) throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__socketProducer, __socketConsumer).channel(NioServerSocketChannel.class)
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
	private Channel __bindWS(int connectionIndex, IEventManager eventManager, IConfiguration configuration,
			IElementsPool<CommonObject> commonObjectPool, IElementsPool<ByteArrayInputStream> byteArrayInputPool,
			SocketConfig socketConfig) throws IOException, InterruptedException {
		var bootstrap = new ServerBootstrap();
		bootstrap.group(__socketProducer, __socketConsumer).channel(NioServerSocketChannel.class)
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
		__close(__broadcastChannel);

		if (__socketProducer != null) {
			__socketProducer.shutdownGracefully();
		}
		if (__socketConsumer != null) {
			__socketConsumer.shutdownGracefully();
		}
		if (__datagramConsumer != null) {
			__datagramConsumer.shutdownGracefully();
		}

		__cleanup();
	}

	private void __cleanup() {
		__serverSockets.clear();
		__serverSockets = null;
		__serverWebsockets.clear();
		__serverWebsockets = null;
		__broadcastInets.clear();
		__broadcastInets = null;

		__socketProducer = null;
		__socketConsumer = null;
		__datagramConsumer = null;
		__broadcastConsumer = null;
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

	@Override
	public boolean isActivated() {
		return __broadcastActive;
	}

	@Override
	public void sendBroadcast(String broadcastId, CommonObject message) {
		__broadcastChannel.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message)),
				__broadcastInets.get(broadcastId)));

	}

}
