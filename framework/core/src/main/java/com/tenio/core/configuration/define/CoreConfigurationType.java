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

package com.tenio.core.configuration.define;

import com.tenio.common.configuration.ConfigurationType;
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
   * The server id (module name).
   */
  SERVER_ID("server-id"),
  /**
   * This current version code of your server in integer type (can be compared).
   */
  SERVER_VERSION_CODE("version-code"),
  /**
   * This current version name of your server in string type.
   */
  SERVER_VERSION_NAME("version-name"),

  CLASS_PACKET_ENCRYPTER("packet-encrypter"),

  CLASS_PACKET_COMPRESSOR("packet-compressor"),

  CLASS_PACKET_ENCODER("packet-encoder"),

  CLASS_PACKET_DECODER("packet-decoder"),

  CLASS_CONNECTION_FILTER("connection-filter"),

  CLASS_PACKET_QUEUE_POLICY("packet-queue-policy"),

  THREADS_SOCKET_ACCEPTOR("socket-acceptor"),

  THREADS_SOCKET_READER("socket-reader"),

  THREADS_SOCKET_WRITER("socket-writer"),

  THREADS_WEBSOCKET_PRODUCER("websocket-producer"),

  THREADS_WEBSOCKET_CONSUMER("websocket-consumer"),

  THREADS_INTERNAL_PROCESSOR("internal-processor"),

  INTERVAL_REMOVED_ROOM_SCAN("removed-room-scan-interval"),

  INTERVAL_DISCONNECTED_PLAYER_SCAN("disconnected-player-scan-interval"),

  INTERVAL_CCU_SCAN("ccu-scan-interval"),

  INTERVAL_DEADLOCK_SCAN("deadlock-scan-interval"),

  INTERVAL_TRAFFIC_COUNTER("traffic-counter-interval"),

  INTERVAL_SYSTEM_MONITORING("system-monitoring-interval"),

  PROP_MAX_PACKET_QUEUE_SIZE("max-packet-queue-size"),

  PROP_MAX_REQUEST_QUEUE_SIZE("max-request-queue-size"),

  PROP_KEEP_PLAYER_ON_DISCONNECTION("keep-player-on-disconnection"),

  PROP_MAX_NUMBER_PLAYERS("max-number-players"),

  PROP_MAX_NUMBER_ROOMS("max-number-rooms"),

  PROP_MAX_PLAYER_IDLE_TIME("max-player-idle-time"),

  NETWORK_PROP_WEBSOCKET_USING_SSL("websocket-using-ssl"),

  NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE("websocket-sender-buffer-size"),

  NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE("websocket-receiver-buffer-size"),

  NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE("socket-acceptor-buffer-size"),

  NETWORK_PROP_SOCKET_READER_BUFFER_SIZE("socket-reader-buffer-size"),

  NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE("socket-writer-buffer-size"),

  NETWORK_PROP_PACKET_COMPRESSION_THRESHOLD_BYTES("packet-compression-threshold-bytes"),

  NETWORK_PROP_MAX_CONNECTIONS_PER_IP("max-connections-per-ip"),

  NETWORK_PROP_ALLOW_CHANGE_SESSION("allow-change-session"),

  /**
   * The list of socket configuration in configuration.
   */
  SOCKET_CONFIGS("socket-configs"),
  /**
   * The list of HTTP configuration in configuration.
   */
  HTTP_CONFIGS("http-configs");

  // Reverse-lookup map for getting a type from a value
  private static final Map<String, CoreConfigurationType> lookup =
      new HashMap<String, CoreConfigurationType>();

  static {
    for (var configurationType : CoreConfigurationType.values()) {
      lookup.put(configurationType.getValue(), configurationType);
    }
  }

  private final String value;

  CoreConfigurationType(final String value) {
    this.value = value;
  }

  public static CoreConfigurationType getByValue(String value) {
    return lookup.get(value);
  }

  public final String getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
