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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
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

  @Test
  @DisplayName("channelRead with HTTP Upgrade request replaces pipeline handler and sends unsupported version response")
  void testChannelReadWithUpgradeRequestReplacesPipelineHandler() {
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel channel = mock(Channel.class);
    ChannelPipeline pipeline = mock(ChannelPipeline.class);
    ChannelFuture future = mock(ChannelFuture.class);

    DefaultHttpRequest request = new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, "/ws");
    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
    request.headers().set(HttpHeaderNames.UPGRADE, "WebSocket");
    request.headers().set(HttpHeaderNames.HOST, "localhost:8080");
    // Use unrecognized WS version so newHandshaker returns null -> sendUnsupportedVersionResponse
    request.headers().set(io.netty.handler.codec.http.HttpHeaderNames.SEC_WEBSOCKET_VERSION, "99");

    when(ctx.channel()).thenReturn(channel);
    when(ctx.pipeline()).thenReturn(pipeline);
    when(channel.alloc()).thenReturn(UnpooledByteBufAllocator.DEFAULT);
    when(channel.writeAndFlush(any())).thenReturn(future);

    assertDoesNotThrow(() -> handShake.channelRead(ctx, request));

    verify(pipeline).replace(any(io.netty.channel.ChannelHandler.class), eq("handler"),
        any(io.netty.channel.ChannelHandler.class));
  }

}
