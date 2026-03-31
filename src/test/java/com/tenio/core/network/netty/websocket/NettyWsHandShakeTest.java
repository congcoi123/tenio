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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For NettyWsHandShake")
class NettyWsHandShakeTest {

  private NettyWsHandShake handShake;
  private EventManager eventManager;
  private SessionManager sessionManager;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    sessionManager = mock(SessionManager.class);
    handShake = NettyWsHandShake.newInstance(
        eventManager,
        sessionManager,
        mock(ConnectionFilter.class),
        mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class));
  }

  @Test
  @DisplayName("newInstance creates a non-null handshaker")
  void testNewInstanceCreatesNonNull() {
    assertNotNull(handShake);
  }

  @Test
  @DisplayName("channelRead with a non-HttpRequest object does nothing")
  void testChannelReadWithNonHttpRequestDoesNothing() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

    assertDoesNotThrow(() -> handShake.channelRead(ctx, "not an http request"));

    verifyNoInteractions(ctx);
    verifyNoInteractions(eventManager);
    verifyNoInteractions(sessionManager);
  }

  @Test
  @DisplayName("channelRead with a non-HttpRequest numeric object does nothing")
  void testChannelReadWithIntegerObjectDoesNothing() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

    assertDoesNotThrow(() -> handShake.channelRead(ctx, 42));

    verifyNoInteractions(ctx);
  }
}
