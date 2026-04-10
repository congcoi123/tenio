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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("Unit Test Cases For NettyWsHandler")
class NettyWsHandlerTest {

  private NettyWsHandler handler;
  private EventManager eventManager;
  private SessionManager sessionManager;

    @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    sessionManager = mock(SessionManager.class);
        ConnectionFilter connectionFilter = mock(ConnectionFilter.class);
    handler = NettyWsHandler.newInstance(
        eventManager, sessionManager, connectionFilter,
        mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class));
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
}
