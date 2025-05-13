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

package com.tenio.core.network;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.entity.session.manager.SessionManagerImpl;
import com.tenio.core.network.jetty.JettyHttpService;
import com.tenio.core.network.kcp.KcpService;
import com.tenio.core.network.kcp.KcpServiceImpl;
import com.tenio.core.network.netty.NettyWebSocketService;
import com.tenio.core.network.netty.NettyWebSocketServiceImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.ZeroSocketService;
import com.tenio.core.network.zero.ZeroSocketServiceImpl;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import jakarta.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * The implementation for network service.
 *
 * @see NetworkService
 */
public final class NetworkServiceImpl extends AbstractManager implements NetworkService {

  private final SessionManager sessionManager;
  private final JettyHttpService httpService;
  private final NettyWebSocketService webSocketService;
  private final KcpService kcpChannelService;
  private final ZeroSocketService socketService;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final NetworkWriterStatistic networkWriterStatistic;
  private DataType dataType;
  private boolean initialized;

  private boolean httpServiceInitialized;
  private boolean webSocketServiceInitialized;
  private boolean socketServiceInitialized;
  private boolean kcpChannelServiceInitialized;

  private NetworkServiceImpl(EventManager eventManager) {
    super(eventManager);

    initialized = false;

    httpServiceInitialized = false;
    webSocketServiceInitialized = false;
    socketServiceInitialized = false;
    kcpChannelServiceInitialized = false;

    sessionManager = SessionManagerImpl.newInstance(eventManager);
    networkReaderStatistic = NetworkReaderStatistic.newInstance();
    networkWriterStatistic = NetworkWriterStatistic.newInstance();

    httpService = JettyHttpService.newInstance(eventManager);
    webSocketService = NettyWebSocketServiceImpl.newInstance(eventManager);
    socketService = ZeroSocketServiceImpl.newInstance(eventManager);
    kcpChannelService = KcpServiceImpl.newInstance(eventManager);
  }

  /**
   * Creates a new instance of the network service.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link NetworkService}
   */
  public static NetworkService newInstance(EventManager eventManager) {
    return new NetworkServiceImpl(eventManager);
  }

  @Override
  public void initialize() {
    initializeServices();
    initialized = true;
  }

  private void initializeServices() {
    webSocketService.setSessionManager(sessionManager);
    webSocketService.setNetworkReaderStatistic(networkReaderStatistic);
    webSocketService.setNetworkWriterStatistic(networkWriterStatistic);

    socketService.setSessionManager(sessionManager);
    socketService.setNetworkReaderStatistic(networkReaderStatistic);
    socketService.setNetworkWriterStatistic(networkWriterStatistic);

    kcpChannelService.setSessionManager(sessionManager);
    kcpChannelService.setNetworkReaderStatistic(networkReaderStatistic);
    kcpChannelService.setNetworkWriterStatistic(networkWriterStatistic);

    if (httpServiceInitialized) {
      httpService.initialize();
    }
    if (webSocketServiceInitialized) {
      webSocketService.initialize();
    }
    if (socketServiceInitialized) {
      socketService.initialize();
    }
    if (kcpChannelServiceInitialized) {
      kcpChannelService.initialize();
    }
  }

