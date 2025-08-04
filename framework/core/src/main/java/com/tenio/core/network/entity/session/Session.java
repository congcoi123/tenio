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

package com.tenio.core.network.entity.session;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.codec.packet.PacketReadState;
import com.tenio.core.network.codec.packet.PendingPacket;
import com.tenio.core.network.codec.packet.ProcessedPacket;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.scheduler.task.core.AutoCleanOrphanSessionTask;
import io.netty.channel.Channel;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;
import kcp.Ukcp;

/**
 * Represents a network session that manages communication between the server and clients.
 * This interface defines the core functionality for handling network connections,
 * data transfer, and session lifecycle management.
 *
 * <p>Key features:
 * <ul>
 *   <li>Network connection management</li>
 *   <li>Data transfer tracking (bytes read/written)</li>
 *   <li>Session state and lifecycle control</li>
 *   <li>Player association management</li>
 *   <li>Idle state monitoring</li>
 * </ul>
 *
 * <p>Thread safety: Implementations of this interface should be thread-safe
 * as they may be accessed from multiple threads concurrently. The interface
 * provides atomic operations for state transitions and data transfer tracking.
 *
 * @see SessionManager
 * @see AssociatedState
 * @see ConnectionDisconnectMode
 * @see PlayerDisconnectMode
 * @since 0.3.0
 */
public interface Session {

  /**
   * IDs generator.
   */
  AtomicLong ID_COUNTER = new AtomicLong(1L);
  /**
   * The maximum time that this session is allowed to be orphan.
   */
  long ORPHAN_ALLOWANCE_TIME_IN_MILLISECONDS = 3000L;
  /**
   * The default UDP convey ID.
   */
  int EMPTY_DATAGRAM_CONVEY_ID = -1;

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
   * @param associatedState the {@link AssociatedState} instance
   * @return {@code true} if the player associated state matches with the parameter, otherwise
   * returns {@code false}
   * @since 0.5.0
   */
  boolean isAssociatedToPlayer(AssociatedState associatedState);

  /**
   * Sets connected state to the session on the server (whether it is ready to associate to a
   * player).
   *
   * @param associatedState the {@link AssociatedState} instance
   * @since 0.5.0
   */
  void setAssociatedToPlayer(AssociatedState associatedState);

  /**
   * Updates associated state in thread-safe.
   *
   * @param expectedState the current expected state
   * @param newState      new state
   * @return {@code true} if the update is successful, otherwise returns {@code false}
   * @since 0.6.1
   */
  boolean transitionAssociatedState(AssociatedState expectedState, AssociatedState newState);

  /**
   * In allowance period of time, if the session can not be associated to any player, it is
   * considered as an orphan session and will be removed.
   *
   * @return {@code true} if the session is orphan, otherwise returns {@code false}
   * @see Player
   * @see AutoCleanOrphanSessionTask
   * @since 0.5.0
   */
  boolean isOrphan();

  /**
   * Retrieves a packet queue of session which is using to send messages to clients side.
   *
   * @return an instance of {@link PacketQueue}
   */
  PacketQueue fetchPacketQueue();

  /**
   * Sets a packet queue to session which is using to send messages to clients side.
   *
   * @param packetQueue an instance of {@link PacketQueue}
   * @throws IllegalStateException when an illegal queue is in use
   */
  void configurePacketQueue(PacketQueue packetQueue) throws IllegalStateException;

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
   * Determines whether the session is able to use the server KCP channel for communication. This
   * only applies for the TCP session.
   *
   * @return {@code true} if the TCP session is able to use the server KCP channel for
   * communication, otherwise returns {@code false}. In case of WebSocket session, always returns
   * {@code false}
   */
  boolean containsKcp();

  /**
   * Sets a socket (TCP) channel, which is using to communicate via TCP protocol.
   *
   * @param socketChannel an instance of {@link SocketChannel}
   * @param selectionKey  an instance of {@link SelectionKey}, selected for a socket channel by a selector
   * @throws IllegalArgumentException when an invalid value is used
   * @throws IllegalCallerException   when an invalid transportation type is declared
   */
  void configureSocketChannel(SocketChannel socketChannel, SelectionKey selectionKey)
      throws IllegalArgumentException, IllegalCallerException;

  /**
   * Retrieves a socket (TCP) channel, which is using to communicate via TCP protocol.
   *
   * @return an instance of {@link SocketChannel}
   */
  SocketChannel fetchSocketChannel();

  /**
   * Retrieves a selection key which is associating to its socket channel.
   *
   * @return an instance of {@link SelectionKey}
   * @see #fetchSocketChannel()
   * @since 0.6.7
   */
  SelectionKey fectchSocketSelectionKey();

