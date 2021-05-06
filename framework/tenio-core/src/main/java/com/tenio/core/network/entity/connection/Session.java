package com.tenio.core.network.entity.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.PacketQueue;

import io.netty.channel.Channel;

public interface Session {

	String getId();

	void setId(String id);

	String getHashId();

	void setHashId(String hashId);

	boolean isConnected();

	void setConnected(boolean connected);

	boolean isLoggedIn();

	void setLoggedIn(boolean loggedIn);

	PacketQueue getPacketQueue();

	void setPacketQueue(PacketQueue packetQueue);

	TransportType getTransportType();

	boolean isTcp();

	boolean isUdp();

	boolean isWebSocket();

	SocketChannel getSocketChannel();

	void setSocketChannel(SocketChannel socketChannel) throws IllegalArgumentException;

	DatagramChannel getDatagramChannel();

	void setDatagramChannel(DatagramChannel datagramChannel) throws IllegalArgumentException;

	Channel getWebSocketChannel();

	void setWebSocketChannel(Channel webSocketChannel) throws IllegalArgumentException;

	long getCreatedTime();

	void setCreatedTime(long timestamp);

	long getLastActivityTime();

	void setLastActivityTime(long timestamp);

	long getLastLoggedInActivityTime();

	void setLastLoggedInActivityTime(long timestamp);

	long getLastReadTime();

	void setLastReadTime(long timestamp);

	long getLastWriteTime();

	void setLastWriteTime(long timestamp);

	long getReadBytes();

	void addReadBytes(long bytes);

	long getWrittenBytes();

	void addWrittenBytes(long bytes);

	int getDroppedMessages();

	void addDroppedMessages(int messages);

	int getMaxIdleTimeInSeconds();

	void setMaxIdleTimeInSeconds(int seconds);

	int getMaxLoggedInIdleTimeInSeconds();

	void setMaxLoggedInIdleTimeInSeconds(int seconds);

	boolean isMarkedForEviction();

	void setMarkedForEviction();

	boolean isIdle();

	boolean isActivated();

	void activate();

	void deactivate();

	long getInactivatedTime();

	boolean isReconnectionTimeExpired();

	Object getProperty(String key);

	void setProperty(String key, Object value);

	void removeProperty(String key);

	InetSocketAddress getClientInetSocketAddress();

	void setClientInetSocketAddress(InetSocketAddress inetSocketAddress);

	String getFullClientIpAddress();

	String getClientAddress();

	int getClientPort();

	String getServerAddress();

	int getServerPort();

	String getFullServerIpAddress();

	SessionManager getSessionManager();

	void setSessionManager(SessionManager sessionManager);

	void close() throws IOException;

	int getReconnectionSeconds();

	void setReconnectionSeconds(int seconds);

	boolean isMobile();

	void setMobile(boolean mobile);

	boolean isWeb();

	void setWeb(boolean web);

}
