/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.entity.kcp.Ukcp;
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
 * When a connection connected to the server, it is managed by a corresponding session.
 */
public interface Session {

  /**
   * Retrieves the unique ID of session.
   *
   * @return the unique {@code long} Id of session
   */
  long getId();

  /**
   * Retrieves the session's name. It is usually assigned by the player's name if applicable.
   *
   * @return the {@link String} name of session
   */
  String getName();

  /**
   * Sets value for the session's name. It is usually assigned by the player's name if applicable.
   *
   * @param name the {@link String} name of session
   */
  void setName(String name);

  /**
   * Determines whether the session connected to the server (It is ready to associate to a player).
   *
   * @return {@code true} if the session connected to the server, otherwise returns {@code false}
   */
  boolean isConnected();

  /**
   * Sets connected state to the session on the server (whether it is ready to associate to a
   * player).
   *
   * @param connected sets value to {@code true} if the session connected to the server,
   *                  otherwise returns {@code false}
   */
  void setConnected(boolean connected);

  /**
   * Retrieves a packet queue of session which is using to send messages to clients side.
   *
   * @return an instance of {@link PacketQueue}
   */
  PacketQueue getPacketQueue();

  /**
   * Sets a packet queue to session which is using to send messages to clients side.
   *
   * @param packetQueue an instance of {@link PacketQueue}
   * @throws IllegalStateException when an illegal queue is in use
   */
  void setPacketQueue(PacketQueue packetQueue) throws IllegalStateException;

  /**
   * Retrieves the transportation type of session.
   *
   * @return the {@link TransportType} of session
   */
  TransportType getTransportType();

  /**
   * Determines whether the session is using the TCP transportation type.
   *
   * @return {@code true} if the session is using the TCP transportation type, otherwise {@code
   * false}
   */
  boolean isTcp();

  /**
   * Determines whether the session is using the WebSocket transportation type.
   *
   * @return {@code true} if the session is using the WebSocket transportation type, otherwise
   * returns {@code false}
   */
  boolean isWebSocket();

  /**
   * Determines whether the session is able to use the server UDP channel for communication. This
   * only applies for the TCP session.
   *
   * @return {@code true} if the TCP session is able to use the server UDP channel for
   * communication, otherwise returns {@code false}. In case of WebSocket session, always returns
   * {@code false}
   */
  boolean containsUdp();

  /**
   * Determines if the client and server can use KCP for communication. This can be used when the
   * UDP channel is enabled on the session.
   *
   * @return {@code true} if KCP transportation mechanism is applied and UDP channel is using for
   * conveying, otherwise returns {@code false}
   */
  boolean isEnabledKcp();

  /**
   * Allows using the KCP transportation via UDP channels.
   *
   * @param enabledKcp sets it {@code true} if enabled, otherwise sets it {code false}
   */
  void setEnabledKcp(boolean enabledKcp);

  /**
   * Determines if the session has a KCP instance for communication.
   *
   * @return {@code true} if the UDP channel is able to use the KCP for communication, otherwise
   * returns {@code false}
   */
  boolean containsKcp();

  /**
   * Retrieves a socket (TCP) channel, which is using to communicate via TCP protocol.
   *
   * @return an instance of {@link SocketChannel}
   */
  SocketChannel getSocketChannel();

  /**
   * Sets a socket (TCP) channel, which is using to communicate via TCP protocol.
   *
   * @param socketChannel an instance of {@link SocketChannel}
   * @throws IllegalArgumentException when an invalid value is used
   * @throws IllegalCallerException   when an invalid transportation type is declared
   */
  void setSocketChannel(SocketChannel socketChannel)
      throws IllegalArgumentException, IllegalCallerException;

  /**
   * Retrieves a selection key for socket (TCP) channel, which is using to switch between channels.
   *
   * @return an instance of {@link SelectionKey}
   */
  SelectionKey getSelectionKey();

  /**
   * Sets a selection key for socket (TCP) channel, which is using to switch between channels.
   *
   * @param selectionKey an instance of {@link SelectionKey}
   */
  void setSelectionKey(SelectionKey selectionKey);

  /**
   * Initializes all processed to handle packet receives from clients side.
   */
  void createPacketSocketHandler();

  /**
   * Retrieves the current processing state on a packet.
   *
   * @return the {@link PacketReadState} current processing state
   */
  PacketReadState getPacketReadState();

