/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.configuration.SocketConfiguration;
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

/**
 * The implementation for the Netty's websockets services.
 */
public final class NettyWebSocketServiceImpl extends AbstractManager
    implements NettyWebSocketService {

  private static final String PREFIX_WEBSOCKET = "websocket";

  private static final int DEFAULT_SENDER_BUFFER_SIZE = 1024;
  private static final int DEFAULT_RECEIVER_BUFFER_SIZE = 1024;
  private static final int DEFAULT_PRODUCER_WORKER_SIZE = 2;
  private static final int DEFAULT_CONSUMER_WORKER_SIZE =
      Runtime.getRuntime().availableProcessors() * 2;

  private EventLoopGroup webSocketAcceptors;
  private EventLoopGroup webSocketWorkers;
  private List<Channel> serverWebSockets;

  private int senderBufferSize;
  private int receiverBufferSize;
  private int producerWorkerSize;
  private int consumerWorkerSize;

  private ConnectionFilter connectionFilter;
  private DataType dataType;
  private SessionManager sessionManager;
  private NetworkReaderStatistic networkReaderStatistic;
  private NetworkWriterStatistic networkWriterStatistic;
  private SocketConfiguration socketConfiguration;
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

  /**
   * Creates a new instance of web socket service.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link NettyWebSocketService}
   */
  public static NettyWebSocketService newInstance(EventManager eventManager) {
    return new NettyWebSocketServiceImpl(eventManager);
  }

  private void attemptToStart() throws InterruptedException {
    if (isInfoEnabled()) {
      info("START SERVICE",
          buildgen(getName(), " (", producerWorkerSize + consumerWorkerSize, ")"));
    }

    var defaultWebsocketThreadFactory =
        new DefaultThreadFactory(PREFIX_WEBSOCKET, true, Thread.NORM_PRIORITY);

    webSocketAcceptors =
        new NioEventLoopGroup(producerWorkerSize, defaultWebsocketThreadFactory);
    webSocketWorkers = new NioEventLoopGroup(consumerWorkerSize, defaultWebsocketThreadFactory);
    serverWebSockets = new ArrayList<>();

    WebSocketSslContext sslContext = null;
    if (usingSsl) {
      sslContext = new WebSocketSslContext();
    }

    var bootstrap = new ServerBootstrap();
    bootstrap.group(webSocketAcceptors, webSocketWorkers).channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 5)
        .childOption(ChannelOption.SO_SNDBUF, senderBufferSize)
        .childOption(ChannelOption.SO_RCVBUF, receiverBufferSize)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(
            NettyWsInitializer.newInstance(eventManager, sessionManager, connectionFilter, dataType,
                networkReaderStatistic, sslContext, usingSsl));

    var channelFuture = bootstrap.bind(Integer.parseInt(socketConfiguration.port())).sync()
        .addListener(future -> {
          if (!future.isSuccess()) {
            if (isErrorEnabled()) {
              error(future.cause());
            }
            throw new IOException(String.valueOf(socketConfiguration.port()));
          }
        });
    serverWebSockets.add(channelFuture.channel());

    if (isInfoEnabled()) {
      info("WEB SOCKET", buildgen("Started at port: ", socketConfiguration.port()));
    }
  }

  private void attemptToShutdown() {
    for (var socket : serverWebSockets) {
      close(socket);
    }
    serverWebSockets.clear();

    if (Objects.nonNull(webSocketAcceptors)) {
      webSocketAcceptors.shutdownGracefully();
    }
    if (Objects.nonNull(webSocketWorkers)) {
      webSocketWorkers.shutdownGracefully();
    }

    if (isInfoEnabled()) {
      info("STOPPED SERVICE",
          buildgen(getName(), " (", producerWorkerSize + consumerWorkerSize, ")"));
    }
    cleanup();
    if (isInfoEnabled()) {
      info("DESTROYED SERVICE",
          buildgen(getName(), " (", producerWorkerSize + consumerWorkerSize, ")"));
    }
  }

  private void cleanup() {
  }

  /**
   * Close a channel, see {@link Channel}.
   *
   * @param channel the closed channel
   */
  private void close(Channel channel) {
    if (Objects.isNull(channel)) {
      return;
    }

    try {
      channel.close().sync().addListener(future -> {
        if (!future.isSuccess()) {
          if (isErrorEnabled()) {
            error(future.cause());
          }
        }
      });
    } catch (InterruptedException exception) {
      if (isErrorEnabled()) {
        error(exception);
      }
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
  public void setWebSocketConfiguration(SocketConfiguration socketConfiguration) {
    this.socketConfiguration = socketConfiguration;
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
      if (packet.isMarkedAsLast()) {
        try {
          session.close(ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception, session.toString());
          }
        }
        return;
      }
      if (session.isActivated()) {
        session.fetchWebSocketChannel()
            .writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(packet.getData())));
        session.addWrittenBytes(packet.getOriginalSize());
        networkWriterStatistic.updateWrittenBytes(packet.getOriginalSize());
        networkWriterStatistic.updateWrittenPackets(1);
      } else {
        if (isDebugEnabled()) {
          debug("READ WEBSOCKET CHANNEL", "Session is inactivated: ", session.toString());
        }
      }
    }
  }
}
