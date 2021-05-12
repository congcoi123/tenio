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
package com.tenio.core.network.entities.session.implement;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.entities.packet.PacketQueue;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import com.tenio.core.network.zero.codec.packet.PendingPacket;
import com.tenio.core.network.zero.codec.packet.ProcessedPacket;

import io.netty.channel.Channel;

/**
 * @author kong
 */
// TODO: Add description
public final class SessionImpl implements Session {

	private static AtomicLong __idCounter = new AtomicLong();

	private final long __id;

	private String __name;

	private SessionManager __sessionManager;
	private SocketChannel __socketChannel;
	private SelectionKey __selectionKey;
	private DatagramChannel __datagramChannel;
	private Channel __webSocketChannel;

	private TransportType __transportType;
	private PacketReadState __packetReadState;
	private ProcessedPacket __processedPacket;
	private PendingPacket __pendingPacket;
	private PacketQueue __packetQueue;

	private volatile long __createdTime;
	private volatile long __lastReadTime;
	private volatile long __lastWriteTime;
	private volatile long __lastActivityTime;

	private volatile long __readBytes;
	private volatile long __writtenBytes;
	private volatile long __droppedPackets;

	private volatile long __inactivatedTime;

	private volatile InetSocketAddress __datagramInetSocketAddress;
	private volatile String __clientAddress;
	private volatile int __clientPort;
	private int __serverPort;
	private String __serverAddress;

	private int __maxIdleTimeInSecond;

	private volatile boolean __active;
	private volatile boolean __connected;
	private volatile boolean __hasUdp;

	public static Session newInstance() {
		return new SessionImpl();
	}

	private SessionImpl() {
		__id = __idCounter.getAndIncrement();

		__transportType = TransportType.UNKNOWN;
		__packetQueue = null;

		setCreatedTime(__now());
		setLastReadTime(__now());
		setLastWriteTime(__now());
		setLastActivityTime(__now());

		__readBytes = 0L;
		__writtenBytes = 0L;
		__droppedPackets = 0L;

		__inactivatedTime = 0L;
		__active = false;
		__connected = false;
		__hasUdp = false;
	}

	@Override
	public long getId() {
		return __id;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) {
		__name = name;
	}

	@Override
	public boolean isConnected() {
		return __connected;
	}

	@Override
	public void setConnected(boolean connected) {
		__connected = connected;
	}

	@Override
	public PacketQueue getPacketQueue() {
		return __packetQueue;
	}

	@Override
	public void setPacketQueue(PacketQueue packetQueue) {
		if (__packetQueue != null) {
			throw new IllegalStateException("Unable to reassign the packet queue. Queue already exists");
		}
		__packetQueue = packetQueue;
	}

	@Override
	public TransportType getTransportType() {
		return __transportType;
	}

	@Override
	public boolean isTcp() {
		return __transportType == TransportType.TCP;
	}

	@Override
	public boolean containsUdp() {
		return __hasUdp;
	}

	@Override
	public boolean isWebSocket() {
		return __transportType == TransportType.WEB_SOCKET;
	}

	@Override
	public SocketChannel getSocketChannel() {
		return __socketChannel;
	}

	@Override
	public void setSocketChannel(SocketChannel socketChannel) {
		if (__transportType != TransportType.UNKNOWN) {
			throw new IllegalCallerException(String.format(
					"Could not add another connection type, the current connection is: ", __transportType.toString()));
		}

		if (socketChannel == null) {
			throw new IllegalArgumentException("Null value is unacceptable");
		}

		if (socketChannel.socket() != null && !socketChannel.socket().isClosed()) {
			__transportType = TransportType.TCP;
			__packetReadState = PacketReadState.WAIT_NEW_PACKET;
			__processedPacket = ProcessedPacket.newInstance();
			__pendingPacket = PendingPacket.newInstance();

			__socketChannel = socketChannel;

			__serverAddress = __socketChannel.socket().getLocalAddress().getHostAddress();
			__serverPort = __socketChannel.socket().getLocalPort();

			InetSocketAddress socketAddress = (InetSocketAddress) __socketChannel.socket().getRemoteSocketAddress();
			InetAddress remoteAdress = socketAddress.getAddress();
			__clientAddress = remoteAdress.getHostAddress();
			__clientPort = socketAddress.getPort();
		}
	}

	@Override
	public SelectionKey getSelectionKey() {
		return __selectionKey;
	}

	@Override
	public void setSelectionKey(SelectionKey selectionKey) {
		__selectionKey = selectionKey;
	}

	@Override
	public PacketReadState getPacketReadState() {
		return __packetReadState;
	}

	@Override
	public void setPacketReadState(PacketReadState packetReadState) {
		__packetReadState = packetReadState;
	}

	@Override
	public ProcessedPacket getProcessedPacket() {
		return __processedPacket;
	}

	@Override
	public PendingPacket getPendingPacket() {
		return __pendingPacket;
	}

	@Override
	public DatagramChannel getDatagramChannel() {
		return __datagramChannel;
	}

	@Override
	public void setDatagramChannel(DatagramChannel datagramChannel) {
		__datagramChannel = datagramChannel;
		__datagramInetSocketAddress = (InetSocketAddress) __datagramChannel.socket().getRemoteSocketAddress();
		__hasUdp = true;
	}

