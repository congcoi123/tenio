/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.entity.session.implement;

import com.tenio.common.utility.TimeUtility;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.exception.EmptyUdpChannelsException;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.kcp.Ukcp;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import com.tenio.core.network.zero.codec.packet.PendingPacket;
import com.tenio.core.network.zero.codec.packet.ProcessedPacket;
import io.netty.channel.Channel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The implementation for session.
 *
 * @see Session
 */
public final class SessionImpl implements Session {

  /**
   * The maximum time that this session is allowed to be orphan.
   */
  private static final long ORPHAN_ALLOWANCE_TIME_IN_MILLISECONDS = 3000L;
  private static final AtomicLong ID_COUNTER = new AtomicLong(1L);

  private final long id;
  private final AtomicLong readBytes;
  private final AtomicLong readMessages;
  private final AtomicLong writtenBytes;
  private final AtomicLong droppedPackets;
  private volatile String name;
  private SessionManager sessionManager;
  private SocketChannel socketChannel;
  private SelectionKey selectionKey;
  private DatagramChannel datagramChannel;
  private Ukcp ukcp;
  private Channel webSocketChannel;
  private ConnectionFilter connectionFilter;
  private TransportType transportType;
  private PacketReadState packetReadState;
  private ProcessedPacket processedPacket;
  private PendingPacket pendingPacket;
  private PacketQueue packetQueue;
  private volatile long createdTime;
  private volatile long lastReadTime;
  private volatile long lastWriteTime;
  private volatile long lastActivityTime;
  private volatile long inactivatedTime;

  private volatile SocketAddress datagramRemoteSocketAddress;
  private volatile String clientAddress;
  private volatile int clientPort;
  private volatile int udpConvey;
  private int serverPort;
  private String serverAddress;

  private int maxIdleTimeInSecond;

  private volatile boolean activated;
  private volatile AssociatedState associatedState;
  private volatile boolean hasUdp;
  private volatile boolean enabledKcp;
  private volatile boolean hasKcp;

  private SessionImpl() {
    id = ID_COUNTER.getAndIncrement();

    transportType = TransportType.UNKNOWN;
    packetQueue = null;
    connectionFilter = null;

    readBytes = new AtomicLong(0L);
    readMessages = new AtomicLong(0L);
    writtenBytes = new AtomicLong(0L);
    droppedPackets = new AtomicLong(0L);

    inactivatedTime = 0L;
    udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
    activated = false;
    associatedState = AssociatedState.NONE;
    hasUdp = false;
    enabledKcp = false;
    hasKcp = false;

    setCreatedTime(now());
    setLastReadTime(now());
    setLastWriteTime(now());
    setLastActivityTime(now());
  }

