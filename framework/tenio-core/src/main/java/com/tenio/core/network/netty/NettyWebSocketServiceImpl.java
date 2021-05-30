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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.netty.websocket.NettyWSInitializer;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.ssl.WebSocketSslContext;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

@ThreadSafe
public final class NettyWebSocketServiceImpl extends AbstractManager implements NettyWebSocketService {

	private static final String PREFIX_WEBSOCKET = "websocket";

	private static final int DEFAULT_SENDER_BUFFER_SIZE = 1024;
	private static final int DEFAULT_RECEIVER_BUFFER_SIZE = 1024;
	private static final int DEFAULT_PRODUCER_WORKER_SIZE = 2;
	private static final int DEFAULT_CONSUMER_WORKER_SIZE = Runtime.getRuntime().availableProcessors() * 2;

	@GuardedBy("this")
	private EventLoopGroup __websocketAcceptors;
	@GuardedBy("this")
	private EventLoopGroup __websocketWorkers;
	@GuardedBy("this")
	private List<Channel> __serverWebsockets;

	private int __senderBufferSize;
	private int __receiverBufferSize;
	private int __producerWorkerSize;
	private int __consumerWorkerSize;

	private ConnectionFilter __connectionFilter;
	private SessionManager __sessionManager;
	private NetworkReaderStatistic __networkReaderStatistic;
	private NetworkWriterStatistic __networkWriterStatistic;
	private SocketConfig __socketConfig;
	private boolean __usingSSL;

	private boolean __initialized;

	public static NettyWebSocketService newInstance(EventManager eventManager) {
		return new NettyWebSocketServiceImpl(eventManager);
	}

	private NettyWebSocketServiceImpl(EventManager eventManager) {
		super(eventManager);

		__senderBufferSize = DEFAULT_SENDER_BUFFER_SIZE;
		__receiverBufferSize = DEFAULT_RECEIVER_BUFFER_SIZE;
		__producerWorkerSize = DEFAULT_PRODUCER_WORKER_SIZE;
		__consumerWorkerSize = DEFAULT_CONSUMER_WORKER_SIZE;

		__initialized = false;
	}

	private void __start() throws InterruptedException {

		info("START SERVICE", buildgen(getName(), " (", __producerWorkerSize + __consumerWorkerSize, ")"));

		var defaultWebsocketThreadFactory = new DefaultThreadFactory(PREFIX_WEBSOCKET, true, Thread.NORM_PRIORITY);

		__websocketAcceptors = new NioEventLoopGroup(__producerWorkerSize, defaultWebsocketThreadFactory);
		__websocketWorkers = new NioEventLoopGroup(__consumerWorkerSize, defaultWebsocketThreadFactory);
		__serverWebsockets = new ArrayList<Channel>();

		WebSocketSslContext sslContext = null;
		if (__usingSSL) {
			sslContext = new WebSocketSslContext();
		}

		var bootstrap = new ServerBootstrap();
		bootstrap.group(__websocketAcceptors, __websocketWorkers).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_SNDBUF, __senderBufferSize)
				.childOption(ChannelOption.SO_RCVBUF, __receiverBufferSize)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(NettyWSInitializer.newInstance(__eventManager, __sessionManager, __connectionFilter,
						__networkReaderStatistic, sslContext, __usingSSL));

		var channelFuture = bootstrap.bind(__socketConfig.getPort()).sync()
				.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						if (future.isSuccess()) {

						} else {
							error(future.cause());
							throw new IOException(String.valueOf(__socketConfig.getPort()));
						}
					}
				});
		__serverWebsockets.add(channelFuture.channel());

		info("WEB SOCKET", buildgen("Started at port: ", __socketConfig.getPort()));

	}

	private synchronized void __shutdown() {
		for (var socket : __serverWebsockets) {
			__close(socket);
		}
		__serverWebsockets.clear();

		if (__websocketAcceptors != null) {
			__websocketAcceptors.shutdownGracefully();
		}
		if (__websocketWorkers != null) {
			__websocketWorkers.shutdownGracefully();
		}

		info("STOPPED SERVICE", buildgen(getName(), " (", __producerWorkerSize + __consumerWorkerSize, ")"));
		__cleanup();
		info("DESTROYED SERVICE", buildgen(getName(), " (", __producerWorkerSize + __consumerWorkerSize, ")"));
	}

	private void __cleanup() {
		__serverWebsockets = null;
		__websocketAcceptors = null;
		__websocketWorkers = null;
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
	public void initialize() {
		__initialized = true;
	}

	@Override
	public void start() {
		if (!__initialized) {
			return;
		}

		try {
			__start();
		} catch (InterruptedException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		if (!__initialized) {
			return;
		}
		__shutdown();
	}

	@Override
	public String getName() {
		return "netty-websocket";
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActivated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSenderBufferSize(int bufferSize) {
		__senderBufferSize = bufferSize;
	}

	@Override
	public void setReceiverBufferSize(int bufferSize) {
		__receiverBufferSize = bufferSize;
	}

	@Override
	public void setProducerWorkerSize(int workerSize) {
		__producerWorkerSize = workerSize;
	}

	@Override
	public void setConsumerWorkerSize(int workerSize) {
		__consumerWorkerSize = workerSize;
	}

	@Override
	public void setConnectionFilter(ConnectionFilter connectionFilter) {
		__connectionFilter = connectionFilter;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		__sessionManager = sessionManager;
	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic) {
		__networkReaderStatistic = readerStatistic;
	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic) {
		__networkWriterStatistic = writerStatistic;
	}

	@Override
	public void setWebSocketConfig(SocketConfig socketConfig) {
		__socketConfig = socketConfig;
	}

	@Override
	public void setUsingSSL(boolean usingSSL) {
		__usingSSL = usingSSL;
	}

	@Override
	public void write(Packet packet) {
		var iterator = packet.getRecipients().iterator();
		while (iterator.hasNext()) {
			var session = iterator.next();
			session.getWebSocketChannel()
					.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(packet.getData())));

			session.addWrittenBytes(packet.getOriginalSize());
			__networkWriterStatistic.updateWrittenBytes(packet.getOriginalSize());
			__networkWriterStatistic.updateWrittenPackets(1);
		}
	}

}
