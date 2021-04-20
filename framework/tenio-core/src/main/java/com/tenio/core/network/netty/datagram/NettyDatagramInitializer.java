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
package com.tenio.core.network.netty.datagram;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.CommonObject;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.pool.IElementsPool;
import com.tenio.core.event.IEventManager;
import com.tenio.core.monitoring.traffic.GlobalTrafficShapingHandlerCustomize;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * This class for initializing a channel.
 * 
 * @author kong
 * 
 */
public final class NettyDatagramInitializer extends ChannelInitializer<DatagramChannel> {

	private final IEventManager __eventManager;
	private final IElementsPool<CommonObject> __commonObjectPool;
	private final IElementsPool<ByteArrayInputStream> __byteArrayInputPool;
	private final GlobalTrafficShapingHandlerCustomize __trafficCounter;
	private final IConfiguration __configuration;
	private final int __connectionIndex;

	public NettyDatagramInitializer(int connectionIndex, IEventManager eventManager,
			IElementsPool<CommonObject> commonObjectPool, IElementsPool<ByteArrayInputStream> byteArrayInputPool,
			GlobalTrafficShapingHandlerCustomize trafficCounter, IConfiguration configuration) {
		__connectionIndex = connectionIndex;
		__eventManager = eventManager;
		__commonObjectPool = commonObjectPool;
		__byteArrayInputPool = byteArrayInputPool;
		__trafficCounter = trafficCounter;
		__configuration = configuration;
	}

	@Override
	protected void initChannel(DatagramChannel channel) throws Exception {
		var pipeline = channel.pipeline();

		// converts each data chunk into a byte array (read-up)
		pipeline.addLast("bytearray-decoder", new ByteArrayDecoder());
		// converts bytes' array to data chunk (write-down)
		pipeline.addLast("bytearray-encoder", new ByteArrayEncoder());

		pipeline.addLast("outbound-queue", new WriteQueueOutboundChannelHandler());

		// traffic counter
		pipeline.addLast("traffic-counter", __trafficCounter);

		// the logic handler
		pipeline.addLast("handler", new NettyDatagramHandler(__connectionIndex, __eventManager, __commonObjectPool,
				__byteArrayInputPool, __configuration));
	}

}
