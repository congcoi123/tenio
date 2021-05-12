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
import com.tenio.common.data.elements.CommonObject;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.common.pool.ElementsPool;
import com.tenio.core.events.EventManager;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.netty.BaseNettyHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * Receive all messages sent from clients. It converts serialize data to a
 * system's object for convenience and easy to use. It also handles the logic
 * for the processing of players and connections.
 * 
 * @see BaseNettyHandler
 * 
 * @author kong
 * 
 */
public class NettyWSHandler extends BaseNettyHandler {

	public NettyWSHandler(int connectionIndex, EventManager eventManager, ElementsPool<CommonObject> commonObjectPool,
			ElementsPool<ByteArrayInputStream> byteArrayInputPool, Configuration configuration) {
		super(eventManager, commonObjectPool, byteArrayInputPool, connectionIndex, TransportType.WEB_SOCKET);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
            this.wsChannel = ctx.getChannel();
            String address = this.wsChannel.getRemoteAddress().toString();
            this.connFilter.validateAndAddAddress(address.substring(1, address.indexOf(58)));
            this.sfsSession = this.sessionManager.createWebSocketSession(this);
            this.sessionManager.addSession(this.sfsSession);
       } catch (RefusedAddressException var4) {
            this.logger.warn("Refused connection. " + var4.getMessage());
            ctx.getChannel().close();
            this.logger.warn(ExceptionUtils.getStackTrace(var4));
       } catch (Exception var5) {
            this.logger.warn("Refused connection. " + var5.getMessage());
            ctx.getChannel().close();
            this.logger.warn(ExceptionUtils.getStackTrace(var5));
       }
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		_channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msgRaw) throws Exception {
		// only allow this type of frame
		if (msgRaw instanceof BinaryWebSocketFrame) {
			// convert the BinaryWebSocketFrame to bytes' array
			var buffer = ((BinaryWebSocketFrame) msgRaw).content();
			var bytes = new byte[buffer.readableBytes()];
			buffer.getBytes(buffer.readerIndex(), bytes);
			buffer.release();

			// create request
			IRequest request = new Request();
            Object controllerKey = null;
            requestObject.get();
            requestObject.getShort();
            controllerKey = requestObject.get();
            request.setId(requestObject.getShort());
            request.setContent(requestObject.compact());
            request.setSender(packet.getSender());
            request.setTransportType(packet.getTransportType());
            this.dispatchRequestToController(request, controllerKey);
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		_exceptionCaught(ctx, cause);
	}

}