  /**
   * Retrieves the remote address associating to the client side whenever the server receives
   * message from him.
   *
   * @return the {@link SocketAddress} remote address associating to the client side
   * @since 0.6.6
   */
  SocketAddress getSocketRemoteAddress();

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
   * Declares a UDP channel that the session is able to use.
   *
   * @param datagramChannel a {@link DatagramChannel} which is using for communication via UDP
   *                        channel (there will be some channels opened on the server side)
   * @param udpConvey       the Udp channel convey ID, which is unique value generated by the server
   */
  void configureDatagramChannel(DatagramChannel datagramChannel, int udpConvey);

  /**
   * Retrieves a UDP channel that the session is able to use.
   *
   * @return a {@link DatagramChannel} which is using for communication via UDP channel
   */
  DatagramChannel fetchDatagramChannel();

  /**
   * Retrieves the Udp channel convey ID, which is unique value generated by the server.
   *
   * @return the Udp channel convey ID, which is unique value generated by the server
   */
  int getUdpConveyId();

  /**
   * Retrieves a KCP channel that the session is able to use.
   *
   * @return a {@link Ukcp} which is using for communication via KCP channel
   */
  Ukcp getKcpChannel();

  /**
   * Declares a KCP channel that the session is able to use.
   *
   * @param kcpChannel a {@link Ukcp} which is using for communication via KCP
   *                   channel (there will be some channels opened on the server side)
   */
  void setKcpChannel(Ukcp kcpChannel);

  /**
   * Retrieves the remote address associating to the client side whenever the server receives
   * message from him.
   *
   * @return the {@link SocketAddress} remote address associating to the client side
   */
  SocketAddress getDatagramRemoteAddress();

  /**
   * Updates the remote address associating to the client side whenever the server receives
   * message from him.
   *
   * @param datagramRemoteAddress remote address associating to the client side
   */
  void setDatagramRemoteAddress(SocketAddress datagramRemoteAddress);

  /**
   * Retrieves a WebSocket channel which is using for communication between the server and
   * client sides via WebSocket.
   *
   * @return the {@link Channel} WebSocket channel instance
   */
  Channel fetchWebSocketChannel();

  /**
   * Sets a WebSocket channel which is using for communication between the server and client
   * sides via WebSocket.
   *
   * @param webSocketChannel the {@link Channel} WebSocket channel instance
   * @throws IllegalArgumentException when an invalid value is in use
   * @throws IllegalCallerException   when an invalid transportation type is declared
   */
  void configureWebSocketChannel(Channel webSocketChannel)
      throws IllegalArgumentException, IllegalCallerException;

  /**
   * Sets an instance for the connection filter.
   *
   * @param connectionFilter an instance of {@link ConnectionFilter}
   */
  void configureConnectionFilter(ConnectionFilter connectionFilter);

  /**
   * Retrieves the creation time of session.
   *
   * @return the creation time of session in milliseconds ({@code long} value)
   */
  long getCreatedTime();

  /**
   * Retrieves the last activity time of session.
   *
   * @return the last activity time in milliseconds ({@code long} value)
   */
  long getLastActivityTime();

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
   * Retrieves the total number of messages which the session receives from client side.
   *
   * @return the total number of messages ({@code long} value) which the session receives from
   * client side
   * @since 0.5.0
   */
  long getReadMessages();

  /**
   * Increases the total number of read messages which the session receives from client side.
   *
   * @since 0.5.0
   */
  void increaseReadMessages();

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
   * Sets the maximum time in seconds which allows the session to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                session to get in IDLE state
   */
  void configureMaxIdleTimeInSeconds(int seconds);

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
   * Activates the session (To be able to perform actions, such as reading or writing data). This
   * method should be invoked when the first time session created, because even if that session
   * is orphan (without associated player), it should still be able to transfer data.
   */
  void activate();

  /**
   * Retrieves how long since the session is inactivated.
   *
   * @return how long since the session is inactivated in milliseconds ({@code long} value)
   */
  long getInactivatedTime();

  /**
   * Sets a session manager instance.
   *
   * @param sessionManager a {@link SessionManager} instance
   */
  void configureSessionManager(SessionManager sessionManager);

  /**
   * Removes itself from management process.
   */
  void remove();

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

  /**
   * The state values present the phase of associating between a session and a player.
   */
  enum AssociatedState {
    /**
     * The session is not associated with any {@link Player}.
     */
    NONE,
    /**
     * The session is trying to associate with a {@link Player}.
     */
    DOING,
    /**
     * The session is associated with a {@link Player}.
     */
    DONE,
  }
}
