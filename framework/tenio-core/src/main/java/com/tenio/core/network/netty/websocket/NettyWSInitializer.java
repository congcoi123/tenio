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

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.element.CommonObject;
import com.tenio.common.logger.pool.ElementsPool;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.core.event.EventManager;
import com.tenio.core.network.netty.monitoring.GlobalTrafficShapingHandlerCustomize;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * This class for initializing a channel.
 * 
 * @author kong
 * 
 */
public final class NettyWSInitializer extends ChannelInitializer<SocketChannel> {

	private final EventManager __eventManager;
	private final ElementsPool<CommonObject> __commonObjectPool;
	private final ElementsPool<ByteArrayInputStream> __byteArrayInputPool;
	private final GlobalTrafficShapingHandlerCustomize __trafficCounter;
	private final Configuration __configuration;
	private final int __connectionIndex;

	public NettyWSInitializer(int connectionIndex, EventManager eventManager,
			ElementsPool<CommonObject> commonObjectPool,
			ElementsPool<ByteArrayInputStream> byteArrayInputPool,
			GlobalTrafficShapingHandlerCustomize trafficCounter, Configuration configuration) {
		__connectionIndex = connectionIndex;
		__eventManager = eventManager;
		__commonObjectPool = commonObjectPool;
		__byteArrayInputPool = byteArrayInputPool;
		__trafficCounter = trafficCounter;
		__configuration = configuration;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		var pipeline = channel.pipeline();

		// traffic counter
		pipeline.addLast("traffic-counter", __trafficCounter);

		// add http-codec for TCP hand shaker
		pipeline.addLast("httpServerCodec", new HttpServerCodec());

		// the logic handler
		pipeline.addLast("http-handshake", new NettyWSHandShake(__connectionIndex, __eventManager, __commonObjectPool,
				__byteArrayInputPool, __configuration));
	}

}
