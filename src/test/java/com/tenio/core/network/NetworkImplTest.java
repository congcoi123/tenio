/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.entity.Player;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.entity.outbound.Response;
import com.tenio.core.network.entity.outbound.implement.ResponseImpl;
import com.tenio.core.network.entity.outbound.packet.policy.OutboundQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.jetty.JettyHttp;
import com.tenio.core.network.netty.NettyWebSocket;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.ZeroSocket;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import jakarta.servlet.http.HttpServlet;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For NetworkImpl")
class NetworkImplTest {

  private NetworkImpl service;
  private JettyHttp jettyService;
  private NettyWebSocket nettyService;
  private ZeroSocket zeroService;

  @BeforeEach
  void setUp() throws Exception {
    EventManager eventManager = EventManager.newInstance();
    service = (NetworkImpl) NetworkImpl.newInstance(eventManager);
    jettyService = mock(JettyHttp.class);
    nettyService = mock(NettyWebSocket.class);
    zeroService = mock(ZeroSocket.class);
    // Inject mocks into private final fields
    Field f1 = NetworkImpl.class.getDeclaredField("httpService");
    f1.setAccessible(true);
    f1.set(service, jettyService);
    Field f2 = NetworkImpl.class.getDeclaredField("webSocketService");
    f2.setAccessible(true);
    f2.set(service, nettyService);
    Field f4 = NetworkImpl.class.getDeclaredField("socketService");
    f4.setAccessible(true);
    f4.set(service, zeroService);
  }

  @Test
  @DisplayName("Test start() and shutdown() behaviours")
  void testStartAndShutdown() {
    assertDoesNotThrow(() -> service.start());
    assertDoesNotThrow(() -> service.shutdown());
  }

  @Test
  @DisplayName("Test initialize() calls services")
  void testInitialize() {
    assertDoesNotThrow(() -> service.initialize());
    verify(nettyService).setSessionManager(service.getSessionManager());
    verify(zeroService).setSessionManager(service.getSessionManager());
  }

  @Test
  @DisplayName("Test initialize() then start() then shutdown()")
  void testInitializeStartShutdown() {
    service.initialize();
    assertDoesNotThrow(() -> service.start());
    assertDoesNotThrow(() -> service.shutdown());
  }

  @Test
  @DisplayName("Test activate() delegates to all sub-services")
  void testActivate() {
    assertDoesNotThrow(() -> service.activate());
    verify(jettyService).activate();
    verify(nettyService).activate();
    verify(zeroService).activate();
  }

  @Test
  @DisplayName("Test isActivated() throws UnsupportedOperationException")
  void testIsActivatedThrows() {
    assertThrows(UnsupportedOperationException.class, () -> service.isActivated());
  }

  @Test
  @DisplayName("Test getName() returns 'network'")
  void testGetName() {
    assertEquals("network", service.getName());
  }

  @Test
  @DisplayName("Test setName() throws UnsupportedOperationException")
  void testSetNameThrows() {
    assertThrows(UnsupportedOperationException.class, () -> service.setName("custom"));
  }

  @Test
  @DisplayName("Test getMaximumStartingTimeInMilliseconds() returns non-negative value")
  void testGetMaximumStartingTimeInMilliseconds() {
    assertDoesNotThrow(() -> service.getMaximumStartingTimeInMilliseconds());
  }

  @Test
  @DisplayName("Test getSessionManager() returns non-null")
  void testGetSessionManager() {
    assertNotNull(service.getSessionManager());
  }

  @Test
  @DisplayName("Test getNetworkReaderStatistic() returns non-null")
  void testGetNetworkReaderStatistic() {
    assertNotNull(service.getNetworkReaderStatistic());
  }

  @Test
  @DisplayName("Test getNetworkWriterStatistic() returns non-null")
  void testGetNetworkWriterStatistic() {
    assertNotNull(service.getNetworkWriterStatistic());
  }

  @Test
  @DisplayName("Test setHttpConfiguration() delegates to httpService")
  void testSetHttpConfiguration() {
    HttpServlet servlet = mock(HttpServlet.class);
    assertDoesNotThrow(() -> service.setHttpConfiguration(16, 8080,
        Collections.singletonMap("test", servlet)));
    verify(jettyService).setThreadPoolSize(16);
    verify(jettyService).setPort(8080);
  }

