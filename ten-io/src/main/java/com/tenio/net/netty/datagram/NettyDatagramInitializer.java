/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.net.netty.datagram;

import com.tenio.configuration.BaseConfiguration;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
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

	/**
	 * @see {@link BaseConfiguration}
	 */
	private BaseConfiguration __configuration;

	public NettyDatagramInitializer(BaseConfiguration configuration) {
		__configuration = configuration;
	}

	@Override
	protected void initChannel(DatagramChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();

		// convert each data chunk into a byte array (read-up)
		pipeline.addLast("bytearray-decoder", new ByteArrayDecoder());
		// convert byte array to data chunk (write-down)
		pipeline.addLast("bytearray-encoder", new ByteArrayEncoder());

		// logic handler
		pipeline.addLast("handler", new NettyDatagramHandler(__configuration));
	}

}
