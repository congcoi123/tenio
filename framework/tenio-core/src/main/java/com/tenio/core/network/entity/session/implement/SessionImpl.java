package com.tenio.core.network.entity.session.implement;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tenio.common.utility.TimeUtility;
import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import com.tenio.core.network.zero.codec.packet.PendingPacket;
import com.tenio.core.network.zero.codec.packet.ProcessedPacket;

import io.netty.channel.Channel;

public final class SessionImpl implements Session {

	private volatile long __readBytes;
	private volatile long __writtenBytes;
	private volatile int __droppedPackets;

	private SocketChannel __socketChannel;
	private SelectionKey __selectionKey;
	private DatagramChannel __datagramChannel;
	private Channel __webSocketChannel;
	private TransportType __transportType;
	private PacketReadState __packetReadState;
	private ProcessedPacket __processedPacket;
	private PendingPacket __pendingPacket;

	private volatile long __createdTime;
	private volatile long __lastReadTime;
	private volatile long __lastWriteTime;
	private volatile long __lastActivityTime;
	private volatile long __lastLoggedInActivityTime;

	private String __id;
	private String __hashId;

	private volatile InetSocketAddress __clientInetSocketAddress;
	private volatile String __clientAddress;
	private volatile int __clientPort;
	private int __serverPort;
	private String __serverAddress;

	private int __maxIdleTime;
	private int __maxLoggedInIdleTime;

	private volatile int __reconnectionSeconds;

	private volatile long __inactivatedTime;
	private volatile boolean __active;

	private boolean __markedForEviction;

	private volatile boolean __connected;
	private volatile boolean __loggedIn;

	private PacketQueue __packetQueue;
	private SessionManager __sessionManager;

	private Map<String, Object> __properties;

	private volatile boolean __mobile;
	private volatile boolean __web;

	public SessionImpl() {
		__properties = new ConcurrentHashMap<String, Object>();
		__transportType = TransportType.UNKNOWN;
		__packetReadState = PacketReadState.WAIT_NEW_PACKET;
		__processedPacket = new ProcessedPacket();
		__pendingPacket = new PendingPacket();
		__inactivatedTime = 0L;
		__active = true;
		__markedForEviction = false;
		__connected = false;
		__loggedIn = false;
		__mobile = false;
		__web = false;
	}

	@Override
	public InternalEventManager getEventManager() {
		return null;
	}

	@Override
	public String getId() {
		return __id;
	}

	@Override
	public void setId(String id) {
		__id = id;
	}

	@Override
	public String getHashId() {
		return __hashId;
	}

	@Override
	public void setHashId(String hashId) {
		__hashId = hashId;
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
	public boolean isLoggedIn() {
		return __loggedIn;
	}

	@Override
	public void setLoggedIn(boolean loggedIn) {
		__loggedIn = loggedIn;
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
	public boolean isUdp() {
		return __transportType == TransportType.UDP;
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
					"Could not add other connection type, the current connection is: ", __transportType.toString()));
		}

		if (socketChannel == null) {
			throw new IllegalArgumentException("Null value is unacceptable");
		}

		__transportType = TransportType.TCP;
		__socketChannel = socketChannel;

		if (__socketChannel.socket() != null && !__socketChannel.socket().isClosed()) {
			__serverAddress = __socketChannel.socket().getLocalAddress().getHostAddress();
			__serverPort = __socketChannel.socket().getLocalPort();

			InetSocketAddress socketAddress = (InetSocketAddress) __socketChannel.socket().getRemoteSocketAddress();
			InetAddress remoteAdress = socketAddress.getAddress();
			__clientAddress = remoteAdress.getHostAddress();
			__clientPort = socketAddress.getPort();

			__connected = true;
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
		if (__transportType != TransportType.UNKNOWN) {
			throw new IllegalCallerException(String.format(
					"Could not add other connection type, the current connection is: ", __transportType.toString()));
		}

		if (datagramChannel == null) {
			throw new IllegalArgumentException("Null value is unacceptable");
		}

		__transportType = TransportType.UDP;
		__datagramChannel = datagramChannel;
		__connected = true;
	}

	@Override
	public Channel getWebSocketChannel() {
		return __webSocketChannel;
	}

