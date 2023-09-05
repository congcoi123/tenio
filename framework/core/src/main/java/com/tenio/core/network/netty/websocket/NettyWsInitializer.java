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

package com.tenio.core.network.netty.websocket;

import com.tenio.common.data.DataType;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.ssl.WebSocketSslContext;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLEngine;

/**
 * An initialization for the Netty websockets.
 */
public final class NettyWsInitializer extends ChannelInitializer<SocketChannel> {

  private final EventManager eventManager;
  private final SessionManager sessionManager;
  private final ConnectionFilter connectionFilter;
  private final DataType dataType;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final WebSocketSslContext sslContext;
  private final boolean usingSsl;

  private NettyWsInitializer(EventManager eventManager, SessionManager sessionManager,
                             ConnectionFilter connectionFilter, DataType dataType,
                             NetworkReaderStatistic networkReaderStatistic,
                             WebSocketSslContext sslContext, boolean usingSsl) {
    this.eventManager = eventManager;
    this.sessionManager = sessionManager;
    this.connectionFilter = connectionFilter;
    this.dataType = dataType;
    this.networkReaderStatistic = networkReaderStatistic;
    this.sslContext = sslContext;
    this.usingSsl = usingSsl;
  }

  /**
   * Initialization.
   *
   * @param eventManager           the event manager
   * @param sessionManager         the sessin manager
   * @param connectionFilter       the connection filter
   * @param dataType               the {@link DataType}
   * @param networkReaderStatistic the network reader statistic
   * @param sslContext             the ssl context
   * @param usingSsl               is using ssl or not
   * @return an instance
   */
  public static NettyWsInitializer newInstance(EventManager eventManager,
                                               SessionManager sessionManager,
                                               ConnectionFilter connectionFilter,
                                               DataType dataType,
                                               NetworkReaderStatistic networkReaderStatistic,
                                               WebSocketSslContext sslContext, boolean usingSsl) {
    return new NettyWsInitializer(eventManager, sessionManager, connectionFilter, dataType,
        networkReaderStatistic, sslContext, usingSsl);
  }

  @Override
  protected void initChannel(SocketChannel channel) {
    var pipeline = channel.pipeline();

    // add ssl handler
    if (usingSsl) {
      SSLEngine engine = sslContext.getServerContext().createSSLEngine();
      engine.setUseClientMode(false);
      pipeline.addLast("ssl", new SslHandler(engine));
    }

    // add http-codec for TCP handshake
    pipeline.addLast("httpServerCodec", new HttpServerCodec());

    // the logic handler
    pipeline.addLast("http-handshake",
        NettyWsHandShake.newInstance(eventManager, sessionManager, connectionFilter,
            dataType, networkReaderStatistic));
  }
}
