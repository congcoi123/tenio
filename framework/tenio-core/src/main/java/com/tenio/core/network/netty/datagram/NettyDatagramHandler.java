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

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.element.CommonObject;
import com.tenio.common.logger.pool.ElementsPool;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.core.event.EventManager;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.netty.BaseNettyHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * In this server, a UDP connection is treated as a sub-connection. That means
 * you need to create one main connection between one client and the server
 * first (a TCP connection). When it's finished, that client can send a request
 * for making a link.
 * 
 * @see BaseNettyHandler
 * 
 * @author kong
 * 
 */
public final class NettyDatagramHandler extends BaseNettyHandler {

	private static final ChannelGroup __datagramChannelWorkers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public NettyDatagramHandler(int connectionIndex, EventManager eventManager,
			ElementsPool<CommonObject> commonObjectPool, ElementsPool<ByteArrayInputStream> byteArrayInputPool,
			Configuration configuration) {
		super(eventManager, commonObjectPool, byteArrayInputPool, connectionIndex, TransportType.UDP);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		__datagramChannelWorkers.add(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (!__datagramChannelWorkers.isEmpty()) {
			__datagramChannelWorkers.clear();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msgRaw) throws Exception {

		// get the message's content
		byte[] content;
		DatagramPacket datagram;
		if (msgRaw instanceof DatagramPacket) {
			// get the packet and sender data, convert it to a bytes' array
			datagram = (DatagramPacket) msgRaw;
			var buffer = datagram.content();
			int readableBytes = buffer.readableBytes();
			content = new byte[readableBytes];
			buffer.readBytes(content);
			buffer.release();
		} else {
			return;
		}

		// retrieve an object from pool
		var msgObject = getCommonObjectPool().get();
		var byteArray = getByteArrayInputPool().get();

		// create a new message
		var message = MsgPackConverter.unserialize(msgObject, byteArray, content);
		content = null;
		if (message == null) {
			// repay
			getCommonObjectPool().repay(msgObject);
			getByteArrayInputPool().repay(byteArray);
			return;
		}

		_channelRead(__datagramChannelWorkers, ctx, message, datagram.sender());

		// repay
		getCommonObjectPool().repay(msgObject);
		getByteArrayInputPool().repay(byteArray);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		_exceptionCaught(ctx, cause);
	}

}