  /**
   * Updates the current processing state on a packet.
   *
   * @param packetReadState the {@link PacketReadState} current processing state
   */
  void setPacketReadState(PacketReadState packetReadState);

  /**
   * Retrieves the current processing packet.
   *
   * @return the {@link ProcessedPacket} current processing packet
   */
  ProcessedPacket getProcessedPacket();

  /**
   * Retrieves the pending process packet.
   *
   * @return the {@link PendingPacket} pending process packet
   */
  PendingPacket getPendingPacket();

  /**
   * Retrieves a UDP channel that the session is able to use.
   *
   * @return a {@link DatagramChannel} which is using for communication via UDP channel
   */
  DatagramChannel getDatagramChannel();

  /**
   * Declares a UDP channel that the session is able to use.
   *
   * @param datagramChannel a {@link DatagramChannel} which is using for communication via UDP
   *                        channel
   * @param remoteAddress   the {@link SocketAddress} remote address associating to the client side
   */
  void setDatagramChannel(DatagramChannel datagramChannel, SocketAddress remoteAddress);

  /**
   * Retrieves a KCP wrapper object that the session is able to use.
   *
   * @return a {@link Ukcp} instance
   */
  Ukcp getUkcp();

  /**
   * Declares a KCP wrapper object that the session is able to use.
   *
   * @param ukcp a {@link Ukcp} instance
   */
  void setUkcp(Ukcp ukcp);

  /**
   * Retrieves the remote address associating to the client side.
   *
   * @return the {@link SocketAddress} remote address associating to the client side
   */
  SocketAddress getDatagramRemoteSocketAddress();

  /**
   * Retrieves a WebSocket channel which is using for communication between the server and
   * client sides via WebSocket.
   *
   * @return the {@link Channel} WebSocket channel instance
   */
  Channel getWebSocketChannel();

  /**
   * Sets a WebSocket channel which is using for communication between the server and client
   * sides via WebSocket.
   *
   * @param webSocketChannel the {@link Channel} WebSocket channel instance
   * @throws IllegalArgumentException when an invalid value is in use
   * @throws IllegalCallerException   when an invalid transportation type is declared
   */
  void setWebSocketChannel(Channel webSocketChannel)
      throws IllegalArgumentException, IllegalCallerException;

  /**
   * Retrieves the creation time of session.
   *
   * @return the creation time of session in milliseconds ({@code long} value)
   */
  long getCreatedTime();

  /**
   * Sets creation time to the session.
   *
   * @param timestamp the creation time of session in milliseconds ({@code long} value)
   */
  void setCreatedTime(long timestamp);

  /**
   * Retrieves the last activity time of session.
   *
   * @return the last activity time in milliseconds ({@code long} value)
   */
  long getLastActivityTime();

  /**
   * Sets the last activity time for the session.
   *
   * @param timestamp the last activity time in milliseconds ({@code long} value)
   */
  void setLastActivityTime(long timestamp);

  /**
   * Retrieves the last time when the session receives the last byte of data from client side.
   *
   * @return the last reading new data time in milliseconds ({@code long} value)
   */
  long getLastReadTime();

  /**
   * Sets the last time when the session receives the last byte of data from client side.
   *
   * @param timestamp the last reading new data time in milliseconds ({@code long} value)
   */
  void setLastReadTime(long timestamp);

  /**
   * Retrieves the last time when session sends the last byte of data to client side.
   *
   * @return the last writing data time in milliseconds ({@code long} value)
   */
  long getLastWriteTime();

  /**
   * Sets the last time when the session sends the last byte of data to client side.
   *
   * @param timestamp the last writing data time in milliseconds ({@code long} value)
   */
  void setLastWriteTime(long timestamp);

  /**
   * Retrieves the total number of binaries which the session receives from client side.
   *
   * @return the total number of binaries ({@code long} value) which the session receives from
   * client side
   */
  long getReadBytes();

  /**
   * Increases the total number of binaries which the session receives from client side.
   *
   * @param numberBytes the additional number of binaries ({@code long} value) which the
   *                    session receives from client side
   */
  void addReadBytes(long numberBytes);

  /**
   * Retrieves the total number of binaries which the session sends to client side.
   *
   * @return the total number of binaries ({@code long} value) which the session sends to
   * client side
   */
  long getWrittenBytes();

  /**
   * Increases the total number of binaries which the session sends to client side.
   *
   * @param numberBytes the additional number of binaries ({@code long} value) which the
   *                    session sends to client side
   */
  void addWrittenBytes(long numberBytes);