  /**
   * Creates a new session instance.
   *
   * @return a new instance of {@link Session}
   */
  public static Session newInstance() {
    return new SessionImpl();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean isAssociatedToPlayer(AssociatedState associatedState) {
    return this.associatedState == associatedState;
  }

  @Override
  public void setAssociatedToPlayer(AssociatedState associatedState) {
    this.associatedState = associatedState;
  }

  @Override
  public boolean isOrphan() {
    return (!isAssociatedToPlayer(AssociatedState.DONE) &&
        (now() - createdTime) >= ORPHAN_ALLOWANCE_TIME_IN_MILLISECONDS);
  }

  @Override
  public PacketQueue getPacketQueue() {
    return packetQueue;
  }

  @Override
  public void setPacketQueue(PacketQueue packetQueue) {
    this.packetQueue = packetQueue;
  }

  @Override
  public TransportType getTransportType() {
    return transportType;
  }

  @Override
  public boolean isTcp() {
    return transportType == TransportType.TCP;
  }

  @Override
  public boolean containsUdp() {
    return hasUdp;
  }

  @Override
  public boolean isEnabledKcp() {
    if (!containsUdp()) {
      throw new EmptyUdpChannelsException();
    }
    return enabledKcp;
  }

  @Override
  public void setEnabledKcp(boolean enabledKcp) {
    this.enabledKcp = enabledKcp;
  }

  @Override
  public boolean containsKcp() {
    return hasKcp;
  }

  @Override
  public boolean isWebSocket() {
    return transportType == TransportType.WEB_SOCKET;
  }

  @Override
  public SocketChannel getSocketChannel() {
    return socketChannel;
  }

  @Override
  public void setSocketChannel(SocketChannel socketChannel) {
    if (transportType != TransportType.UNKNOWN) {
      throw new IllegalCallerException(
          String.format("Unable to add another connection type, the current connection is: %s",
              transportType.toString()));
    }

    if (Objects.isNull(socketChannel)) {
      throw new IllegalArgumentException("Null value is unacceptable");
    }

    if (Objects.nonNull(socketChannel.socket()) && !socketChannel.socket().isClosed()) {
      transportType = TransportType.TCP;
      createPacketSocketHandler();

      this.socketChannel = socketChannel;

      serverAddress = this.socketChannel.socket().getLocalAddress().getHostAddress();
      serverPort = this.socketChannel.socket().getLocalPort();

      InetSocketAddress socketAddress =
          (InetSocketAddress) this.socketChannel.socket().getRemoteSocketAddress();
      InetAddress remoteAddress = socketAddress.getAddress();
      clientAddress = remoteAddress.getHostAddress();
      clientPort = socketAddress.getPort();
    }
  }

  @Override
  public void createPacketSocketHandler() {
    packetReadState = PacketReadState.WAIT_NEW_PACKET;
    processedPacket = ProcessedPacket.newInstance();
    pendingPacket = PendingPacket.newInstance();
  }

  @Override
  public SelectionKey getSelectionKey() {
    return selectionKey;
  }

  @Override
  public void setSelectionKey(SelectionKey selectionKey) {
    this.selectionKey = selectionKey;
  }

  @Override
  public PacketReadState getPacketReadState() {
    return packetReadState;
  }

  @Override
  public void setPacketReadState(PacketReadState packetReadState) {
    this.packetReadState = packetReadState;
  }

  @Override
  public ProcessedPacket getProcessedPacket() {
    return processedPacket;
  }

  @Override
  public PendingPacket getPendingPacket() {
    return pendingPacket;
  }

  @Override
  public DatagramChannel getDatagramChannel() {
    return datagramChannel;
  }

  @Override
  public void setDatagramChannel(DatagramChannel datagramChannel, int udpConvey) {
    this.datagramChannel = datagramChannel;
    if (Objects.isNull(this.datagramChannel)) {
      this.datagramRemoteSocketAddress = null;
      this.udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
      this.hasUdp = false;
    } else {
      this.udpConvey = udpConvey;
      this.hasUdp = true;
    }
  }

  @Override
  public int getUdpConveyId() {
    return udpConvey;
  }

  @Override
  public SocketAddress getDatagramRemoteSocketAddress() {
    return datagramRemoteSocketAddress;
  }

  @Override
  public void setDatagramRemoteSocketAddress(SocketAddress datagramRemoteSocketAddress) {
    this.datagramRemoteSocketAddress = datagramRemoteSocketAddress;
  }

  @Override
  public Ukcp getUkcp() {
    return ukcp;
  }

  @Override
  public void setUkcp(Ukcp ukcp) {
    this.ukcp = ukcp;
    hasKcp = Objects.nonNull(this.ukcp);
  }

  @Override
  public Channel getWebSocketChannel() {
    return webSocketChannel;
  }

  @Override
  public void setWebSocketChannel(Channel webSocketChannel) {
    if (transportType != TransportType.UNKNOWN) {
      throw new IllegalCallerException(
          String.format("Unable to add another connection type, the current connection is: %s",
              transportType.toString()));
    }

    if (Objects.isNull(webSocketChannel)) {
      throw new IllegalArgumentException("Null value is unacceptable");
    }

    if (webSocketChannel.isActive()) {
      transportType = TransportType.WEB_SOCKET;
      this.webSocketChannel = webSocketChannel;

      var serverSocketAddress = (InetSocketAddress) this.webSocketChannel.localAddress();
      var serverAddress = serverSocketAddress.getAddress();
      this.serverAddress = serverAddress.getHostAddress();
      serverPort = serverSocketAddress.getPort();

      var socketAddress = (InetSocketAddress) this.webSocketChannel.remoteAddress();
      var remoteAddress = socketAddress.getAddress();
      clientAddress = remoteAddress.getHostAddress();
      clientPort = socketAddress.getPort();
    }
  }

  @Override
  public void setConnectionFilter(ConnectionFilter connectionFilter) {
    this.connectionFilter = connectionFilter;
  }

  @Override
  public long getCreatedTime() {
    return createdTime;
  }

  @Override
  public void setCreatedTime(long timestamp) {
    createdTime = timestamp;
  }

  @Override
  public long getLastActivityTime() {
    return lastActivityTime;
  }

  @Override
  public void setLastActivityTime(long timestamp) {
    lastActivityTime = timestamp;
  }

  @Override
  public long getLastReadTime() {
    return lastReadTime;
  }

  @Override
  public void setLastReadTime(long timestamp) {
    lastReadTime = timestamp;
    setLastActivityTime(lastReadTime);
  }

  @Override
  public long getLastWriteTime() {
    return lastWriteTime;
  }

  @Override
  public void setLastWriteTime(long timestamp) {
    lastWriteTime = timestamp;
    setLastActivityTime(lastWriteTime);
  }

  @Override
  public long getReadBytes() {
    return readBytes.get();
  }

  @Override
  public void addReadBytes(long bytes) {
    readBytes.addAndGet(bytes);
  }

  @Override
  public long getWrittenBytes() {
    return writtenBytes.get();
  }

  @Override
  public void addWrittenBytes(long bytes) {
    writtenBytes.addAndGet(bytes);
  }

  @Override
  public long getReadMessages() {
    return readMessages.get();
  }

  @Override
  public void increaseReadMessages() {
    readMessages.incrementAndGet();
  }

  @Override
  public long getDroppedPackets() {
    return droppedPackets.get();
  }

  @Override
  public void addDroppedPackets(int packets) {
    droppedPackets.addAndGet(packets);
  }

  @Override
  public int getMaxIdleTimeInSeconds() {
    return maxIdleTimeInSecond;
  }

  @Override
  public void setMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public boolean isIdle() {
    return isConnectionIdle();
  }

  private boolean isConnectionIdle() {
    if (getMaxIdleTimeInSeconds() > 0) {
      long elapsedSinceLastActivity = TimeUtility.currentTimeMillis() - getLastActivityTime();
      return elapsedSinceLastActivity / 1000L > (long) getMaxIdleTimeInSeconds();
    }
    return false;
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public void activate() {
    activated = true;
  }

  @Override
  public void deactivate() {
    activated = false;
    inactivatedTime = TimeUtility.currentTimeMillis();
  }

  @Override
  public long getInactivatedTime() {
    return inactivatedTime;
  }

  @Override
  public String getFullClientIpAddress() {
    return String.format("%s:%d", clientAddress, clientPort);
  }

  @Override
  public String getClientAddress() {
    return clientAddress;
  }

  @Override
  public int getClientPort() {
    return clientPort;
  }

  @Override
  public String getServerAddress() {
    return serverAddress;
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public String getFullServerIpAddress() {
    return String.format("%s:%d", serverAddress, serverPort);
  }

  @Override
  public SessionManager getSessionManager() {
    return sessionManager;
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void remove() {
    getSessionManager().removeSession(this);
  }

  @Override
  public void close(ConnectionDisconnectMode connectionDisconnectMode,
                    PlayerDisconnectMode playerDisconnectMode)
      throws IOException {
    if (!isActivated()) {
      return;
    }
    deactivate();

    connectionFilter.removeAddress(clientAddress);

    if (Objects.nonNull(packetQueue)) {
      packetQueue.clear();
      setPacketQueue(null);
    }

    if (hasKcp) {
      ukcp.getKcpIoHandler().channelInactiveIn(this);
      setUkcp(null);
    }

    switch (transportType) {
      case TCP:
        if (Objects.nonNull(socketChannel)) {
          var socket = socketChannel.socket();
          if (Objects.nonNull(socket) && !socket.isClosed()) {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            socketChannel.close();
          }
        }
        break;

      case WEB_SOCKET:
        if (Objects.nonNull(webSocketChannel)) {
          webSocketChannel.close();
        }
        break;

      default:
        break;
    }

    getSessionManager().emitEvent(ServerEvent.SESSION_WILL_BE_CLOSED, this,
        connectionDisconnectMode, playerDisconnectMode);
  }

  private long now() {
    return TimeUtility.currentTimeMillis();
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Session session)) {
      return false;
    } else {
      return getId() == session.getId();
    }
  }

  /**
   * It is generally necessary to override the <b>hashCode</b> method whenever
   * equals method is overridden, so as to maintain the general contract for the
   * hashCode method, which states that equal objects must have equal hash codes.
   *
   * @see <a href="https://imgur.com/x6rEAZE">Formula</a>
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Session{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", transportType=" + transportType +
        ", createdTime=" + createdTime +
        ", lastReadTime=" + lastReadTime +
        ", lastWriteTime=" + lastWriteTime +
        ", lastActivityTime=" + lastActivityTime +
        ", readBytes=" + readBytes +
        ", writtenBytes=" + writtenBytes +
        ", droppedPackets=" + droppedPackets +
        ", inactivatedTime=" + inactivatedTime +
        ", datagramRemoteSocketAddress=" + datagramRemoteSocketAddress +
        ", clientAddress='" + clientAddress + '\'' +
        ", clientPort=" + clientPort +
        ", serverPort=" + serverPort +
        ", serverAddress='" + serverAddress + '\'' +
        ", maxIdleTimeInSecond=" + maxIdleTimeInSecond +
        ", activated=" + activated +
        ", associatedState=" + associatedState +
        ", hasUdp=" + hasUdp +
        ", udpConvey=" + udpConvey +
        ", enabledKcp=" + enabledKcp +
        ", hasKcp=" + hasKcp +
        '}';
  }
}
