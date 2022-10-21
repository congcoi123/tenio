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

package com.tenio.core.network;

import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.define.data.PathConfig;
import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.entity.session.manager.SessionManagerImpl;
import com.tenio.core.network.jetty.JettyHttpService;
import com.tenio.core.network.netty.NettyWebSocketService;
import com.tenio.core.network.netty.NettyWebSocketServiceImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.ZeroSocketService;
import com.tenio.core.network.zero.ZeroSocketServiceImpl;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The implementation for network service.
 *
 * @see NetworkService
 */
public final class NetworkServiceImpl extends AbstractManager implements NetworkService {

  private final SessionManager sessionManager;
  private JettyHttpService httpService;
  private NettyWebSocketService webSocketService;
  private ZeroSocketService socketService;
  private DataType dataType;
  private NetworkReaderStatistic networkReaderStatistic;
  private NetworkWriterStatistic networkWriterStatistic;

  private boolean initialized;

  private boolean httpServiceInitialized;
  private boolean webSocketServiceInitialized;
  private boolean socketServiceInitialized;

  private NetworkServiceImpl(EventManager eventManager) {
    super(eventManager);

    initialized = false;

    httpServiceInitialized = false;
    webSocketServiceInitialized = false;
    socketServiceInitialized = false;

    sessionManager = SessionManagerImpl.newInstance(eventManager);
    networkReaderStatistic = NetworkReaderStatistic.newInstance();
    networkWriterStatistic = NetworkWriterStatistic.newInstance();

    httpService = JettyHttpService.newInstance(eventManager);
    webSocketService = NettyWebSocketServiceImpl.newInstance(eventManager);
    socketService = ZeroSocketServiceImpl.newInstance(eventManager);
  }

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

    if (httpServiceInitialized) {
      httpService.initialize();
    }
    if (webSocketServiceInitialized) {
      webSocketService.initialize();
    }
    if (socketServiceInitialized) {
      socketService.initialize();
    }
  }

  @Override
  public void start() {
    httpService.start();
    webSocketService.start();
    socketService.start();
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

    destroy();
  }

  private void destroy() {
    httpService = null;
    webSocketService = null;
    socketService = null;

    networkReaderStatistic = null;
    networkWriterStatistic = null;
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
  public void setHttpPort(int port) {
    httpService.setPort(port);
  }

  @Override
  public void setHttpPathConfigs(List<PathConfig> pathConfigs) {
    if (Objects.isNull(pathConfigs)) {
      return;
    }
    httpService.setPathConfigs(pathConfigs);
    httpServiceInitialized = true;
  }

  @Override
  public void setConnectionFilterClass(Class<? extends ConnectionFilter> clazz,
                                       int maxConnectionsPerIp)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException {
    var connectionFilter = clazz.getDeclaredConstructor().newInstance();
    connectionFilter.setMaxConnectionsPerIp(maxConnectionsPerIp);

    webSocketService.setConnectionFilter(connectionFilter);
    socketService.setConnectionFilter(connectionFilter);
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
  public void setSocketAcceptorAmountUdpWorkers(int amountUdpWorkers) {
    socketService.setAcceptorAmountUdpWorkers(amountUdpWorkers);
  }

  @Override
  public void setSocketAcceptorWorkers(int workerSize) {
    socketService.setAcceptorWorkerSize(workerSize);
  }

  @Override
  public void setSocketAcceptorEnabledKcp(boolean enabledKcp) {
    socketService.setAcceptorEnabledKcp(enabledKcp);
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
  public void setSocketConfigs(List<SocketConfig> socketConfigs) {
    if (containsSocketPort(socketConfigs)) {
      socketServiceInitialized = true;
      socketService.setSocketConfigs(socketConfigs);
    }

    if (containsWebSocketPort(socketConfigs)) {
      webSocketServiceInitialized = true;
      webSocketService.setWebSocketConfig(socketConfigs.stream()
          .filter(socketConfig -> socketConfig.getType() == TransportType.WEB_SOCKET).findFirst()
          .get());
    }
  }

  @Override
  public void setSessionEnabledKcp(boolean enabledKcp) {
    sessionManager.setEnabledKcp(enabledKcp);
  }

  private boolean containsSocketPort(List<SocketConfig> socketConfigs) {
    return socketConfigs.stream()
        .anyMatch(socketConfig -> socketConfig.getType() == TransportType.TCP
            || socketConfig.getType() == TransportType.UDP);
  }

  private boolean containsWebSocketPort(List<SocketConfig> socketConfigs) {
    return socketConfigs.stream()
        .anyMatch(socketConfig -> socketConfig.getType() == TransportType.WEB_SOCKET);
  }

  @Override
  public void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException {
    sessionManager.setPacketQueuePolicy(clazz);
  }

  @Override
  public void setPacketQueueSize(int queueSize) {
    sessionManager.setPacketQueueSize(queueSize);
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
  public void write(Response response) {
    var data = DataUtility.binaryToCollection(dataType, response.getContent());
    var message = ServerMessage.newInstance().setData(data);

    var playerIterator = response.getRecipientPlayers().iterator();
    while (playerIterator.hasNext()) {
      var player = playerIterator.next();
      eventManager.emit(ServerEvent.SEND_MESSAGE_TO_PLAYER, player, message);
    }

    var nonSessionPlayers = response.getNonSessionRecipientPlayers();
    if (Objects.nonNull(nonSessionPlayers)) {
      var nonSessionIterator = nonSessionPlayers.iterator();
      while (nonSessionIterator.hasNext()) {
        var player = nonSessionIterator.next();
        eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
      }
    }

    var socketSessions = response.getRecipientSocketSessions();
    var datagramSessions = response.getRecipientDatagramSessions();
    var webSocketSessions = response.getRecipientWebSocketSessions();

    if (Objects.nonNull(socketSessions)) {
      var packet = createPacket(response, socketSessions, TransportType.TCP);
      socketService.write(packet);
      socketSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    if (Objects.nonNull(datagramSessions)) {
      var packet = createPacket(response, datagramSessions, TransportType.UDP);
      socketService.write(packet);
      datagramSessions.forEach(
          session -> eventManager.emit(ServerEvent.SESSION_WRITE_MESSAGE, session, packet));
    }

    if (Objects.nonNull(webSocketSessions)) {
      var packet = createPacket(response, webSocketSessions, TransportType.WEB_SOCKET);
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