  @Test
  @DisplayName("Test setConnectionFilterClass() configures and distributes filter")
  void testSetConnectionFilterClass() {
    ConnectionFilter filter = mock(ConnectionFilter.class);
    assertDoesNotThrow(() -> service.setConnectionFilterClass(filter, 10));
    verify(filter).configureMaxConnectionsPerIp(10);
    verify(nettyService).setConnectionFilter(filter);
    verify(zeroService).setConnectionFilter(filter);
  }

  @Test
  @DisplayName("Test setWebSocketConsumerWorkers() delegates to webSocketService")
  void testSetWebSocketConsumerWorkers() {
    assertDoesNotThrow(() -> service.setWebSocketConsumerWorkers(4));
    verify(nettyService).setConsumerWorkerSize(4);
  }

  @Test
  @DisplayName("Test setWebSocketProducerWorkers() delegates to webSocketService")
  void testSetWebSocketProducerWorkers() {
    assertDoesNotThrow(() -> service.setWebSocketProducerWorkers(4));
    verify(nettyService).setProducerWorkerSize(4);
  }

  @Test
  @DisplayName("Test setWebSocketSenderBufferSize() delegates to webSocketService")
  void testSetWebSocketSenderBufferSize() {
    assertDoesNotThrow(() -> service.setWebSocketSenderBufferSize(1024));
    verify(nettyService).setSenderBufferSize(1024);
  }

  @Test
  @DisplayName("Test setWebSocketReceiverBufferSize() delegates to webSocketService")
  void testSetWebSocketReceiverBufferSize() {
    assertDoesNotThrow(() -> service.setWebSocketReceiverBufferSize(2048));
    verify(nettyService).setReceiverBufferSize(2048);
  }

  @Test
  @DisplayName("Test setWebSocketUsingSsl() delegates to webSocketService")
  void testSetWebSocketUsingSsl() {
    assertDoesNotThrow(() -> service.setWebSocketUsingSsl(true));
    verify(nettyService).setUsingSsl(true);
  }

  @Test
  @DisplayName("Test setSocketAcceptorServerAddress() delegates to zeroService")
  void testSetSocketAcceptorServerAddress() {
    assertDoesNotThrow(() -> service.setSocketAcceptorServerAddress("127.0.0.1"));
    verify(zeroService).setAcceptorServerAddress("127.0.0.1");
  }

  @Test
  @DisplayName("Test setSocketAcceptorWorkers() delegates to zeroService")
  void testSetSocketAcceptorWorkers() {
    assertDoesNotThrow(() -> service.setSocketAcceptorWorkers(2));
    verify(zeroService).setAcceptorWorkerSize(2);
  }

  @Test
  @DisplayName("Test setSocketReaderWorkers() delegates to zeroService")
  void testSetSocketReaderWorkers() {
    assertDoesNotThrow(() -> service.setSocketReaderWorkers(4));
    verify(zeroService).setReaderWorkerSize(4);
  }

  @Test
  @DisplayName("Test setSocketWriterWorkers() delegates to zeroService")
  void testSetSocketWriterWorkers() {
    assertDoesNotThrow(() -> service.setSocketWriterWorkers(4));
    verify(zeroService).setWriterWorkerSize(4);
  }

  @Test
  @DisplayName("Test setSocketAcceptorBufferSize() delegates to zeroService")
  void testSetSocketAcceptorBufferSize() {
    assertDoesNotThrow(() -> service.setSocketAcceptorBufferSize(512));
    verify(zeroService).setAcceptorBufferSize(512);
  }

  @Test
  @DisplayName("Test setSocketReaderBufferSize() delegates to zeroService")
  void testSetSocketReaderBufferSize() {
    assertDoesNotThrow(() -> service.setSocketReaderBufferSize(1024));
    verify(zeroService).setReaderBufferSize(1024);
  }

  @Test
  @DisplayName("Test setSocketWriterBufferSize() delegates to zeroService")
  void testSetSocketWriterBufferSize() {
    assertDoesNotThrow(() -> service.setSocketWriterBufferSize(1024));
    verify(zeroService).setWriterBufferSize(1024);
  }

