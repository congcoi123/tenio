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

package com.tenio.core.network;

import com.tenio.common.data.DataType;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.packet.policy.DefaultPacketQueuePolicy;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.security.filter.DefaultConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.service.Service;
import jakarta.servlet.http.HttpServlet;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Defines the core network service functionality for the game server.
 * This interface provides comprehensive network management capabilities including
 * HTTP, WebSocket, TCP, UDP, and KCP protocol support.
 *
 * <p>Key features:
 * <ul>
 *   <li>Multi-protocol support (HTTP, WebSocket, TCP, UDP, KCP)</li>
 *   <li>Connection filtering and security</li>
 *   <li>Configurable worker thread pools</li>
 *   <li>Buffer size management</li>
 *   <li>SSL/TLS support for WebSocket</li>
 *   <li>Network statistics tracking</li>
 *   <li>Packet queue management</li>
 * </ul>
 *
 * <p>Configuration categories:
 * <ul>
 *   <li>HTTP Service: Web server configuration and servlet mapping</li>
 *   <li>WebSocket Service: Real-time communication settings</li>
 *   <li>Socket Service: TCP/UDP connection management</li>
 *   <li>KCP Service: Reliable UDP implementation</li>
 *   <li>Security: Connection filtering and SSL/TLS</li>
 * </ul>
 *
 * <p>Note: This service is responsible for managing all network-related
 * operations and should be properly configured before starting the server.
 *
 * @see NetworkServiceImpl
 * @see SessionManager
 * @see ConnectionFilter
 * @see PacketQueue
 * @since 0.3.0
 */
public interface NetworkService extends Service {

  /**
   * Declares a collection of path configurations for the HTTP service.
   *
   * @param threadPoolSize the number of workers in HTTP service
   * @param port           the port number for the HTTP service
   * @param servletMap     a collection of {@link HttpServlet}
   * @see Map
   */
  void setHttpConfiguration(int threadPoolSize, int port, Map<String, HttpServlet> servletMap);

  /**
   * Sets a connection filter.
   *
   * @param connectionFilter    instance of {@link ConnectionFilter}
   * @param maxConnectionsPerIp maximum number of connections per ip address
   * @see DefaultConnectionFilter
   */
  void setConnectionFilterClass(ConnectionFilter connectionFilter, int maxConnectionsPerIp);

  /**
   * Sets the number of consumer workers for the WebSocket.
   *
   * @param workerSize the number ({@code integer} value) of consumer workers for the WebSocket
   */
  void setWebSocketConsumerWorkers(int workerSize);

  /**
   * Sets the number of producer workers for the WebSocket.
   *
   * @param workerSize the number ({@code integer} value) of producer workers for the WebSocket
   */
  void setWebSocketProducerWorkers(int workerSize);

  /**
   * Sets size of {@link ByteBuffer} using for the WebSocket to write binaries data down.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for writing
   *                   binaries data
   */
  void setWebSocketSenderBufferSize(int bufferSize);

  /**
   * Sets size of {@link ByteBuffer} using for the WebSocket to read binaries data from.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for reading
   *                   binaries data
   */
  void setWebSocketReceiverBufferSize(int bufferSize);

  /**
   * Determines whether the WebSocket is able to use the SSL.
   *
   * @param usingSsl sets to {@code true} in case of using SSL, otherwise returns {@code false}
   */
  void setWebSocketUsingSsl(boolean usingSsl);

  /**
   * Declares the server IP address.
   *
   * @param serverAddress the {@link String} value of server IP address
   */
  void setSocketAcceptorServerAddress(String serverAddress);

  /**
   * Sets the number of acceptor workers for the socket (TCP) which are using to accept new coming
   * clients.
   *
   * @param workerSize the number of acceptor workers for the socket ({@code integer} value)
   */
  void setSocketAcceptorWorkers(int workerSize);

  /**
   * Sets the number of reader workers for the socket (TCP) which are using to read coming packets
   * from clients side.
   *
   * @param workerSize the number of reader workers for the socket ({@code integer} value)
   */
  void setSocketReaderWorkers(int workerSize);

