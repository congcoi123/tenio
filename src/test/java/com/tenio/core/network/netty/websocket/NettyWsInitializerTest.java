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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.ssl.WebSocketSslContext;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For NettyWsInitializer")
class NettyWsInitializerTest {

  @Test
  @DisplayName("newInstance without SSL creates a non-null initializer")
  void testNewInstanceWithoutSslCreatesNonNull() {
    NettyWsInitializer initializer = assertDoesNotThrow(() ->
        NettyWsInitializer.newInstance(
            mock(EventManager.class),
            mock(SessionManager.class),
            mock(ConnectionFilter.class),
            mock(BinaryPacketDecoder.class),
            mock(NetworkReaderStatistic.class),
            mock(WebSocketSslContext.class),
            false));

    assertNotNull(initializer);
  }

  @Test
  @DisplayName("newInstance with SSL flag creates a non-null initializer")
  void testNewInstanceWithSslCreatesNonNull() {
    NettyWsInitializer initializer = assertDoesNotThrow(() ->
        NettyWsInitializer.newInstance(
            mock(EventManager.class),
            mock(SessionManager.class),
            mock(ConnectionFilter.class),
            mock(BinaryPacketDecoder.class),
            mock(NetworkReaderStatistic.class),
            mock(WebSocketSslContext.class),
            true));

    assertNotNull(initializer);
  }

  @Test
  @DisplayName("initChannel without SSL adds httpServerCodec and http-handshake handlers to pipeline")
  void testInitChannelWithoutSslAddsPipelineHandlers() {
    NettyWsInitializer initializer = NettyWsInitializer.newInstance(
        mock(EventManager.class),
        mock(SessionManager.class),
        mock(ConnectionFilter.class),
        mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class),
        mock(WebSocketSslContext.class),
        false);

    io.netty.channel.socket.SocketChannel channel =
        mock(io.netty.channel.socket.SocketChannel.class);
    ChannelPipeline pipeline = mock(ChannelPipeline.class);
    when(channel.pipeline()).thenReturn(pipeline);
    when(pipeline.addLast(anyString(), any(ChannelHandler.class))).thenReturn(pipeline);

    assertDoesNotThrow(() -> initializer.initChannel(channel));

    verify(pipeline, atLeast(2)).addLast(anyString(), any(ChannelHandler.class));
  }

  @Test
  @DisplayName("initChannel with SSL adds ssl handler plus httpServerCodec and handshake handlers")
  void testInitChannelWithSslAddsSslHandler() {
    SSLEngine sslEngine = mock(SSLEngine.class);
    SSLContext sslCtx = mock(SSLContext.class);
    WebSocketSslContext wsSslContext = mock(WebSocketSslContext.class);
    when(wsSslContext.getServerContext()).thenReturn(sslCtx);
    when(sslCtx.createSSLEngine()).thenReturn(sslEngine);

    NettyWsInitializer initializer = NettyWsInitializer.newInstance(
        mock(EventManager.class),
        mock(SessionManager.class),
        mock(ConnectionFilter.class),
        mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class),
        wsSslContext,
        true);

    io.netty.channel.socket.SocketChannel channel =
        mock(io.netty.channel.socket.SocketChannel.class);
    ChannelPipeline pipeline = mock(ChannelPipeline.class);
    when(channel.pipeline()).thenReturn(pipeline);
    when(pipeline.addLast(anyString(), any(ChannelHandler.class))).thenReturn(pipeline);

    assertDoesNotThrow(() -> initializer.initChannel(channel));

    verify(wsSslContext).getServerContext();
    verify(pipeline, atLeast(3)).addLast(anyString(), any(ChannelHandler.class));
  }
}