  /**
   * Retrieves the total number of dropped packets which violated the session's policies and is
   * not able to send to client side.
   *
   * @return the total number of dropped packets ({@code long} value) which violated the
   * session's policies and is not able to send to client side
   */
  long getDroppedPackets();

  /**
   * Increases the total number of dropped packets which violated the session's policies and is
   * not able to send to client side.
   *
   * @param numberPackets the additional number of dropped packets ({@code integer} value)
   *                      which violated the session's policies and is not able to send to client
   *                      side
   */
  void addDroppedPackets(int numberPackets);

  /**
   * Retrieves the maximum time in seconds which allows the session to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @return the maximum time in seconds ({@code integer} value) which allows the session to
   * get in IDLE state
   */
  int getMaxIdleTimeInSeconds();

  /**
   * Sets the maximum time in seconds which allows the session to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                session to get in IDLE state
   */
  void setMaxIdleTimeInSeconds(int seconds);

  /**
   * Determines whether the session got in IDLE state (Do not perform any action, such as reading
   * or writing data).
   *
   * @return {@code true} if the session got in IDLE state, otherwise returns {@code false}
   */
  boolean isIdle();

  /**
   * Determines whether the session is activated (To be able to perform actions, such as reading
   * or writing data).
   *
   * @return {@code true} if the session is activated, otherwise returns {@code false}
   */
  boolean isActivated();

  /**
   * Activates the session (To be able to perform actions, such as reading or writing data).
   */
  void activate();

  /**
   * Deactivates the session (Not be able to perform actions, such as reading or writing data).
   */
  void deactivate();

  /**
   * Retrieves how long since the session is inactivated.
   *
   * @return how long since the session is inactivated in milliseconds ({@code long} value)
   */
  long getInactivatedTime();

  /**
   * Retrieves full IP address information of the client side which is using the TCP/WebSocket
   * session for communication.
   *
   * @return a {@link String} value, full IP address information of the client side which is
   * using the session for communication
   */
  String getFullClientIpAddress();

  /**
   * Retrieves IP address information of the client side which is using the TCP/WebSocket session
   * for communication.
   *
   * @return a {@link String} value, IP address information of the client side which is using the
   * session for communication
   */
  String getClientAddress();

  /**
   * Retrieves port number of the client side which is using the TCP/WebSocket session for
   * communication.
   *
   * @return an {@code integer} value, the port number of the client side which is using by
   * the session for communication
   */
  int getClientPort();

  /**
   * Retrieves IP address information of the server which is using the TCP/WebSocket session for
   * communication.
   *
   * @return a {@link String} value, IP address information of the server which is using the
   * session for communication
   */
  String getServerAddress();

  /**
   * Retrieves port number of the server which is using the TCP/WebSocket session for communication.
   *
   * @return an {@code integer} value, the port number of the server which is using by the
   * session for communication
   */
  int getServerPort();

  /**
   * Retrieves full IP address information of the server which is using the TCP/WebSocket session
   * for communication.
   *
   * @return a {@link String} value, full IP address information of the server which is using by the
   * session for communication
   */
  String getFullServerIpAddress();

  /**
   * Retrieves the session manager instance.
   *
   * @return the {@link SessionManager} instance.
   */
  SessionManager getSessionManager();

  /**
   * Sets a session manager instance.
   *
   * @param sessionManager a {@link SessionManager} instance
   */
  void setSessionManager(SessionManager sessionManager);

  /**
   * Closes the session, disconnects the connection between client side and the server.
   *
   * @param connectionDisconnectMode a {@link ConnectionDisconnectMode} regarding rules are
   *                                 applied when the session is closed, and the connection is
   *                                 disconnected
   * @param playerDisconnectMode     a {@link PlayerDisconnectMode} regarding rules are applied
   *                                 when a player associating to the session is disconnected to
   *                                 the server
   * @throws IOException when the closing process went through any issue
   */
  void close(ConnectionDisconnectMode connectionDisconnectMode,
             PlayerDisconnectMode playerDisconnectMode)
      throws IOException;

  /**
   * Closes the session, disconnects the connection between client side and the server in all
   * default modes.
   *
   * @throws IOException when the closing process went through any issue
   * @see ConnectionDisconnectMode#UNKNOWN
   * @see PlayerDisconnectMode#UNKNOWN
   */
  default void close() throws IOException {
    close(ConnectionDisconnectMode.UNKNOWN, PlayerDisconnectMode.UNKNOWN);
  }
}