  /**
   * Sets the number of writer workers for the socket (TCP) which are using to send packets to
   * clients side.
   *
   * @param workerSize the number of writer workers for the socket ({@code integer} value)
   */
  void setSocketWriterWorkers(int workerSize);

  /**
   * Sets size of {@link ByteBuffer} using for an acceptor worker to read/write binaries data
   * from/down.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for
   *                   reading/writing binaries data
   */
  void setSocketAcceptorBufferSize(int bufferSize);

  /**
   * Sets size of {@link ByteBuffer} using for a reader worker to read/write binaries data
   * from/down.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for
   *                   reading/writing binaries data
   */
  void setSocketReaderBufferSize(int bufferSize);

  /**
   * Sets size of {@link ByteBuffer} using for a writer worker to read/write binaries data
   * from/down.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for
   *                   reading/writing binaries data
   */
  void setSocketWriterBufferSize(int bufferSize);

  /**
   * Declares socket configurations for the network.
   *
   * @param tcpSocketConfiguration  a {@link SocketConfiguration} instance for TCP
   * @param udpChannelConfiguration a {@link SocketConfiguration} instance for UDP
   * @param webSocketConfiguration  a {@link SocketConfiguration} instance for WebSocket
   * @param kcpSocketConfiguration  a {@link SocketConfiguration} instance for KCP
   */
  void setSocketConfigurations(SocketConfiguration tcpSocketConfiguration,
                               SocketConfiguration udpChannelConfiguration,
                               SocketConfiguration webSocketConfiguration,
                               SocketConfiguration kcpSocketConfiguration);

  /**
   * Sets the maximum time in seconds which allows the session to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                session to get in IDLE state
   */
  void setSessionMaxIdleTimeInSeconds(int seconds);

  /**
   * Sets an instance of packet queue policy the network.
   *
   * @param packetQueuePolicy instance of {@link PacketQueuePolicy}
   * @see PacketQueue
   * @see DefaultPacketQueuePolicy
   */
  void setPacketQueuePolicy(PacketQueuePolicy packetQueuePolicy);

  /**
   * Sets the packet queue size.
   *
   * @param queueSize the new size ({@code integer} value) for the packet queue
   * @see PacketQueue
   * @see PacketQueuePolicy
   */
  void setPacketQueueSize(int queueSize);

  /**
   * Sets an instance of packet encoder to encode packets for sending to clients side via the
   * socket (TCP).
   *
   * @param packetEncoder an instance of {@link BinaryPacketEncoder}
   */
  void setPacketEncoder(BinaryPacketEncoder packetEncoder);

  /**
   * Sets an instance of packet decoder to decode packets sent from clients side via the socket
   * (TCP).
   *
   * @param packetDecoder an instance of {@link BinaryPacketDecoder}
   */
  void setPacketDecoder(BinaryPacketDecoder packetDecoder);

  /**
   * Set the data serialization type.
   *
   * @param dataType the {@link DataType} value
   */
  void setDataType(DataType dataType);

  /**
   * Retrieves the session manager instance.
   *
   * @return an instance of {@link SessionManager}
   */
  SessionManager getSessionManager();

  /**
   * Retrieves a network reader statistic instance which takes responsibility recording the
   * receiving data from clients.
   *
   * @return a {@link NetworkReaderStatistic} instance
   */
  NetworkReaderStatistic getNetworkReaderStatistic();

  /**
   * Retrieves a network writer statistic instance which takes responsibility recording the
   * sending data from the network.
   *
   * @return a {@link NetworkWriterStatistic} instance
   */
  NetworkWriterStatistic getNetworkWriterStatistic();

  /**
   * Writes down (binaries) data to socket/channel in order to send them to clients side.
   *
   * @param response     an instance of {@link Response} using to carry conveying information
   * @param markedAsLast marks as this writing is the last one
   */
  void write(Response response, boolean markedAsLast);
}
