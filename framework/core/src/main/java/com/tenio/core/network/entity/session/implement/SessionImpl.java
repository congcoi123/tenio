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
import com.tenio.core.network.define.TransportType;
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
import java.util.concurrent.atomic.AtomicReference;
import kcp.Ukcp;

/**
 * The implementation for session.
 *
 * @see Session
 */
public final class SessionImpl implements Session {

  private final long id;
  private final long createdTime;
  private final AtomicReference<AssociatedState> atomicAssociatedState;
  private volatile AssociatedState associatedState;
  private volatile String name;
  private volatile boolean activated;

  private SessionManager sessionManager;
  private SocketChannel socketChannel;
  private SelectionKey selectionKey;
  private DatagramChannel datagramChannel;
  private Channel webSocketChannel;
  private ConnectionFilter connectionFilter;

  private PacketQueue packetQueue;
  private ProcessedPacket processedPacket;
  private volatile PendingPacket pendingPacket;
  private volatile PacketReadState packetReadState;

  private volatile Ukcp kcpChannel;
  private volatile TransportType transportType;
  private volatile SocketAddress datagramRemoteSocketAddress;
  private volatile String clientAddress;
  private volatile int clientPort;
  private volatile int udpConvey;

  private volatile long inactivatedTime;
  private volatile long lastActivityTime;
  private volatile boolean hasUdp;
  private volatile boolean hasKcp;

  private int maxIdleTimeInSecond;

  private SessionImpl() {
    id = ID_COUNTER.getAndIncrement();
    transportType = TransportType.UNKNOWN;
    udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
    atomicAssociatedState = new AtomicReference<>();
    setAssociatedState(AssociatedState.NONE);
    long currentTime = now();
    createdTime = currentTime;
    setLastReadTime(currentTime);
    setLastWriteTime(currentTime);
  }

  /**
   * Creates a new session instance.
   *
   * @return a new instance of {@link Session}
   */
  public static Session newInstance() {
    return new SessionImpl();
  }

  /**
   * Special instance which is built for TCP.
   *
   * @return a new instance of {@link Session}
   */
  public static Session newInstanceForTcp() {
    SessionImpl session = new SessionImpl();
    session.createPacketSocketHandler();
    return session;
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
    if (this.associatedState != associatedState) {
      synchronized (this) {
        if (this.associatedState != associatedState) {
          setAssociatedState(associatedState);
        }
      }
    }
  }

  @Override
  public boolean transitionAssociatedState(AssociatedState expectedState, AssociatedState newState) {
    if (atomicAssociatedState.compareAndSet(expectedState, newState)) {
      associatedState = newState;
      return true;
    }
    return false;
  }

  @Override
  public boolean isOrphan() {
    return (!isAssociatedToPlayer(AssociatedState.DONE) &&
        (now() - createdTime) >= ORPHAN_ALLOWANCE_TIME_IN_MILLISECONDS);
  }

  @Override
  public PacketQueue fetchPacketQueue() {
    return packetQueue;
  }

  @Override
  public void configurePacketQueue(PacketQueue packetQueue) {
    this.packetQueue = packetQueue;
  }

  @Override
  public TransportType getTransportType() {
    return transportType;
  }

  @Override
  public boolean isTcp() {
    return getTransportType() == TransportType.TCP;
  }

  @Override
  public boolean containsUdp() {
    return hasUdp;
  }

  @Override
  public boolean containsKcp() {
    return hasKcp;
  }

  @Override
  public boolean isWebSocket() {
    return getTransportType() == TransportType.WEB_SOCKET;
  }

  @Override
  public SocketChannel fetchSocketChannel() {
    return socketChannel;
  }

  @Override
  public void configureSocketChannel(SocketChannel socketChannel) {
    if (getTransportType() != TransportType.UNKNOWN) {
      throw new IllegalCallerException(
          String.format("Unable to add another connection type, the current connection is: %s",
              getTransportType().toString()));
    }

    if (Objects.isNull(socketChannel)) {
      throw new IllegalArgumentException("Null value is unacceptable");
    }

    if (Objects.nonNull(socketChannel.socket()) && !socketChannel.socket().isClosed()) {
      transportType = TransportType.TCP;
      createPacketSocketHandler();
      this.socketChannel = socketChannel;

      InetSocketAddress socketAddress =
          (InetSocketAddress) this.socketChannel.socket().getRemoteSocketAddress();
      InetAddress remoteAddress = socketAddress.getAddress();
      clientAddress = remoteAddress.getHostAddress();
      clientPort = socketAddress.getPort();
    }
  }

  @Override
  public SelectionKey fetchSelectionKey() {
    return selectionKey;
  }

