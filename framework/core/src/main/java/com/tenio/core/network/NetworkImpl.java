/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import com.tenio.core.network.jetty.JettyHttp;
import com.tenio.core.network.kcp.KcpChannel;
import com.tenio.core.network.kcp.KcpChannelImpl;
import com.tenio.core.network.netty.NettyWebSocket;
import com.tenio.core.network.netty.NettyWebSocketImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.ZeroSocket;
import com.tenio.core.network.zero.ZeroSocketImpl;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import jakarta.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Map;

/**
 * The implementation for network service.
 *
 * @see Network
 */
public final class NetworkImpl extends AbstractManager implements Network {

  private final SessionManager sessionManager;
  private final JettyHttp httpService;
  private final NettyWebSocket webSocketService;
  private final KcpChannel kcpChannelService;
  private final ZeroSocket socketService;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final NetworkWriterStatistic networkWriterStatistic;
  private boolean initialized;

  private boolean httpServiceInitialized;
  private boolean webSocketServiceInitialized;
  private boolean socketServiceInitialized;
  private boolean kcpChannelServiceInitialized;

  private NetworkImpl(EventManager eventManager) {
    super(eventManager);

    initialized = false;

    httpServiceInitialized = false;
    webSocketServiceInitialized = false;
    socketServiceInitialized = false;
    kcpChannelServiceInitialized = false;

    sessionManager = SessionManagerImpl.newInstance(eventManager);
    networkReaderStatistic = NetworkReaderStatistic.newInstance();
    networkWriterStatistic = NetworkWriterStatistic.newInstance();

    httpService = JettyHttp.newInstance(eventManager);
    webSocketService = NettyWebSocketImpl.newInstance(eventManager);
    socketService = ZeroSocketImpl.newInstance(eventManager);
    kcpChannelService = KcpChannelImpl.newInstance(eventManager);
  }

  /**
   * Creates a new instance of the network service.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link Network}
   */
  public static Network newInstance(EventManager eventManager) {
    return new NetworkImpl(eventManager);
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

  @Override
  public void activate() {
    httpService.activate();
    webSocketService.activate();
    socketService.activate();
    kcpChannelService.activate();
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
  public int getMaximumStartingTimeInMilliseconds() {
    int httpServiceStartingTime = httpService.getMaximumStartingTimeInMilliseconds();
    int webSocketServiceStartingTime = httpService.getMaximumStartingTimeInMilliseconds();
    int socketServiceStartingTime = socketService.getMaximumStartingTimeInMilliseconds();
    int kcpChannelServiceStartingTime = kcpChannelService.getMaximumStartingTimeInMilliseconds();

    return Math.max(Math.max(httpServiceStartingTime, webSocketServiceStartingTime),
        Math.max(socketServiceStartingTime, kcpChannelServiceStartingTime));
  }

  @Override
  public void setHttpConfiguration(int threadPoolSize, int port,
                                   Map<String, HttpServlet> servletMap) {
    httpService.setThreadPoolSize(threadPoolSize);
    httpService.setPort(port);
    httpService.setServletMap(servletMap);
    httpServiceInitialized = (port != 0 && servletMap != null);
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
  public void setSocketConfigurations(SocketConfiguration tcpSocketConfiguration,
                                      SocketConfiguration udpChannelConfiguration,
                                      SocketConfiguration webSocketConfiguration,
                                      SocketConfiguration kcpSocketConfiguration) {
    if (tcpSocketConfiguration != null) {
      socketServiceInitialized = true;
      socketService.setSocketConfigurations(tcpSocketConfiguration, udpChannelConfiguration);
    }

    if (webSocketConfiguration != null) {
      webSocketServiceInitialized = true;
      webSocketService.setWebSocketConfiguration(webSocketConfiguration);
    }

    if (kcpSocketConfiguration != null) {
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
    webSocketService.setPacketEncoder(packetEncoder);
  }

  @Override
  public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
    socketService.setPacketDecoder(packetDecoder);
    webSocketService.setPacketDecoder(packetDecoder);
  }

  @Override
  public void setDatagramPacketPolicy(DatagramPacketPolicy datagramPacketPolicy) {
    socketService.setDatagramPacketPolicy(datagramPacketPolicy);
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
    var message = response.getContent();

    var recipientPlayers = response.getRecipientPlayers();
    if (recipientPlayers != null && !recipientPlayers.isEmpty()) {
      var playerIterator = recipientPlayers.iterator();
      while (playerIterator.hasNext()) {
        var player = playerIterator.next();
        eventManager.emit(ServerEvent.SEND_MESSAGE_TO_PLAYER, player, message);
      }
    }

    var nonSessionRecipientPlayers = response.getNonSessionRecipientPlayers();
    if (nonSessionRecipientPlayers != null && !nonSessionRecipientPlayers.isEmpty()) {
      var nonSessionIterator = nonSessionRecipientPlayers.iterator();
      while (nonSessionIterator.hasNext()) {
        var player = nonSessionIterator.next();
        eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
      }
    }

    var socketSessions = response.getRecipientSocketSessions();
    if (socketSessions != null) {
      var packet = createPacket(response, socketSessions, TransportType.TCP);
      packet.setMarkedAsLast(markedAsLast);
      socketService.write(packet);
      socketSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    var datagramSessions = response.getRecipientDatagramSessions();
    if (datagramSessions != null) {
      var packet = createPacket(response, datagramSessions, TransportType.UDP);
      socketService.write(packet);
      datagramSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    var kcpSessions = response.getRecipientKcpSessions();
    if (kcpSessions != null) {
      var packet = createPacket(response, kcpSessions, TransportType.KCP);
      kcpChannelService.write(packet);
      kcpSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    var webSocketSessions = response.getRecipientWebSocketSessions();
    if (webSocketSessions != null) {
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
    packet.setDataType(response.getDataType());
    packet.setData(response.getContent().toBinaries());
    packet.needsEncrypted(response.needsEncrypted());
    packet.setGuarantee(response.getGuarantee());
    packet.setRecipients(recipients);
    packet.setTransportType(transportType);

    return packet;
  }
}
