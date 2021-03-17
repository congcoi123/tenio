/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.core.network.netty.ws;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.MessageObject;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.pool.IElementPool;
import com.tenio.core.event.IEventManager;
import com.tenio.core.network.netty.GlobalTrafficShapingHandlerCustomize;

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

	private final IEventManager __eventManager;
	private final IElementPool<MessageObject> __msgObjectPool;
	private final IElementPool<ByteArrayInputStream> __byteArrayPool;
	private final GlobalTrafficShapingHandlerCustomize __trafficCounter;
	private final IConfiguration __configuration;
	private final int __index;

	public NettyWSInitializer(int index, IEventManager eventManager, IElementPool<MessageObject> msgObjectPool,
			IElementPool<ByteArrayInputStream> byteArrayPool, GlobalTrafficShapingHandlerCustomize trafficCounter,
			IConfiguration configuration) {
		__index = index;
		__eventManager = eventManager;
		__msgObjectPool = msgObjectPool;
		__byteArrayPool = byteArrayPool;
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
		pipeline.addLast("http-handshake",
				new NettyWSHandShake(__index, __eventManager, __msgObjectPool, __byteArrayPool, __configuration));
	}

}
