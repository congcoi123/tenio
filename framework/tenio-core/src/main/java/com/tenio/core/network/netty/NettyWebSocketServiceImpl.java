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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.elements.CommonObject;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.common.pool.ElementsPool;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.events.EventManager;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.network.Network;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.netty.websocket.NettyWSInitializer;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
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
public final class NettyWebSocketServiceImpl extends SystemLogger implements NettyWebSocketService {

	private static final String PREFIX_WEBSOCKET = "websocket";

	@GuardedBy("this")
	private EventLoopGroup __websocketAcceptors;
	@GuardedBy("this")
	private EventLoopGroup __websocketWorkers;

	@GuardedBy("this")
	private List<Channel> __serverWebsockets;

	public NettyWebSocketServiceImpl() {

	}

	public void start(EventManager eventManager, Configuration configuration,
			ElementsPool<CommonObject> commonObjectPool, ElementsPool<ByteArrayInputStream> byteArrayInputPool)
			throws IOException, InterruptedException, BindException {

		var defaultWebsocketThreadFactory = new DefaultThreadFactory(PREFIX_WEBSOCKET, true, Thread.NORM_PRIORITY);

		__websocketAcceptors = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_ACCEPTOR),
				defaultWebsocketThreadFactory);
		__websocketWorkers = new NioEventLoopGroup(
				configuration.getInt(CoreConfigurationType.SOCKET_THREADS_POOL_WORKER), defaultWebsocketThreadFactory);
		__serverWebsockets = new ArrayList<Channel>();

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

	public synchronized void shutdown() {
		for (var socket : __serverWebsockets) {
			__close(socket);
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
		__serverWebsockets.clear();
		__serverWebsockets = null;
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
	public void initialize() throws ServiceRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws ServiceRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void halt() throws ServiceRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActivated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setProducerWorkerSize(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConsumerWorkerSize(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConnectionFilter(ConnectionFilter connectionFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSocketConfigs(List<SocketConfig> socketConfigs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUsingSSL(boolean usingSSL) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(Packet packet) {
		// TODO Auto-generated method stub

	}

}
