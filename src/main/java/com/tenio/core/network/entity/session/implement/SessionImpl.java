/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import com.tenio.core.network.codec.packet.PacketReadState;
import com.tenio.core.network.codec.packet.PendingPacket;
import com.tenio.core.network.codec.packet.ProcessedPacket;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.utility.SocketUtility;
import io.netty.channel.Channel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicReference;
import kcp.Ukcp;

/**
 * The implementation for session.
 *
 * @see Session
 */
public class SessionImpl implements Session {

  private final long id;
  private final long createdTime;
  private final AtomicReference<AssociatedState> atomicAssociatedState;
  private volatile AssociatedState associatedState;
  private volatile String name;
  private volatile boolean activated;

  private SessionManager sessionManager;
  private SocketChannel socketChannel;
  private SelectionKey socketSelectionKey;
  private DatagramChannel datagramChannel;
  private Channel webSocketChannel;
  private ConnectionFilter connectionFilter;

  private PacketQueue packetQueue;
  private ProcessedPacket processedPacket;
  private volatile PendingPacket pendingPacket;
  private volatile PacketReadState packetReadState;

  private volatile Ukcp kcpChannel;
  private volatile TransportType transportType;
  private volatile InetSocketAddress socketRemoteAddress;
  private volatile InetSocketAddress datagramRemoteAddress;
  private volatile int udpConvey;

  private volatile long inactivatedTime;
  private volatile long lastActivityTime;
  private volatile boolean hasUdp;
  private volatile boolean hasKcp;

  private int maxIdleTimeInSecond;

  /**
   * Constructor.
   *
   * @since 0.6.7
   */
  protected SessionImpl() {
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
  public boolean transitionAssociatedState(AssociatedState expectedState,
                                           AssociatedState newState) {
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
  public void configureSocketChannel(SocketChannel socketChannel, SelectionKey selectionKey)
      throws IllegalArgumentException, IllegalCallerException {
    if (getTransportType() != TransportType.UNKNOWN) {
      throw new IllegalCallerException(
          String.format("Unable to add another connection type, the current connection is: %s",
              getTransportType().toString()));
    }

    if (socketChannel == null) {
      throw new IllegalArgumentException("Null value is unacceptable");
    }

    transportType = TransportType.TCP;
    createPacketSocketHandler();
    this.socketChannel = socketChannel;
    this.socketSelectionKey = selectionKey;

    socketRemoteAddress = (InetSocketAddress) this.socketChannel.socket().getRemoteSocketAddress();
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
  public SelectionKey fectchSocketSelectionKey() {
    return socketSelectionKey;
  }

  @Override
  public SocketAddress getSocketRemoteAddress() {
    return socketRemoteAddress;
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
  public void configureDatagramChannel(DatagramChannel datagramChannel, int udpConvey) {
    this.datagramChannel = datagramChannel;
    if (this.datagramChannel == null) {
      datagramRemoteAddress = null;
      this.udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
      hasUdp = false;
    } else {
      this.udpConvey = udpConvey;
      hasUdp = true;
    }
  }

  @Override
  public DatagramChannel fetchDatagramChannel() {
    return datagramChannel;
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
    if (this.kcpChannel != null && this.kcpChannel.isActive()) {
      this.kcpChannel.close();
    }

    this.kcpChannel = kcpChannel;
    hasKcp = kcpChannel != null;
  }

  @Override
  public SocketAddress getDatagramRemoteAddress() {
    return datagramRemoteAddress;
  }

  @Override
  public void setDatagramRemoteAddress(SocketAddress datagramRemoteAddress) {
    this.datagramRemoteAddress = (InetSocketAddress) datagramRemoteAddress;
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

    if (webSocketChannel == null) {
      throw new IllegalArgumentException("Null value is unacceptable");
    }

    transportType = TransportType.WEB_SOCKET;
    this.webSocketChannel = webSocketChannel;

    socketRemoteAddress = (InetSocketAddress) this.webSocketChannel.remoteAddress();
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

  private void setLastActivityTime(long timestamp) {
    lastActivityTime = timestamp;
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
    return (maxIdleTimeInSecond > 0) &&
        ((now() - getLastActivityTime()) / 1000L > maxIdleTimeInSecond);
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

    connectionFilter.removeAddress(socketRemoteAddress.getAddress().getHostAddress());

    if (packetQueue != null) {
      packetQueue.clear();
    }

    switch (transportType) {
      case TCP:
        SocketUtility.closeSocket(socketChannel, socketSelectionKey);
        break;

      case WEB_SOCKET:
        SocketUtility.closeSocket(webSocketChannel);
        break;

      default:
        break;
    }

    sessionManager.emitEvent(ServerEvent.SESSION_WILL_BE_CLOSED, this,
        connectionDisconnectMode, playerDisconnectMode);
  }

  private void setAssociatedState(AssociatedState associatedState) {
    this.associatedState = associatedState;
    atomicAssociatedState.set(associatedState);
  }

  /**
   * Creates packet handler objects.
   *
   * @since 0.6.7
   */
  protected void createPacketSocketHandler() {
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
        ", socketRemoteAddress='" +
        (socketRemoteAddress == null ? "null" :
            (socketRemoteAddress.getAddress() + ":" + socketRemoteAddress.getPort())) + '\'' +
        ", datagramRemoteAddress='" + (datagramRemoteAddress == null ? "null" :
        (datagramRemoteAddress.getAddress() + ":" + datagramRemoteAddress.getPort())) + '\'' +
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