	@Override
	public void setWebSocketChannel(Channel webSocketChannel) {
		if (__transportType != TransportType.UNKNOWN) {
			throw new IllegalCallerException(String.format(
					"Could not add other connection type, the current connection is: ", __transportType.toString()));
		}

		if (webSocketChannel == null) {
			throw new IllegalArgumentException("Null value is unacceptable");
		}

		__transportType = TransportType.WEB_SOCKET;
		__webSocketChannel = webSocketChannel;

		if (__webSocketChannel.isActive()) {
			InetSocketAddress serverSocketAddress = (InetSocketAddress) __webSocketChannel.localAddress();
			InetAddress serverAdress = serverSocketAddress.getAddress();
			__serverAddress = serverAdress.getHostAddress();
			__serverPort = serverSocketAddress.getPort();

			InetSocketAddress socketAddress = (InetSocketAddress) __webSocketChannel.remoteAddress();
			InetAddress remoteAdress = socketAddress.getAddress();
			__clientAddress = remoteAdress.getHostAddress();
			__clientPort = socketAddress.getPort();

			__connected = true;
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
	public long getLastLoggedInActivityTime() {
		return __lastLoggedInActivityTime;
	}

	@Override
	public void setLastLoggedInActivityTime(long timestamp) {
		__lastLoggedInActivityTime = timestamp;
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
	public int getDroppedPackets() {
		return __droppedPackets;
	}

	@Override
	public void addDroppedPackets(int packets) {
		__droppedPackets += packets;
	}

	@Override
	public int getMaxIdleTimeInSeconds() {
		return __maxIdleTime;
	}

	@Override
	public void setMaxIdleTimeInSeconds(int seconds) {
		__maxIdleTime = seconds;
	}

	@Override
	public int getMaxLoggedInIdleTimeInSeconds() {
		return __maxLoggedInIdleTime;
	}

	@Override
	public void setMaxLoggedInIdleTimeInSeconds(int seconds) {
		if (seconds < getMaxIdleTimeInSeconds()) {
			seconds = getMaxIdleTimeInSeconds() + 60;
		}
		__maxLoggedInIdleTime = seconds;
	}

	@Override
	public boolean isMarkedForEviction() {
		return __markedForEviction;
	}

	@Override
	public void setMarkedForEviction() {
		__markedForEviction = true;
		__reconnectionSeconds = 0;
	}

	@Override
	public boolean isIdle() {
		return isLoggedIn() ? __isLoggedInIdle() : __isConnectionIdle();
	}

	private boolean __isConnectionIdle() {
		if (getMaxIdleTimeInSeconds() > 0) {
			long elapsedSinceLastActivity = TimeUtility.currentTimeMillis() - getLastActivityTime();
			return elapsedSinceLastActivity / 1000L > (long) getMaxIdleTimeInSeconds();
		}

		return false;
	}

	private boolean __isLoggedInIdle() {
		if (getMaxLoggedInIdleTimeInSeconds() > 0) {
			long elapsedSinceLastActivity = TimeUtility.currentTimeMillis() - getLastLoggedInActivityTime();
			return elapsedSinceLastActivity / 1000L > (long) getMaxLoggedInIdleTimeInSeconds();
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
	public boolean isReconnectionTimeExpired() {
		long expiry = __inactivatedTime + (long) (1000 * __reconnectionSeconds);
		return TimeUtility.currentTimeMillis() > expiry;
	}

	@Override
	public Object getProperty(String key) {
		return __properties.get(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		__properties.put(key, value);
	}

	@Override
	public void removeProperty(String key) {
		__properties.remove(key);
	}

	@Override
	public InetSocketAddress getClientInetSocketAddress() {
		return __clientInetSocketAddress;
	}

	@Override
	public void setClientInetSocketAddress(InetSocketAddress inetSocketAddress) {
		__clientInetSocketAddress = inetSocketAddress;
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
			}
			__sessionManager.removeSessionBySocket(__socketChannel);
			break;

		case UDP:
			__datagramChannel = null;
			__sessionManager.removeSessionByDatagram(getClientInetSocketAddress().toString());
			break;

		case WEB_SOCKET:
			if (__webSocketChannel != null) {
				__webSocketChannel.close();
			}
			__sessionManager.removeSessionByWebSocket(__webSocketChannel);
			break;

		default:
			break;
		}
		__connected = false;
	}

	@Override
	public int getReconnectionSeconds() {
		return __reconnectionSeconds;
	}

	@Override
	public void setReconnectionSeconds(int seconds) {
		__reconnectionSeconds = seconds;
	}

	@Override
	public boolean isMobile() {
		return __mobile;
	}

	@Override
	public void setMobile(boolean mobile) {
		__mobile = mobile;
	}

	@Override
	public boolean isWeb() {
		return __web;
	}

	@Override
	public void setWeb(boolean web) {
		__web = web;
	}

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