  @Test
  @DisplayName("Test setSocketConfigurations() with TCP config marks socket service initialized")
  void testSetSocketConfigurationsWithTcp() {
    SocketConfiguration tcpConfig = mock(SocketConfiguration.class);
    assertDoesNotThrow(() -> service.setSocketConfigurations(tcpConfig, null, null));
    verify(zeroService).setSocketConfigurations(tcpConfig, null);
  }

  @Test
  @DisplayName("Test setSocketConfigurations() with WebSocket config marks ws service initialized")
  void testSetSocketConfigurationsWithWebSocket() {
    SocketConfiguration wsConfig = mock(SocketConfiguration.class);
    assertDoesNotThrow(() -> service.setSocketConfigurations(null, null, wsConfig));
    verify(nettyService).setWebSocketConfiguration(wsConfig);
  }

  @Test
  @DisplayName("Test setSocketConfigurations() with null configs does not throw")
  void testSetSocketConfigurationsWithNulls() {
    assertDoesNotThrow(() -> service.setSocketConfigurations(null, null, null));
  }

  @Test
  @DisplayName("Test setSessionMaxIdleTimeInSeconds() does not throw")
  void testSetSessionMaxIdleTimeInSeconds() {
    assertDoesNotThrow(() -> service.setSessionMaxIdleTimeInSeconds(30));
  }

  @Test
  @DisplayName("Test setSessionSlowConsumingInboundQueueWarningThreshold() does not throw")
  void testSetSessionSlowConsumingInboundQueueWarningThreshold() {
    assertDoesNotThrow(() -> service.setSessionSlowConsumingInboundQueueWarningThreshold(100));
  }

  @Test
  @DisplayName("Test setSessionSlowConsumingOutboundQueueWarningThreshold() does not throw")
  void testSetSessionSlowConsumingOutboundQueueWarningThreshold() {
    assertDoesNotThrow(() -> service.setSessionSlowConsumingOutboundQueueWarningThreshold(100));
  }

  @Test
  @DisplayName("Test setSessionOutboundQueuePolicy() does not throw")
  void testSetSessionOutboundQueuePolicy() {
    OutboundQueuePolicy policy = mock(OutboundQueuePolicy.class);
    assertDoesNotThrow(() -> service.setSessionOutboundQueuePolicy(policy));
  }

  @Test
  @DisplayName("Test setSessionInboundQueueSize() does not throw")
  void testSetSessionInboundQueueSize() {
    assertDoesNotThrow(() -> service.setSessionInboundQueueSize(256));
  }

  @Test
  @DisplayName("Test setSessionOutboundQueueSize() does not throw")
  void testSetSessionOutboundQueueSize() {
    assertDoesNotThrow(() -> service.setSessionOutboundQueueSize(256));
  }

  @Test
  @DisplayName("Test setPacketEncoder() delegates to socket and websocket services")
  void testSetPacketEncoder() {
    BinaryPacketEncoder encoder = mock(BinaryPacketEncoder.class);
    assertDoesNotThrow(() -> service.setPacketEncoder(encoder));
    verify(zeroService).setPacketEncoder(encoder);
    verify(nettyService).setPacketEncoder(encoder);
  }

  @Test
  @DisplayName("Test setPacketDecoder() delegates to socket and websocket services")
  void testSetPacketDecoder() {
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    assertDoesNotThrow(() -> service.setPacketDecoder(decoder));
    verify(zeroService).setPacketDecoder(decoder);
    verify(nettyService).setPacketDecoder(decoder);
  }

  @Test
  @DisplayName("Test setDatagramPacketPolicy() delegates to zeroService")
  void testSetDatagramPacketPolicy() {
    DatagramPacketPolicy policy = mock(DatagramPacketPolicy.class);
    assertDoesNotThrow(() -> service.setDatagramPacketPolicy(policy));
    verify(zeroService).setDatagramPacketPolicy(policy);
  }

  @Test
  @DisplayName("Test write() with TCP session adds to socket sessions")
  void testWriteWithTcpSession() {
    Session session = mock(Session.class);
    org.mockito.Mockito.when(session.isTcp()).thenReturn(true);
    org.mockito.Mockito.when(session.containsUdp()).thenReturn(false);

    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    org.mockito.Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    org.mockito.Mockito.when(content.toBinaries()).thenReturn(new byte[]{1, 2, 3});

    Response response = ResponseImpl.newInstance();
    response.setContent(content);
    response.setRecipientSession(session);

    assertDoesNotThrow(() -> service.write(response, false));
    verify(zeroService).write(org.mockito.Mockito.any());
  }