  @Override
  public void start() {
    httpService.start();
    webSocketService.start();
    socketService.start();
    kcpChannelService.start();
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }
    attemptToShutdown();
  }

  private void attemptToShutdown() {
    httpService.shutdown();
    webSocketService.shutdown();
    socketService.shutdown();
    kcpChannelService.shutdown();
  }

  @Override
  public boolean isActivated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "network";
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHttpConfiguration(int threadPoolSize, int port,
                                   Map<String, HttpServlet> servletMap) {
    httpService.setThreadPoolSize(threadPoolSize);
    httpService.setPort(port);
    httpService.setServletMap(servletMap);
    httpServiceInitialized = (port != 0 && Objects.nonNull(servletMap));
  }

  @Override
  public void setConnectionFilterClass(ConnectionFilter connectionFilter, int maxConnectionsPerIp) {
    connectionFilter.configureMaxConnectionsPerIp(maxConnectionsPerIp);

    webSocketService.setConnectionFilter(connectionFilter);
    socketService.setConnectionFilter(connectionFilter);
    sessionManager.configureConnectionFilter(connectionFilter);
  }

  @Override
  public void setWebSocketConsumerWorkers(int workerSize) {
    webSocketService.setConsumerWorkerSize(workerSize);
  }

  @Override
  public void setWebSocketProducerWorkers(int workerSize) {
    webSocketService.setProducerWorkerSize(workerSize);
  }

  @Override
  public void setWebSocketSenderBufferSize(int bufferSize) {
    webSocketService.setSenderBufferSize(bufferSize);
  }

  @Override
  public void setWebSocketReceiverBufferSize(int bufferSize) {
    webSocketService.setReceiverBufferSize(bufferSize);
  }

  @Override
  public void setWebSocketUsingSsl(boolean usingSsl) {
    webSocketService.setUsingSsl(usingSsl);
  }

  @Override
  public void setSocketAcceptorServerAddress(String serverAddress) {
    socketService.setAcceptorServerAddress(serverAddress);
  }

  @Override
  public void setSocketAcceptorWorkers(int workerSize) {
    socketService.setAcceptorWorkerSize(workerSize);
  }

  @Override
  public void setSocketReaderWorkers(int workerSize) {
    socketService.setReaderWorkerSize(workerSize);
  }

  @Override
  public void setSocketWriterWorkers(int workerSize) {
    socketService.setWriterWorkerSize(workerSize);
  }

  @Override
  public void setSocketAcceptorBufferSize(int bufferSize) {
    socketService.setAcceptorBufferSize(bufferSize);
  }

  @Override
  public void setSocketReaderBufferSize(int bufferSize) {
    socketService.setReaderBufferSize(bufferSize);
  }

  @Override
  public void setSocketWriterBufferSize(int bufferSize) {
    socketService.setWriterBufferSize(bufferSize);
  }

  @Override
  public void setSocketConfiguration(SocketConfiguration tcpSocketConfiguration,
                                     SocketConfiguration udpSocketConfiguration,
                                     SocketConfiguration webSocketConfiguration,
                                     SocketConfiguration kcpSocketConfiguration) {
    if (Objects.nonNull(tcpSocketConfiguration)) {
      socketServiceInitialized = true;
      socketService.setSocketConfiguration(tcpSocketConfiguration, udpSocketConfiguration);
    }

    if (Objects.nonNull(webSocketConfiguration)) {
      webSocketServiceInitialized = true;
      webSocketService.setWebSocketConfiguration(webSocketConfiguration);
    }

    if (Objects.nonNull(kcpSocketConfiguration)) {
      kcpChannelServiceInitialized = true;
      kcpChannelService.setKcpSocketConfiguration(kcpSocketConfiguration);
    }
  }

  @Override
  public void setSessionMaxIdleTimeInSeconds(int seconds) {
    sessionManager.configureMaxIdleTimeInSeconds(seconds);
  }

  @Override
  public void setPacketQueuePolicy(PacketQueuePolicy packetQueuePolicy) {
    sessionManager.configurePacketQueuePolicy(packetQueuePolicy);
  }

  @Override
  public void setPacketQueueSize(int queueSize) {
    sessionManager.configurePacketQueueSize(queueSize);
  }

  @Override
  public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
    socketService.setPacketEncoder(packetEncoder);
  }

  @Override
  public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
    socketService.setPacketDecoder(packetDecoder);
  }

  @Override
  public void setDataType(DataType dataType) {
    this.dataType = dataType;

    socketService.setDataType(dataType);
    webSocketService.setDataType(dataType);
    kcpChannelService.setDataType(dataType);
  }

  @Override
  public SessionManager getSessionManager() {
    return sessionManager;
  }

  @Override
  public NetworkReaderStatistic getNetworkReaderStatistic() {
    return networkReaderStatistic;
  }

  @Override
  public NetworkWriterStatistic getNetworkWriterStatistic() {
    return networkWriterStatistic;
  }

  @Override
  public void write(Response response, boolean markedAsLast) {
    var message = DataUtility.binaryToCollection(dataType, response.getContent());

    var recipientPlayers = response.getRecipientPlayers();
    if (Objects.nonNull(recipientPlayers) && !recipientPlayers.isEmpty()) {
      var playerIterator = recipientPlayers.iterator();
      while (playerIterator.hasNext()) {
        var player = playerIterator.next();
        eventManager.emit(ServerEvent.SEND_MESSAGE_TO_PLAYER, player, message);
      }
    }

    var nonSessionRecipientPlayers = response.getNonSessionRecipientPlayers();
    if (Objects.nonNull(nonSessionRecipientPlayers) && !nonSessionRecipientPlayers.isEmpty()) {
      var nonSessionIterator = nonSessionRecipientPlayers.iterator();
      while (nonSessionIterator.hasNext()) {
        var player = nonSessionIterator.next();
        eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
      }
    }

    var socketSessions = response.getRecipientSocketSessions();
    if (Objects.nonNull(socketSessions)) {
      var packet = createPacket(response, socketSessions, TransportType.TCP);
      packet.setMarkedAsLast(markedAsLast);
      socketService.write(packet);
      socketSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    var datagramSessions = response.getRecipientDatagramSessions();
    if (Objects.nonNull(datagramSessions)) {
      var packet = createPacket(response, datagramSessions, TransportType.UDP);
      socketService.write(packet);
      datagramSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    var kcpSessions = response.getRecipientKcpSessions();
    if (Objects.nonNull(kcpSessions)) {
      var packet = createPacket(response, kcpSessions, TransportType.KCP);
      kcpChannelService.write(packet);
      kcpSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    var webSocketSessions = response.getRecipientWebSocketSessions();
    if (Objects.nonNull(webSocketSessions)) {
      var packet = createPacket(response, webSocketSessions, TransportType.WEB_SOCKET);
      packet.setMarkedAsLast(markedAsLast);
      webSocketService.write(packet);
      webSocketSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }
  }

  private Packet createPacket(Response response, Collection<Session> recipients,
                              TransportType transportType) {
    var packet = PacketImpl.newInstance();
    packet.setData(response.getContent());
    packet.setEncrypted(response.isEncrypted());
    packet.setPriority(response.getPriority());
    packet.setRecipients(recipients);
    packet.setTransportType(transportType);

    return packet;
  }
}
