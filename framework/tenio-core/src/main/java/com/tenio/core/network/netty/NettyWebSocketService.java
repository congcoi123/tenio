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
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.elements.CommonObject;
import com.tenio.common.loggers.AbstractLogger;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.common.pool.ElementsPool;
import com.tenio.common.utilities.OsUtility;
import com.tenio.common.utilities.OsUtility.OSType;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.data.BroadcastConfig;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.events.EventManager;
import com.tenio.core.network.IBroadcast;
import com.tenio.core.network.Network;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.netty.broadcast.NettyBroadcastInitializer;
import com.tenio.core.network.netty.datagram.NettyDatagramInitializer;
import com.tenio.core.network.netty.monitoring.GlobalTrafficShapingHandlerCustomize;
import com.tenio.core.network.netty.socket.NettySocketInitializer;
import com.tenio.core.network.netty.websocket.NettyWSInitializer;
import com.tenio.core.service.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
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
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Use <a href="https://netty.io/">Netty</a> to handle a network instance @see
 * {@link Network}
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class NettyWebSocketService extends AbstractLogger implements Network, Service {

	private static final String PREFIX_SOCKET = "socket";
	private static final String PREFIX_DATAGRAM = "datagram";
	private static final String PREFIX_WEBSOCKET = "websocket";
	private static final String PREFIX_BROADCAST = "broadcast";

	@GuardedBy("this")
	private EventLoopGroup __socketAcceptors;
	@GuardedBy("this")
	private EventLoopGroup __socketWorkers;
	@GuardedBy("this")
	private EventLoopGroup __datagramWorkers;
	@GuardedBy("this")
	private EventLoopGroup __broadcastWorkers;
	@GuardedBy("this")
	private EventLoopGroup __websocketAcceptors;
	@GuardedBy("this")
	private EventLoopGroup __websocketWorkers;

	private GlobalTrafficShapingHandlerCustomize __traficCounterSockets;
	private GlobalTrafficShapingHandlerCustomize __traficCounterDatagrams;
	private GlobalTrafficShapingHandlerCustomize __traficCounterWebsockets;
	private GlobalTrafficShapingHandlerCustomize __traficCounterBroadcast;

	@GuardedBy("this")
	private List<Channel> __serverSockets;
	@GuardedBy("this")
	private List<Channel> __serverWebsockets;
	@GuardedBy("this")
	private Channel __broadcastChannel;
	@GuardedBy("this")
	private Map<String, InetSocketAddress> __broadcastInets;

	private boolean __broadcastActive;

	private final OSType __osType;

	public NettyWebSocketService() {
		__broadcastActive = false;
		__osType = OsUtility.getOperatingSystemType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(EventManager eventManager, Configuration configuration,
			ElementsPool<CommonObject> commonObjectPool, ElementsPool<ByteArrayInputStream> byteArrayInputPool)
			throws IOException, InterruptedException, BindException {

		var defaultSocketThreadFactory = new DefaultThreadFactory(PREFIX_SOCKET, true, Thread.NORM_PRIORITY);
		var defaultDatagramThreadFactory = new DefaultThreadFactory(PREFIX_DATAGRAM, true, Thread.NORM_PRIORITY);
		var defaultBroadcastThreadFactory = new DefaultThreadFactory(PREFIX_BROADCAST, true, Thread.NORM_PRIORITY);
		var defaultWebsocketThreadFactory = new DefaultThreadFactory(PREFIX_WEBSOCKET, true, Thread.NORM_PRIORITY);

		__socketAcceptors = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_ACCEPTOR), defaultSocketThreadFactory);
		__socketWorkers = new NioEventLoopGroup(configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_WORKER),
				defaultSocketThreadFactory);

		__broadcastWorkers = new NioEventLoopGroup(1, defaultBroadcastThreadFactory);

		__websocketAcceptors = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_ACCEPTOR),
				defaultWebsocketThreadFactory);
		__websocketWorkers = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_WORKER), defaultWebsocketThreadFactory);

		switch (__osType) {
		case MacOS:
			__datagramWorkers = new KQueueEventLoopGroup(
					configuration.getInt(CoreConfigurationType.DATAGRAM_THREADS_POOL_WORKER),
					defaultDatagramThreadFactory);
			break;

		case Linux:
			__datagramWorkers = new EpollEventLoopGroup(
					configuration.getInt(CoreConfigurationType.DATAGRAM_THREADS_POOL_WORKER),
					defaultDatagramThreadFactory);
			break;

		case Windows:
			__datagramWorkers = new NioEventLoopGroup(1, defaultDatagramThreadFactory);
			break;

		default:
			__datagramWorkers = new NioEventLoopGroup(1, defaultDatagramThreadFactory);
			break;
		}

		__traficCounterSockets = new GlobalTrafficShapingHandlerCustomize(PREFIX_SOCKET, eventManager, __socketWorkers,
				CoreConstant.TRAFFIC_COUNTER_WRITE_LIMIT, CoreConstant.TRAFFIC_COUNTER_READ_LIMIT,
				configuration.getInt(CoreConfigurationType.TRAFFIC_COUNTER_CHECK_INTERVAL) * 1000);
		__traficCounterDatagrams = new GlobalTrafficShapingHandlerCustomize(PREFIX_DATAGRAM, eventManager,
				__datagramWorkers, CoreConstant.TRAFFIC_COUNTER_WRITE_LIMIT, CoreConstant.TRAFFIC_COUNTER_READ_LIMIT,
				configuration.getInt(CoreConfigurationType.TRAFFIC_COUNTER_CHECK_INTERVAL) * 1000);
		__traficCounterWebsockets = new GlobalTrafficShapingHandlerCustomize(PREFIX_WEBSOCKET, eventManager,
				__websocketWorkers, CoreConstant.TRAFFIC_COUNTER_WRITE_LIMIT, CoreConstant.TRAFFIC_COUNTER_READ_LIMIT,
				configuration.getInt(CoreConfigurationType.TRAFFIC_COUNTER_CHECK_INTERVAL) * 1000);
		__traficCounterBroadcast = new GlobalTrafficShapingHandlerCustomize(PREFIX_BROADCAST, eventManager,
				__broadcastWorkers, CoreConstant.TRAFFIC_COUNTER_WRITE_LIMIT, CoreConstant.TRAFFIC_COUNTER_READ_LIMIT,
				configuration.getInt(CoreConfigurationType.TRAFFIC_COUNTER_CHECK_INTERVAL) * 1000);

		__serverSockets = new ArrayList<Channel>();
		__serverWebsockets = new ArrayList<Channel>();

		var socketPorts = (List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_PORTS);
		for (int connectionIndex = 0; connectionIndex < socketPorts.size(); connectionIndex++) {
			var socketConfig = socketPorts.get(connectionIndex);
			switch (socketConfig.getType()) {
			case TCP:
				__bindTCP(connectionIndex, eventManager, configuration, commonObjectPool, byteArrayInputPool,
						socketConfig);
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
		__bindBroadcast(configuration, broadcastPorts);

		var webSocketPorts = (List<SocketConfig>) configuration.get(CoreConfigurationType.WEBSOCKET_PORTS);
		for (int connectionIndex = 0; connectionIndex < webSocketPorts.size(); connectionIndex++) {
			var socket = webSocketPorts.get(connectionIndex);
			switch (socket.getType()) {
			case WEB_SOCKET:
				__bindWS(connectionIndex, eventManager, configuration, commonObjectPool, byteArrayInputPool, socket);
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
	 * @param configuration  your own configuration, see {@link Configuration}
	 * @param broadcastPorts the list of broadcast ports
	 * @throws InterruptedException
	 */
	private void __bindBroadcast(Configuration configuration, List<BroadcastConfig> broadcastPorts)
			throws InterruptedException {
		if (broadcastPorts == null || broadcastPorts.isEmpty()) {
			return;
		}

		var bootstrap = new Bootstrap();
		bootstrap.group(__broadcastWorkers).channel(NioDatagramChannel.class).option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_RCVBUF, CoreConstant.BROADCAST_RECEIVE_BUFFER)
				.option(ChannelOption.SO_BROADCAST, true)
				.option(ChannelOption.SO_SNDBUF, CoreConstant.BROADCAST_SEND_BUFFER)
				.handler(new NettyBroadcastInitializer(__traficCounterBroadcast));

		var channelFuture = bootstrap.bind(configuration.getInt(CoreConfigurationType.SERVER_BROADCAST_PORT)).sync()
				.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						if (future.isSuccess()) {

						} else {
							error(future.cause());
							throw new IOException(
									String.valueOf(configuration.getInt(CoreConfigurationType.SERVER_BROADCAST_PORT)));
						}
					}
				});
		__broadcastChannel = channelFuture.channel();

		info("BROADCAST",
				buildgen("Server port: ", configuration.getInt(CoreConfigurationType.SERVER_BROADCAST_PORT)));

		__broadcastInets = new HashMap<String, InetSocketAddress>();
		broadcastPorts.forEach(broadcastConfig -> {
			__broadcastInets.put(broadcastConfig.getId(),
					new InetSocketAddress(CoreConstant.BROADCAST_ADDRESS, broadcastConfig.getPort()));

			info("BROADCAST", buildgen("Name: ", broadcastConfig.getName(), " > Id: ", broadcastConfig.getId(),
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
	 * @param configuration      your own configuration, see {@link Configuration}
	 * @param commonObjectPool   the pool of message objects
	 * @param byteArrayInputPool the pool of byte array input stream objects
	 * @param socketConfig       the socket information
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void __bindUDP(int connectionIndex, EventManager eventManager, Configuration configuration,
			ElementsPool<CommonObject> commonObjectPool, ElementsPool<ByteArrayInputStream> byteArrayInputPool,
			SocketConfig socketConfig) throws IOException, InterruptedException {

		var bootstrap = new Bootstrap();
		var threadsPoolWorker = configuration.getInt(CoreConfigurationType.DATAGRAM_THREADS_POOL_WORKER);

		switch (__osType) {
		case MacOS:
			bootstrap.group(__datagramWorkers).channel(KQueueDatagramChannel.class)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.option(ChannelOption.SO_RCVBUF, CoreConstant.DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, CoreConstant.DATAGRAM_SEND_BUFFER)
					.option(KQueueChannelOption.SO_REUSEPORT, true)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounterDatagrams, configuration));
			break;

		case Linux:
			bootstrap.group(__datagramWorkers).channel(EpollDatagramChannel.class)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.option(ChannelOption.SO_RCVBUF, CoreConstant.DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, CoreConstant.DATAGRAM_SEND_BUFFER)
					.option(EpollChannelOption.SO_REUSEPORT, true)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounterDatagrams, configuration));
			break;

		case Windows:
			bootstrap.group(__datagramWorkers).channel(NioDatagramChannel.class)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_RCVBUF, CoreConstant.DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, CoreConstant.DATAGRAM_SEND_BUFFER)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounterDatagrams, configuration));
			break;

		default:
			bootstrap.group(__datagramWorkers).channel(NioDatagramChannel.class)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_RCVBUF, CoreConstant.DATAGRAM_RECEIVE_BUFFER)
					.option(ChannelOption.SO_SNDBUF, CoreConstant.DATAGRAM_SEND_BUFFER)
					.handler(new NettyDatagramInitializer(connectionIndex, eventManager, commonObjectPool,
							byteArrayInputPool, __traficCounterDatagrams, configuration));
			break;
		}

		if (__osType == OSType.MacOS || __osType == OSType.Linux) {
			for (int i = 0; i < threadsPoolWorker; i++) {
				var channelFuture = bootstrap.bind(socketConfig.getPort()).sync()
						.addListener(new GenericFutureListener<Future<? super Void>>() {

							@Override
							public void operationComplete(Future<? super Void> future) throws Exception {
								if (future.isSuccess()) {

								} else {
									error(future.cause());
									throw new IOException(String.valueOf(socketConfig.getPort()));
								}
							}
						});
				__serverSockets.add(channelFuture.channel());
			}
		} else {
			var channelFuture = bootstrap.bind(socketConfig.getPort()).sync()
					.addListener(new GenericFutureListener<Future<? super Void>>() {

						@Override
						public void operationComplete(Future<? super Void> future) throws Exception {
							if (future.isSuccess()) {

							} else {
								error(future.cause());
								throw new IOException(String.valueOf(socketConfig.getPort()));
							}
						}
					});
			__serverSockets.add(channelFuture.channel());
		}

		info("DATAGRAM", buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Number of workers: ", threadsPoolWorker, " > Started at port: ", socketConfig.getPort()));
	}

	/**
	 * Constructs a socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param connectionIndex    the order of socket
	 * @param eventManager       the system event management
	 * @param configuration      your own configuration, see {@link Configuration}
	 * @param commonObjectPool   the pool of message objects
	 * @param byteArrayInputPool the pool of byte array input stream objects
	 * @param socketConfig       the socket information
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void __bindTCP(int connectionIndex, EventManager eventManager, Configuration configuration,
			ElementsPool<CommonObject> commonObjectPool, ElementsPool<ByteArrayInputStream> byteArrayInputPool,
			SocketConfig socketConfig) throws IOException, InterruptedException {

		var bootstrap = new ServerBootstrap();
		var threadsPoolWorker = configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_WORKER);

		bootstrap.group(__socketAcceptors, __socketWorkers).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_SNDBUF, CoreConstant.SOCKET_SEND_BUFFER)
				.childOption(ChannelOption.SO_RCVBUF, CoreConstant.SOCKET_RECEIVE_BUFFER)
				.childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new NettySocketInitializer(connectionIndex,
						eventManager, commonObjectPool, byteArrayInputPool, __traficCounterSockets, configuration));

		var channelFuture = bootstrap.bind(socketConfig.getPort()).sync()
				.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						if (future.isSuccess()) {

						} else {
							error(future.cause());
							throw new IOException(String.valueOf(socketConfig.getPort()));
						}
					}
				});
		__serverSockets.add(channelFuture.channel());

		info("SOCKET", buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Number of workers: ", threadsPoolWorker, " > Started at port: ", socketConfig.getPort()));
	}

	/**
	 * Constructs a web socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param connectionIndex    the order of socket
	 * @param eventManager       the system event management
	 * @param configuration      configuration your own configuration, see
	 *                           {@link Configuration}
	 * @param commonObjectPool   the pool of message objects
	 * @param byteArrayInputPool the pool of byte array input stream objects
	 * @param socketConfig       the socket information
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void __bindWS(int connectionIndex, EventManager eventManager, Configuration configuration,
			ElementsPool<CommonObject> commonObjectPool, ElementsPool<ByteArrayInputStream> byteArrayInputPool,
			SocketConfig socketConfig) throws IOException, InterruptedException {

		var bootstrap = new ServerBootstrap();
		var threadsPoolWorker = configuration.getInt(CoreConfigurationType.WEBSOCKET_THREADS_POOL_WORKER);

		bootstrap.group(__websocketAcceptors, __websocketWorkers).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5)
				.childOption(ChannelOption.SO_SNDBUF, CoreConstant.WEBSOCKET_SEND_BUFFER)
				.childOption(ChannelOption.SO_RCVBUF, CoreConstant.WEBSOCKET_RECEIVE_BUFFER)
				.childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new NettyWSInitializer(connectionIndex,
						eventManager, commonObjectPool, byteArrayInputPool, __traficCounterWebsockets, configuration));

		var channelFuture = bootstrap.bind(socketConfig.getPort()).sync()
				.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						if (future.isSuccess()) {

						} else {
							error(future.cause());
							throw new IOException(String.valueOf(socketConfig.getPort()));
						}
					}
				});
		__serverWebsockets.add(channelFuture.channel());

		info("WEB SOCKET", buildgen("Name: ", socketConfig.getName(), " > Index: ", connectionIndex,
				" > Number of workers: ", threadsPoolWorker, " > Started at port: ", socketConfig.getPort()));
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

		if (__socketAcceptors != null) {
			__socketAcceptors.shutdownGracefully();
		}
		if (__socketWorkers != null) {
			__socketWorkers.shutdownGracefully();
		}
		if (__datagramWorkers != null) {
			__datagramWorkers.shutdownGracefully();
		}
		if (__broadcastWorkers != null) {
			__broadcastWorkers.shutdownGracefully();
		}
		if (__websocketAcceptors != null) {
			__websocketAcceptors.shutdownGracefully();
		}
		if (__websocketWorkers != null) {
			__websocketWorkers.shutdownGracefully();
		}

		__cleanup();
	}

	private void __cleanup() {
		__serverSockets.clear();
		__serverSockets = null;
		__serverWebsockets.clear();
		__serverWebsockets = null;
		if (__broadcastInets != null) {
			__broadcastInets.clear();
			__broadcastInets = null;
		}

		__traficCounterBroadcast = null;
		__traficCounterDatagrams = null;
		__traficCounterSockets = null;
		__traficCounterWebsockets = null;
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
			channel.close().sync().addListener(new GenericFutureListener<Future<? super Void>>() {

				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					if (future.isSuccess()) {

					} else {
						error(future.cause());
					}
				}
			});
			return true;
		} catch (InterruptedException e) {
			error(e);
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
