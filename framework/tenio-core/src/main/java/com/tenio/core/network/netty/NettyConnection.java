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
package com.tenio.core.network.netty;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.ThreadSafe;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.tenio.common.element.CommonObject;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.configuration.define.TransportType;
import com.tenio.core.event.IEventManager;
import com.tenio.core.network.Connection;
import com.tenio.core.network.IConnection;
import com.tenio.core.network.netty.option.NettyConnectionOption;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;

/**
 * Use <a href="https://netty.io/">Netty</a> to create a connection
 * instance @see {@link Connection}
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class NettyConnection extends Connection {

	private final int DATAGRAM_WORKERS_SIZE;

	/**
	 * @see Channel
	 */
	@GuardedBy("this")
	private final Channel __channel;
	/**
	 * Save the client's address, in Datagram connection it is used for saving as a
	 * key of the {@link #getUsername()}
	 */
	@GuardedBy("this")
	private InetSocketAddress __remoteAddress;
	@GuardedBy("this")
	private List<Channel> __datagramChannelWorkers;
	private ReentrantLock __counterDatagramLock;
	private volatile int __roundRobinCounter;

	private NettyConnection(int connectionIndex, IEventManager eventManager, TransportType transportType,
			Channel channel, ChannelGroup datagramChannelWorkers) {
		super(eventManager, transportType, connectionIndex);
		__channel = channel;
		// set fixed address in a TCP and WebSocket instance
		// in case of Datagram connection, this value will be set later (when you
		// receive a message from client)
		// in the Datagram connection there is only one channel existed
		if (!isType(TransportType.UDP)) {
			setAddress(((InetSocketAddress) __channel.remoteAddress()).toString());
			datagramChannelWorkers = null;
		} else {
			__counterDatagramLock = new ReentrantLock();
		}

		if (datagramChannelWorkers != null) {
			__roundRobinCounter = 0;
			__datagramChannelWorkers = Collections.synchronizedList(new ArrayList<Channel>());
			datagramChannelWorkers.forEach(ch -> {
				__datagramChannelWorkers.add(ch);
			});
			DATAGRAM_WORKERS_SIZE = __datagramChannelWorkers.size();
		} else {
			DATAGRAM_WORKERS_SIZE = 0;
		}

	}

	public static NettyConnection newInstance(int connectionIndex, IEventManager eventManager,
			TransportType transportType, Channel channel, ChannelGroup datagramChannelWorkers) {
		return new NettyConnection(connectionIndex, eventManager, transportType, channel, datagramChannelWorkers);
	}

	@Override
	public void send(CommonObject message) {
		if (isType(TransportType.TCP)) {
			__channel.writeAndFlush(MsgPackConverter.serialize(message));
		} else if (isType(TransportType.WEB_SOCKET)) {
			__channel.writeAndFlush(
					new BinaryWebSocketFrame(Unpooled.wrappedBuffer(MsgPackConverter.serialize(message))));
		} else if (isType(TransportType.UDP)) {
			if (__remoteAddress != null) {
				if (DATAGRAM_WORKERS_SIZE > 1) {
					__counterDatagramLock.lock();
					if (__roundRobinCounter >= DATAGRAM_WORKERS_SIZE) {
						__roundRobinCounter = 0;
					}
					
					var channel = __datagramChannelWorkers.get(__roundRobinCounter);
					channel.writeAndFlush(new DatagramPacket(
							Unpooled.wrappedBuffer(MsgPackConverter.serialize(message)), __remoteAddress));

					__roundRobinCounter++;
					__counterDatagramLock.unlock();
				} else {
					__channel.writeAndFlush(new DatagramPacket(
							Unpooled.wrappedBuffer(MsgPackConverter.serialize(message)), __remoteAddress));
				}
			}
		}
	}

	@Override
	public synchronized void close() {
		// this channel will be closed in the future
		__channel.close();
		// need to push event now
		getEventManager().getInternal().emit(InternalEvent.CONNECTION_WAS_CLOSED_MANUALLY, getPlayerName());
	}

	@Override
	public IConnection getThis() {
		if (isType(TransportType.UDP)) {
			return (IConnection) __channel.attr(AttributeKey.valueOf(getAddress())).get();
		}
		return __channel.attr(NettyConnectionOption.CONNECTION).get();
	}

	@Override
	public synchronized void setThis() {
		if (isType(TransportType.UDP)) {
			__channel.attr(AttributeKey.valueOf(getAddress())).set(this);
		} else {
			__channel.attr(NettyConnectionOption.CONNECTION).set(this);
		}
	}

	@Override
	public synchronized void removeThis() {
		if (isType(TransportType.UDP)) {
			__channel.attr(AttributeKey.valueOf(getAddress())).set(null);
		} else {
			__channel.attr(NettyConnectionOption.CONNECTION).set(null);
		}
	}

	@Override
	public synchronized void setRemote(InetSocketAddress remoteAddress) {
		// only need for the Datagram connection
		if (isType(TransportType.UDP)) {
			__remoteAddress = remoteAddress;
			setAddress(__remoteAddress.toString());
		}
	}

	@Override
	public synchronized void clean() {
		// only need for WebSocket and Socket
		removePlayerName();
		removeThis();
	}

}
