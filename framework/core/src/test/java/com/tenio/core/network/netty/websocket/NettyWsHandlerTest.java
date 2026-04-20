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

package com.tenio.core.network.netty.websocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.exception.InboundQueueFullException;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For NettyWsHandler")
class NettyWsHandlerTest {

  private NettyWsHandler handler;
  private EventManager eventManager;
  private SessionManager sessionManager;
  private BinaryPacketDecoder binaryPacketDecoder;
  private NetworkReaderStatistic networkReaderStatistic;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    sessionManager = mock(SessionManager.class);
    binaryPacketDecoder = mock(BinaryPacketDecoder.class);
    networkReaderStatistic = mock(NetworkReaderStatistic.class);
    handler = NettyWsHandler.newInstance(
        eventManager, sessionManager, mock(ConnectionFilter.class),
        binaryPacketDecoder, networkReaderStatistic);
  }

  @Test
  @DisplayName("channelInactive with no session closes the channel directly")
  void testChannelInactiveWithNoSessionClosesChannel() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    when(ctx.channel()).thenReturn(channel);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(null);

    assertDoesNotThrow(() -> handler.channelInactive(ctx));

    verify(channel).close();
  }

  @Test
  @DisplayName("channelInactive with inactive session closes the channel directly")
  void testChannelInactiveWithInactiveSessionClosesChannel() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    Session session = mock(Session.class);
    when(ctx.channel()).thenReturn(channel);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.channelInactive(ctx));

    verify(channel).close();
  }

  @Test
  @DisplayName("channelRead with a non-BinaryWebSocketFrame object does nothing")
  void testChannelReadWithNonBinaryFrameDoesNothing() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

    assertDoesNotThrow(() -> handler.channelRead(ctx, "not a frame"));

    verify(eventManager, never()).emit(any(), any());
  }

  @Test
  @DisplayName("channelInactive with activated session closes the session")
  void testChannelInactiveWithActivatedSessionClosesSession() throws IOException {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    Session session = mock(Session.class);
    when(ctx.channel()).thenReturn(channel);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);

    assertDoesNotThrow(() -> handler.channelInactive(ctx));

    verify(session).close(ConnectionDisconnectMode.LOST_IN_READ, PlayerDisconnectMode.CONNECTION_LOST);
  }

  @Test
  @DisplayName("exceptionCaught with session found closes session")
  void testExceptionCaughtWithSessionClosesSession() throws IOException {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    Session session = mock(Session.class);
    Throwable cause = new RuntimeException("ws error");
    when(ctx.channel()).thenReturn(channel);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);

    handler.exceptionCaught(ctx, cause);

    verify(session).close();
  }

  @Test
  @DisplayName("exceptionCaught with no session does not throw")
  void testExceptionCaughtWithNoSessionDoesNotThrow() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    when(ctx.channel()).thenReturn(channel);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(null);

    assertDoesNotThrow(() -> handler.exceptionCaught(ctx, new RuntimeException("no session")));
  }

  @Test
  @DisplayName("channelRead with BinaryWebSocketFrame and activated DONE session enqueues message")
  void testChannelReadWithBinaryFrameActivatedDoneSessionEnqueuesMessage() throws Exception {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);

    when(ctx.channel()).thenReturn(channel);
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.NONE)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(binaryPacketDecoder.decode(any(byte[].class))).thenReturn(message);

    assertDoesNotThrow(() -> handler.channelRead(ctx, frame));

    verify(session).enqueueInbound(message);
    verify(networkReaderStatistic).updateReadBytes(3);
    verify(networkReaderStatistic).updateReadPackets(1);
  }

  @Test
  @DisplayName("channelRead with BinaryWebSocketFrame and activated NONE session emits connection request")
  void testChannelReadWithBinaryFrameActivatedNoneSessionEmitsConnectionRequest() throws Exception {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);

    when(ctx.channel()).thenReturn(channel);
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.NONE)).thenReturn(true);
    when(binaryPacketDecoder.decode(any(byte[].class))).thenReturn(message);

    assertDoesNotThrow(() -> handler.channelRead(ctx, frame));

    verify(eventManager).emit(com.tenio.core.configuration.define.ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
  }

  @Test
  @DisplayName("channelRead with BinaryWebSocketFrame and inactive session returns early")
  void testChannelReadWithBinaryFrameInactiveSessionReturnsEarly() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    Session session = mock(Session.class);

    when(ctx.channel()).thenReturn(channel);
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(false);

    assertDoesNotThrow(() -> handler.channelRead(ctx, frame));

    verify(eventManager, never()).emit(any(), any());
  }

  @Test
  @DisplayName("channelRead with no existing session creates a new session")
  void testChannelReadWithNoExistingSessionCreatesNewSession() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    Session session = mock(Session.class);

    when(ctx.channel()).thenReturn(channel);
    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 1234));
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(null);
    when(sessionManager.createWebSocketSession(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.NONE)).thenReturn(true);

    assertDoesNotThrow(() -> handler.channelRead(ctx, frame));

    verify(sessionManager).createWebSocketSession(channel);
  }

  @Test
  @DisplayName("channelRead with session in DOING state returns early without emitting events")
  void testChannelReadWithDOINGSessionReturnsEarly() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    Session session = mock(Session.class);

    when(ctx.channel()).thenReturn(channel);
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(true);

    assertDoesNotThrow(() -> handler.channelRead(ctx, frame));

    verify(eventManager, never()).emit(any(), any());
  }

  @Test
  @DisplayName("channelInactive with activated session that throws IOException on close does not propagate")
  void testChannelInactiveWithSessionCloseIOExceptionDoesNotPropagate() throws IOException {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    Session session = mock(Session.class);
    when(ctx.channel()).thenReturn(channel);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);
    doThrow(new IOException("close failed")).when(session).close(any(), any());

    assertDoesNotThrow(() -> handler.channelInactive(ctx));
  }

  @Test
  @DisplayName("exceptionCaught with session that throws IOException on close does not propagate")
  void testExceptionCaughtWithSessionCloseIOExceptionDoesNotPropagate() throws IOException {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    Session session = mock(Session.class);
    when(ctx.channel()).thenReturn(channel);
    when(channel.toString()).thenReturn("MockChannel");
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    doThrow(new IOException("close failed")).when(session).close();

    assertDoesNotThrow(() -> handler.exceptionCaught(ctx, new RuntimeException("err")));
  }

  @Test
  @DisplayName("channelRead with null session and refused connection emits WEBSOCKET_CONNECTION_REFUSED and closes channel")
  void testChannelReadWithRefusedConnectionEmitsEventAndClosesChannel() throws Exception {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    ChannelFuture future = mock(ChannelFuture.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    ConnectionFilter filter = mock(ConnectionFilter.class);
    Session newSession = mock(Session.class);

    NettyWsHandler localHandler = NettyWsHandler.newInstance(
        eventManager, sessionManager, filter, binaryPacketDecoder, networkReaderStatistic);

    when(ctx.channel()).thenReturn(channel);
    when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 9999));
    when(channel.close()).thenReturn(future);
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(null);
    // After refused connection, code falls through and creates a session - return inactive session
    when(sessionManager.createWebSocketSession(channel)).thenReturn(newSession);
    when(newSession.isActivated()).thenReturn(false);
    doThrow(new RefusedConnectionAddressException("refused", "127.0.0.1"))
        .when(filter).validateAndAddAddress(any());

    assertDoesNotThrow(() -> localHandler.channelRead(ctx, frame));

    verify(eventManager).emit(eq(ServerEvent.WEBSOCKET_CONNECTION_REFUSED), eq(channel),
        any(RefusedConnectionAddressException.class));
    verify(channel).close();
  }

  @Test
  @DisplayName("channelRead with InboundQueueFullException increments dropped packets statistic")
  void testChannelReadWithInboundQueueFullExceptionUpdatesDroppedStats() throws Exception {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    BinaryWebSocketFrame frame = mock(BinaryWebSocketFrame.class);
    ByteBuf byteBuf = mock(ByteBuf.class);
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);

    when(ctx.channel()).thenReturn(channel);
    when(frame.content()).thenReturn(byteBuf);
    when(byteBuf.readableBytes()).thenReturn(3);
    when(byteBuf.readerIndex()).thenReturn(0);
    when(sessionManager.getSessionByWebSocket(channel)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DOING)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.NONE)).thenReturn(false);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(binaryPacketDecoder.decode(any(byte[].class))).thenReturn(message);
    doThrow(new InboundQueueFullException(100)).when(session).enqueueInbound(message);

    assertDoesNotThrow(() -> handler.channelRead(ctx, frame));

    verify(networkReaderStatistic).updateReadDroppedPackets(1);
  }
}