  @Override
  public void configureSelectionKey(SelectionKey selectionKey) {
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
  public DatagramChannel fetchDatagramChannel() {
    return datagramChannel;
  }

  @Override
  public void configureDatagramChannel(DatagramChannel datagramChannel, int udpConvey) {
    this.datagramChannel = datagramChannel;
    if (Objects.isNull(this.datagramChannel)) {
      datagramRemoteSocketAddress = null;
      this.udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
      hasUdp = false;
    } else {
      this.udpConvey = udpConvey;
      hasUdp = true;
    }
  }

  @Override
  public int getUdpConveyId() {
    return udpConvey;
  }

  @Override
  public Ukcp getKcpChannel() {
    return kcpChannel;
  }

  @Override
  public void setKcpChannel(Ukcp kcpChannel) {
    if (Objects.nonNull(this.kcpChannel) && this.kcpChannel.isActive()) {
      this.kcpChannel.close();
    }

    this.kcpChannel = kcpChannel;
    hasKcp = Objects.nonNull(kcpChannel);
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
  public Channel fetchWebSocketChannel() {
    return webSocketChannel;
  }

  @Override
  public void configureWebSocketChannel(Channel webSocketChannel) {
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

      var socketAddress = (InetSocketAddress) this.webSocketChannel.remoteAddress();
      var remoteAddress = socketAddress.getAddress();
      clientAddress = remoteAddress.getHostAddress();
      clientPort = socketAddress.getPort();
    }
  }

  @Override
  public void configureConnectionFilter(ConnectionFilter connectionFilter) {
    this.connectionFilter = connectionFilter;
  }

  @Override
  public long getCreatedTime() {
    return createdTime;
  }

  @Override
  public long getLastActivityTime() {
    return lastActivityTime;
  }

  @Override
  public long getLastReadTime() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLastReadTime(long timestamp) {
    // Reversed
    setLastActivityTime(timestamp);
  }

  @Override
  public long getLastWriteTime() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLastWriteTime(long timestamp) {
    // Reversed
    setLastActivityTime(timestamp);
  }

  @Override
  public long getReadBytes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addReadBytes(long bytes) {
    // Reserved
  }

  @Override
  public long getWrittenBytes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addWrittenBytes(long bytes) {
    // Reserved
  }

  @Override
  public long getReadMessages() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void increaseReadMessages() {
    // Reserved
  }

  @Override
  public long getDroppedPackets() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addDroppedPackets(int packets) {
    // Reserved
  }

  @Override
  public void configureMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public boolean isIdle() {
    return isConnectionIdle();
  }

  private boolean isConnectionIdle() {
    return (maxIdleTimeInSecond > 0) && ((now() - getLastActivityTime()) / 1000L > maxIdleTimeInSecond);
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public synchronized void activate() {
    activated = true;
  }

  @Override
  public long getInactivatedTime() {
    return now() - inactivatedTime;
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
  public void configureSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  @Override
  public void remove() {
    sessionManager.removeSession(this);
  }

  @Override
  public void close(ConnectionDisconnectMode connectionDisconnectMode,
                    PlayerDisconnectMode playerDisconnectMode) throws IOException {
    synchronized (this) {
      if (!activated) {
        return;
      }
      activated = false;
    }
    
    inactivatedTime = now();

    connectionFilter.removeAddress(clientAddress);

    if (Objects.nonNull(packetQueue)) {
      packetQueue.clear();
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

    sessionManager.emitEvent(ServerEvent.SESSION_WILL_BE_CLOSED, this,
        connectionDisconnectMode, playerDisconnectMode);
  }

  private void setLastActivityTime(long timestamp) {
    lastActivityTime = timestamp;
  }

  private void setAssociatedState(AssociatedState associatedState) {
    this.associatedState = associatedState;
    atomicAssociatedState.set(associatedState);
  }

  private void createPacketSocketHandler() {
    packetReadState = PacketReadState.WAIT_NEW_PACKET;
    processedPacket = ProcessedPacket.newInstance();
    pendingPacket = PendingPacket.newInstance();
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
   * equals method is overridden, to maintain the general contract for the
   * hashCode method, which states that equal objects must have equal hash codes.
   *
   * @see <a href="https://imgur.com/x6rEAZE">Formula</a>
   */
  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public String toString() {
    return "Session{" +
        "id=" + id +
        ", createdTime=" + createdTime +
        ", name='" + name + '\'' +
        ", transportType=" + transportType +
        ", clientAddress='" + clientAddress + '\'' +
        ", clientPort=" + clientPort +
        ", udpConvey=" + udpConvey +
        ", maxIdleTimeInSecond=" + maxIdleTimeInSecond +
        ", inactivatedTime=" + inactivatedTime +
        ", lastActivityTime=" + lastActivityTime +
        ", activated=" + activated +
        ", hasUdp=" + hasUdp +
        ", hasKcp=" + hasKcp +
        ", associatedState=" + associatedState +
        '}';
  }
}
