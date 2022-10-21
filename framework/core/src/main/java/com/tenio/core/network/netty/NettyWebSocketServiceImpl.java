/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.common.data.DataType;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.netty.websocket.NettyWsInitializer;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.ssl.WebSocketSslContext;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The implementation for the Netty's websockets services.
 */
@ThreadSafe
public final class NettyWebSocketServiceImpl extends AbstractManager
    implements NettyWebSocketService {

  private static final String PREFIX_WEBSOCKET = "websocket";

  private static final int DEFAULT_SENDER_BUFFER_SIZE = 1024;
  private static final int DEFAULT_RECEIVER_BUFFER_SIZE = 1024;
  private static final int DEFAULT_PRODUCER_WORKER_SIZE = 2;
  private static final int DEFAULT_CONSUMER_WORKER_SIZE =
      Runtime.getRuntime().availableProcessors() * 2;

  @GuardedBy("this")
  private EventLoopGroup websocketAcceptors;
  @GuardedBy("this")
  private EventLoopGroup websocketWorkers;
  @GuardedBy("this")
  private List<Channel> serverWebsockets;

  private int senderBufferSize;
  private int receiverBufferSize;
  private int producerWorkerSize;
  private int consumerWorkerSize;

  private ConnectionFilter connectionFilter;
  private DataType dataType;
  private SessionManager sessionManager;
  private NetworkReaderStatistic networkReaderStatistic;
  private NetworkWriterStatistic networkWriterStatistic;
  private SocketConfig socketConfig;
  private boolean usingSsl;

  private boolean initialized;

  private NettyWebSocketServiceImpl(EventManager eventManager) {
    super(eventManager);

    senderBufferSize = DEFAULT_SENDER_BUFFER_SIZE;
    receiverBufferSize = DEFAULT_RECEIVER_BUFFER_SIZE;
    producerWorkerSize = DEFAULT_PRODUCER_WORKER_SIZE;
    consumerWorkerSize = DEFAULT_CONSUMER_WORKER_SIZE;

    initialized = false;
  }

  public static NettyWebSocketService newInstance(EventManager eventManager) {
    return new NettyWebSocketServiceImpl(eventManager);
  }

  private void attemptToStart() throws InterruptedException {

    info("START SERVICE",
        buildgen(getName(), " (", producerWorkerSize + consumerWorkerSize, ")"));

    var defaultWebsocketThreadFactory =
        new DefaultThreadFactory(PREFIX_WEBSOCKET, true, Thread.NORM_PRIORITY);

    websocketAcceptors =
        new NioEventLoopGroup(producerWorkerSize, defaultWebsocketThreadFactory);
    websocketWorkers = new NioEventLoopGroup(consumerWorkerSize, defaultWebsocketThreadFactory);
    serverWebsockets = new ArrayList<>();

    WebSocketSslContext sslContext = null;
    if (usingSsl) {
      sslContext = new WebSocketSslContext();
    }

    var bootstrap = new ServerBootstrap();
    bootstrap.group(websocketAcceptors, websocketWorkers).channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 5)
        .childOption(ChannelOption.SO_SNDBUF, senderBufferSize)
        .childOption(ChannelOption.SO_RCVBUF, receiverBufferSize)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(
            NettyWsInitializer.newInstance(eventManager, sessionManager, connectionFilter, dataType,
                networkReaderStatistic, sslContext, usingSsl));

    var channelFuture = bootstrap.bind(socketConfig.getPort()).sync()
        .addListener(future -> {
          if (!future.isSuccess()) {
            error(future.cause());
            throw new IOException(String.valueOf(socketConfig.getPort()));
          }
        });
    serverWebsockets.add(channelFuture.channel());

    info("WEB SOCKET", buildgen("Started at port: ", socketConfig.getPort()));
  }

  private synchronized void attemptToShutdown() {
    for (var socket : serverWebsockets) {
      close(socket);
    }
    serverWebsockets.clear();

    if (Objects.nonNull(websocketAcceptors)) {
      websocketAcceptors.shutdownGracefully();
    }
    if (Objects.nonNull(websocketWorkers)) {
      websocketWorkers.shutdownGracefully();
    }

    info("STOPPED SERVICE",
        buildgen(getName(), " (", producerWorkerSize + consumerWorkerSize, ")"));
    cleanup();
    info("DESTROYED SERVICE",
        buildgen(getName(), " (", producerWorkerSize + consumerWorkerSize, ")"));
  }

  private void cleanup() {
    serverWebsockets = null;
    websocketAcceptors = null;
    websocketWorkers = null;
  }

  /**
   * Close a channel, see {@link Channel}.
   *
   * @param channel the closed channel
   * @return {@code true} if the channel is closed without any exceptions
   */
  private boolean close(Channel channel) {
    if (Objects.isNull(channel)) {
      return false;
    }

    try {
      channel.close().sync().addListener(future -> {
        if (!future.isSuccess()) {
          error(future.cause());
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
    initialized = true;
  }

  @Override
  public void start() {
    if (!initialized) {
      return;
    }

    try {
      attemptToStart();
    } catch (InterruptedException e) {
      throw new ServiceRuntimeException(e.getMessage());
    }
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }
    attemptToShutdown();
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
    senderBufferSize = bufferSize;
  }

  @Override
  public void setReceiverBufferSize(int bufferSize) {
    receiverBufferSize = bufferSize;
  }

  @Override
  public void setProducerWorkerSize(int workerSize) {
    producerWorkerSize = workerSize;
  }

  @Override
  public void setConsumerWorkerSize(int workerSize) {
    consumerWorkerSize = workerSize;
  }

  @Override
  public void setConnectionFilter(ConnectionFilter connectionFilter) {
    this.connectionFilter = connectionFilter;
  }

  @Override
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    this.networkReaderStatistic = networkReaderStatistic;
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    this.networkWriterStatistic = networkWriterStatistic;
  }

  @Override
  public void setWebSocketConfig(SocketConfig socketConfig) {
    this.socketConfig = socketConfig;
  }

  @Override
  public void setUsingSsl(boolean usingSsl) {
    this.usingSsl = usingSsl;
  }

  @Override
  public void write(Packet packet) {
    var iterator = packet.getRecipients().iterator();
    while (iterator.hasNext()) {
      var session = iterator.next();
      session.getWebSocketChannel()
          .writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(packet.getData())));

      session.addWrittenBytes(packet.getOriginalSize());
      networkWriterStatistic.updateWrittenBytes(packet.getOriginalSize());
      networkWriterStatistic.updateWrittenPackets(1);
    }
  }
}
