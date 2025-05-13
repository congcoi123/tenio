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

package com.tenio.core.configuration.define;

import com.tenio.common.configuration.ConfigurationType;
import com.tenio.common.data.DataType;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * <b>configuration.example.xml</b>. You can also extend this file to create your own
 * configuration values.
 */
public enum CoreConfigurationType implements ConfigurationType {

  /**
   * The server name.
   */
  SERVER_NAME("server-name"),
  /**
   * The server ID (module name).
   */
  SERVER_ID("server-id"),
  /**
   * The server address.
   *
   * @since 0.3.0
   */
  SERVER_ADDRESS("server-address"),
  /**
   * This server version code in numeric type (It can be used to compare to other versions).
   */
  SERVER_VERSION_CODE("version-code"),
  /**
   * This server version code in text type.
   */
  SERVER_VERSION_NAME("version-name"),
  /**
   * This server's core setting file's path.
   *
   * @since 0.4.0
   */
  SERVER_SETTING("setting"),
  /**
   * The number of threads using for handlers to accept new incoming client socket on the server.
   */
  WORKER_SOCKET_ACCEPTOR("socket-acceptor"),
  /**
   * The number of threads using for handlers to read new messages from client sockets on the
   * server.
   */
  WORKER_SOCKET_READER("socket-reader"),
  /**
   * The number of threads using for handlers to write new messages to client sockets on the server.
   */
  WORKER_SOCKET_WRITER("socket-writer"),
  /**
   * The number of threads using for handlers of WebSocket producers on the server.
   */
  WORKER_WEBSOCKET_PRODUCER("websocket-producer"),
  /**
   * The number of threads using for handlers of WebSocket consumers on the server.
   */
  WORKER_WEBSOCKET_CONSUMER("websocket-consumer"),
  /**
   * The thread pool size configured for Jetty server.
   *
   * @since 0.5.0
   */
  WORKER_HTTP_WORKER("http-worker"),
  /**
   * The number of threads using for handlers to manage internal processes on the server.
   */
  WORKER_INTERNAL_PROCESSOR("internal-processor"),
  /**
   * Sets an interval to frequently check removable rooms for removing them.
   *
   * @see RoomRemoveMode
   */
  INTERVAL_REMOVED_ROOM_SCAN("removed-room-scan-interval"),
  /**
   * Sets an interval to frequently check disconnected players. In case a disconnected time of a
   * player excess the allowed time then that player will be logged out from the server.
   *
   * @see PlayerDisconnectMode
   */
  INTERVAL_DISCONNECTED_PLAYER_SCAN("disconnected-player-scan-interval"),
  /**
   * Sets an interval to frequently check the concurrent users activating on the server. Disable
   * this feature by setting the value to {@code 0}.
   *
   * @since 0.5.1
   */
  INTERVAL_CCU_SCAN("ccu-scan-interval"),
  /**
   * Sets an interval to frequently check whether a deadlock occurred.Disable
   * this feature by setting the value to {@code 0}.
   *
   * @since 0.5.1
   */
  INTERVAL_DEADLOCK_SCAN("deadlock-scan-interval"),
  /**
   * Sets an interval to frequently report the current read and written number of packets on the
   * server. Disable this feature by setting the value to {@code 0}.
   *
   * @since 0.5.1
   */
  INTERVAL_TRAFFIC_COUNTER("traffic-counter-interval"),
  /**
   * Sets an interval to frequently monitoring the server information. Disable
   * this feature by setting the value to {@code 0}.
   *
   * @since 0.5.1
   */
  INTERVAL_SYSTEM_MONITORING("system-monitoring-interval"),
  /**
   * Sets the data serialization mechanism is in use, currently, there are 2 types supported:
   * (internal) zero and msgpack.
   *
   * @see DataType
   */
  DATA_SERIALIZATION("data-serialization"),
  /**
   * Sets the maximum size of a packet queue. Notes that every {@link Session} has its own queue,
   * and this setting applies for all of them.
   *
   * @see PacketQueuePolicy
   * @see PacketQueueFullException
   */
  PROP_MAX_PACKET_QUEUE_SIZE("max-packet-queue-size"),
  /**
   * Sets the maximum number of requesting packets in queue. In case there are more packets than
   * expected, some of them should be removed.
   */
  PROP_MAX_REQUEST_QUEUE_SIZE("max-request-queue-size"),
  /**
   * Determines whether a disconnected connection could be held for a while or be removed
   * immediately.
   */
  PROP_KEEP_PLAYER_ON_DISCONNECTION("keep-player-on-disconnection"),
  /**
   * Sets the maximum number of players allowed to join the server.
   */
  PROP_MAX_NUMBER_PLAYERS("max-number-players"),
  /**
   * Sets the maximum number of rooms could be created on the server.
   */
  PROP_MAX_NUMBER_ROOMS("max-number-rooms"),
  /**
   * Sets the maximum time in seconds a player can be in IDLE state (Without sending or receiving
   * packets). Excesses this time then the player will be removed from the server.
   */
  PROP_MAX_PLAYER_IDLE_TIME("max-player-idle-time"),
  /**
   * Sets the maximum time in seconds a player can be in IDLE state (Without sending or receiving
   * packets) in case of never deported selection. Excesses this time then the player will be
   * removed from the server.
   *
   * @since 0.5.0
   */
  PROP_MAX_PLAYER_IDLE_TIME_NEVER_DEPORTED("max-player-idle-time-never-deported"),
  /**
   * Determines whether the WebSocket connection could use SSL configuration.
   */
  NETWORK_PROP_WEBSOCKET_USING_SSL("websocket-using-ssl"),
  /**
   * Sets packet handling buffer size in bytes for the WebSocket sender.
   */
  NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE("websocket-sender-buffer-size"),
  /**
   * Sets packet handling buffer size in bytes for the WebSocket receiver.
   */
  NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE("websocket-receiver-buffer-size"),
  /**
   * Sets packet handling buffer size in bytes for the socket acceptor (Accepting new incoming
   * client sockets).
   */
  NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE("socket-acceptor-buffer-size"),
  /**
   * Sets packet handling buffer size in bytes for the socket receiver.
   */
  NETWORK_PROP_SOCKET_READER_BUFFER_SIZE("socket-reader-buffer-size"),
  /**
   * Sets packet handling buffer size in bytes for the socket sender.
   */
  NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE("socket-writer-buffer-size"),
  /**
   * Sets packet compression threshold in bytes at that the packet will be compressed.
   */
  NETWORK_PROP_PACKET_COMPRESSION_THRESHOLD_BYTES("packet-compression-threshold-bytes"),
  /**
   * Sets maximum number of connections each IP address can have.
   */
  NETWORK_PROP_MAX_CONNECTIONS_PER_IP("max-connections-per-ip"),
  /**
   * Socket in the server configuration.
   *
   * @since 0.5.0
   */
  NETWORK_TCP("tcp-configuration"),
  /**
   * WebSocket in the server configuration.
   *
   * @since 0.5.0
   */
  NETWORK_WEBSOCKET("websocket-configuration"),
  /**
   * KCP in the server configuration.
   *
   * @since 0.6.0
   */
  NETWORK_KCP("kcp-configuration"),
  /**
   * UDP in the server configuration.
   *
   * @since 0.6.0
   */
  NETWORK_UDP("udp-configuration"),
  /**
   * HTTP in the server configuration.
   *
   * @since 0.5.0
   */
  NETWORK_HTTP("http-configuration");

  // Reverse-lookup map for getting a type from a value
  private static final Map<String, CoreConfigurationType> lookup = new HashMap<>();

  static {
    for (var configurationType : CoreConfigurationType.values()) {
      lookup.put(configurationType.getValue(), configurationType);
    }
  }

  private final String value;

  CoreConfigurationType(final String value) {
    this.value = value;
  }

  /**
   * Retrieves a configuration type by using its value.
   *
   * @param value the configuration's {@link String} value
   * @return a corresponding {@link CoreConfigurationType} instance
   */
  public static CoreConfigurationType getByValue(String value) {
    return lookup.get(value);
  }

  /**
   * Retrieves a configuration type's value.
   *
   * @return a {@link String} value
   */
  public final String getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
