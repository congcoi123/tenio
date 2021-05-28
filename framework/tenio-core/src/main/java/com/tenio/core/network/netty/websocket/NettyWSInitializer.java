/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

import javax.net.ssl.SSLEngine;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.ssl.WebSocketSslContext;
import com.tenio.core.network.statistics.NetworkReaderStatistic;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

public final class NettyWSInitializer extends ChannelInitializer<SocketChannel> {

	private final EventManager __eventManager;
	private final SessionManager __sessionManager;
	private final ConnectionFilter __connectionFilter;
	private final NetworkReaderStatistic __networkReaderStatistic;
	private final WebSocketSslContext __sslContext;
	private final boolean __usingSSL;

	public static NettyWSInitializer newInstance(EventManager eventManager, SessionManager sessionManager,
			ConnectionFilter connectionFilter, NetworkReaderStatistic networkReaderStatistic,
			WebSocketSslContext sslContext, boolean usingSSL) {
		return new NettyWSInitializer(eventManager, sessionManager, connectionFilter, networkReaderStatistic,
				sslContext, usingSSL);
	}

	private NettyWSInitializer(EventManager eventManager, SessionManager sessionManager,
			ConnectionFilter connectionFilter, NetworkReaderStatistic networkReaderStatistic,
			WebSocketSslContext sslContext, boolean usingSSL) {
		__eventManager = eventManager;
		__sessionManager = sessionManager;
		__connectionFilter = connectionFilter;
		__networkReaderStatistic = networkReaderStatistic;
		__sslContext = sslContext;
		__usingSSL = usingSSL;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		var pipeline = channel.pipeline();

		// add ssl handler
		if (__usingSSL) {
			SSLEngine engine = __sslContext.getServerContext().createSSLEngine();
			engine.setUseClientMode(false);
			pipeline.addLast("ssl", new SslHandler(engine));
		}

		// add http-codec for TCP hand shaker
		pipeline.addLast("httpServerCodec", new HttpServerCodec());

		// the logic handler
		pipeline.addLast("http-handshake", NettyWSHandShake.newInstance(__eventManager, __sessionManager,
				__connectionFilter, __networkReaderStatistic));
	}

}