	@Override
	public InetSocketAddress getDatagramInetSocketAddress() {
		return __datagramInetSocketAddress;
	}

	@Override
	public Channel getWebSocketChannel() {
		return __webSocketChannel;
	}

	@Override
	public void setWebSocketChannel(Channel webSocketChannel) {
		if (__transportType != TransportType.UNKNOWN) {
			throw new IllegalCallerException(String.format(
					"Could not add another connection type, the current connection is: ", __transportType.toString()));
		}

		if (webSocketChannel == null) {
			throw new IllegalArgumentException("Null value is unacceptable");
		}

		if (webSocketChannel.isActive()) {
			__transportType = TransportType.WEB_SOCKET;
			__webSocketChannel = webSocketChannel;

			InetSocketAddress serverSocketAddress = (InetSocketAddress) __webSocketChannel.localAddress();
			InetAddress serverAdress = serverSocketAddress.getAddress();
			__serverAddress = serverAdress.getHostAddress();
			__serverPort = serverSocketAddress.getPort();

			InetSocketAddress socketAddress = (InetSocketAddress) __webSocketChannel.remoteAddress();
			InetAddress remoteAdress = socketAddress.getAddress();
			__clientAddress = remoteAdress.getHostAddress();
			__clientPort = socketAddress.getPort();
		}

	}

	@Override
	public long getCreatedTime() {
		return __createdTime;
	}

	@Override
	public void setCreatedTime(long timestamp) {
		__createdTime = timestamp;
	}

	@Override
	public long getLastActivityTime() {
		return __lastActivityTime;
	}

	@Override
	public void setLastActivityTime(long timestamp) {
		__lastActivityTime = timestamp;
	}

	@Override
	public long getLastReadTime() {
		return __lastReadTime;
	}

	@Override
	public void setLastReadTime(long timestamp) {
		__lastReadTime = timestamp;
		setLastActivityTime(__lastReadTime);
	}

	@Override
	public long getLastWriteTime() {
		return __lastWriteTime;
	}

	@Override
	public void setLastWriteTime(long timestamp) {
		__lastWriteTime = timestamp;
		setLastActivityTime(__lastWriteTime);
	}

	@Override
	public long getReadBytes() {
		return __readBytes;
	}

	@Override
	public void addReadBytes(long bytes) {
		__readBytes += bytes;
	}

	@Override
	public long getWrittenBytes() {
		return __writtenBytes;
	}

	@Override
	public void addWrittenBytes(long bytes) {
		__writtenBytes += bytes;
	}

	@Override
	public long getDroppedPackets() {
		return __droppedPackets;
	}

	@Override
	public void addDroppedPackets(int packets) {
		__droppedPackets += packets;
	}

	@Override
	public int getMaxIdleTimeInSeconds() {
		return __maxIdleTimeInSecond;
	}

	@Override
	public void setMaxIdleTimeInSeconds(int seconds) {
		__maxIdleTimeInSecond = seconds;
	}

	@Override
	public boolean isIdle() {
		return __isConnectionIdle();
	}

	private boolean __isConnectionIdle() {
		if (getMaxIdleTimeInSeconds() > 0) {
			long elapsedSinceLastActivity = TimeUtility.currentTimeMillis() - getLastActivityTime();
			return elapsedSinceLastActivity / 1000L > (long) getMaxIdleTimeInSeconds();
		}

		return false;
	}

	@Override
	public boolean isActivated() {
		return __active;
	}

	@Override
	public void activate() {
		__active = true;
	}

	@Override
	public void deactivate() {
		__active = false;
		__inactivatedTime = TimeUtility.currentTimeMillis();
	}

	@Override
	public long getInactivatedTime() {
		return __inactivatedTime;
	}

	@Override
	public String getFullClientIpAddress() {
		return String.format("%s:%d", __clientAddress, __clientPort);
	}

	@Override
	public String getClientAddress() {
		return __clientAddress;
	}

	@Override
	public int getClientPort() {
		return __clientPort;
	}

	@Override
	public String getServerAddress() {
		return __serverAddress;
	}

	@Override
	public int getServerPort() {
		return __serverPort;
	}

	@Override
	public String getFullServerIpAddress() {
		return String.format("%s:%d", __serverAddress, __serverPort);
	}

	@Override
	public SessionManager getSessionManager() {
		return __sessionManager;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		__sessionManager = sessionManager;
	}

	@Override
	public void close() throws IOException {
		__packetQueue.clear();
		__packetQueue = null;

		switch (__transportType) {
		case TCP:
			if (__socketChannel != null) {
				Socket socket = __socketChannel.socket();
				if (socket != null && !socket.isClosed()) {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
					__socketChannel.close();
				}
				getSessionManager().removeSessionBySocket(__socketChannel);
			}
			break;

		case WEB_SOCKET:
			if (__webSocketChannel != null) {
				__webSocketChannel.close();
				getSessionManager().removeSessionByWebSocket(__webSocketChannel);
			}
			break;

		default:
			break;
		}

		__active = false;
		__connected = false;
		__hasUdp = false;

		__sessionManager.removeSession(this);
	}

	private long __now() {
		return TimeUtility.currentTimeMillis();
	}

	// FIXME: Needs to implement these methods
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
