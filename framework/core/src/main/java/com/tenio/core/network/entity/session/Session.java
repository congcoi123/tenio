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

package com.tenio.core.network.entity.session;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import com.tenio.core.network.zero.codec.packet.PendingPacket;
import com.tenio.core.network.zero.codec.packet.ProcessedPacket;
import io.netty.channel.Channel;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * When a connection connected to the server, it's managed via the session.
 */
public interface Session {

  long getId();

  String getName();

  void setName(String name);

  boolean isConnected();

  void setConnected(boolean connected);

  PacketQueue getPacketQueue();

  void setPacketQueue(PacketQueue packetQueue) throws IllegalStateException;

  TransportType getTransportType();

  boolean isTcp();

  boolean isWebSocket();

  boolean containsUdp();

  SocketChannel getSocketChannel();

  void setSocketChannel(SocketChannel socketChannel)
      throws IllegalArgumentException, IllegalCallerException;

  SelectionKey getSelectionKey();

  void setSelectionKey(SelectionKey selectionKey);

  void createPacketSocketHandle();

  PacketReadState getPacketReadState();

  void setPacketReadState(PacketReadState packetReadState);

  ProcessedPacket getProcessedPacket();

  PendingPacket getPendingPacket();

  DatagramChannel getDatagramChannel();

  void setDatagramChannel(DatagramChannel datagramChannel, SocketAddress remoteAddress);

  SocketAddress getDatagramRemoteSocketAddress();

  Channel getWebSocketChannel();

  void setWebSocketChannel(Channel webSocketChannel)
      throws IllegalArgumentException, IllegalCallerException;

  long getCreatedTime();

  void setCreatedTime(long timestamp);

  long getLastActivityTime();

  void setLastActivityTime(long timestamp);

  long getLastReadTime();

  void setLastReadTime(long timestamp);

  long getLastWriteTime();

  void setLastWriteTime(long timestamp);

  long getReadBytes();

  void addReadBytes(long numberBytes);

  long getWrittenBytes();

  void addWrittenBytes(long numberBytes);

  long getDroppedPackets();

  void addDroppedPackets(int numberPackets);

  int getMaxIdleTimeInSeconds();

  void setMaxIdleTimeInSeconds(int seconds);

  boolean isIdle();

  boolean isActivated();

  void activate();

  void deactivate();

  long getInactivatedTime();

  String getFullClientIpAddress();

  String getClientAddress();

  int getClientPort();

  String getServerAddress();

  int getServerPort();

  String getFullServerIpAddress();

  SessionManager getSessionManager();

  void setSessionManager(SessionManager sessionManager);

  void close(ConnectionDisconnectMode connectionDisconnectMode,
             PlayerDisconnectMode playerDisconnectMode)
      throws IOException;
}