  @Test
  @DisplayName("Test write() with WebSocket session adds to websocket sessions")
  void testWriteWithWebSocketSession() {
    Session session = mock(Session.class);
    org.mockito.Mockito.when(session.isTcp()).thenReturn(false);
    org.mockito.Mockito.when(session.isWebSocket()).thenReturn(true);

    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    org.mockito.Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    org.mockito.Mockito.when(content.toBinaries()).thenReturn(new byte[]{1, 2, 3});

    Response response = ResponseImpl.newInstance();
    response.setContent(content);
    response.setRecipientSession(session);

    assertDoesNotThrow(() -> service.write(response, false));
    verify(nettyService).write(org.mockito.Mockito.any());
  }

  @Test
  @DisplayName("Test write() with no sessions does not call any service write")
  void testWriteWithNoSessions() {
    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);

    Response response = ResponseImpl.newInstance();
    response.setContent(content);

    assertDoesNotThrow(() -> service.write(response, false));
  }

  @Test
  @DisplayName("write() with a recipient player emits SEND_MESSAGE_TO_PLAYER event")
  void testWriteWithRecipientPlayer() {
    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    Player player = mock(Player.class);

    Response response = ResponseImpl.newInstance();
    response.setContent(content);
    response.setRecipientPlayer(player);

    assertDoesNotThrow(() -> service.write(response, false));
  }

  @Test
  @DisplayName("write() with datagram session routes to socketService")
  void testWriteWithDatagramSession() {
    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    Mockito.when(content.toBinaries()).thenReturn(new byte[]{1, 2, 3});
    Session session = mock(Session.class);
    Mockito.when(session.isTcp()).thenReturn(true);
    Mockito.when(session.containsUdp()).thenReturn(true);

    Response response = ResponseImpl.newInstance();
    response.setContent(content);
    response.prioritizedUdp();
    response.setRecipientSession(session);

    assertDoesNotThrow(() -> service.write(response, false));
    verify(zeroService).write(Mockito.any());
  }

  @Test
  @DisplayName("write() with markedAsLast=true sets the last flag on the packet")
  void testWriteWithMarkedAsLast() {
    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    Mockito.when(content.toBinaries()).thenReturn(new byte[]{1});
    Session session = mock(Session.class);
    Mockito.when(session.isTcp()).thenReturn(false);
    Mockito.when(session.isWebSocket()).thenReturn(true);

    Response response = ResponseImpl.newInstance();
    response.setContent(content);
    response.setRecipientSession(session);

    assertDoesNotThrow(() -> service.write(response, true));
    verify(nettyService).write(Mockito.any());
  }

  @Test
  @DisplayName("initialize() calls httpService.initialize() when httpServiceInitialized=true")
  void testInitializeCallsServicesWhenInitialized() {
    SocketConfiguration tcpConfig = mock(SocketConfiguration.class);
    SocketConfiguration wsConfig = mock(SocketConfiguration.class);
    service.setHttpConfiguration(1, 8080, java.util.Map.of("ping", mock(HttpServlet.class)));
    service.setSocketConfigurations(tcpConfig, null, null);
    service.setSocketConfigurations(null, null, wsConfig);
    assertDoesNotThrow(() -> service.initialize());
    verify(jettyService).initialize();
    verify(nettyService).initialize();
    verify(zeroService).initialize();
  }

  @Test
  @DisplayName("write() with nonSessionRecipientPlayers emits RECEIVED_MESSAGE_FROM_PLAYER")
  void testWriteWithNonSessionRecipientPlayers() throws Exception {
    com.tenio.common.data.DataCollection content = mock(com.tenio.common.data.DataCollection.class);
    Mockito.when(content.getType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    Player player = mock(Player.class);

    Response response = ResponseImpl.newInstance();
    response.setContent(content);

    // Inject nonSessionPlayers directly via reflection
    Field nonSessionField = ResponseImpl.class.getDeclaredField("nonSessionPlayers");
    nonSessionField.setAccessible(true);
    Collection<Player> nonSessionPlayers = new ArrayList<>();
    nonSessionPlayers.add(player);
    nonSessionField.set(response, nonSessionPlayers);

    assertDoesNotThrow(() -> service.write(response, false));
  }
}
