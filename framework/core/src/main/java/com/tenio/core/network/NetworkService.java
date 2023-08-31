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
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Map;
import javax.servlet.http.HttpServlet;

/**
 * All designed APIs for the network services.
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
   * Sets an implementation class for the connection filter.
   *
   * @param clazz               an implementation class of {@link ConnectionFilter}
   * @param maxConnectionsPerIp an {@code integer} value, the maximum number of connections
   *                            allowed in a same IP address
   * @throws InstantiationException    it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws IllegalAccessException    it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws IllegalArgumentException  it is related to the illegal argument exception
   * @throws InvocationTargetException it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws NoSuchMethodException     it is caused by
   *                                   {@link Class#getDeclaredConstructor(Class[])}
   * @throws SecurityException         it is related to the security exception
   * @see DefaultConnectionFilter
   */
  void setConnectionFilterClass(Class<? extends ConnectionFilter> clazz, int maxConnectionsPerIp)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException;

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
   * Declares the number of Udp channel will be opened on the server.
   *
   * @param amountUdpWorkers the number of opening Udp channels
   */
  void setSocketAcceptorAmountUdpWorkers(int amountUdpWorkers);

  /**
   * Sets the number of acceptor workers for the socket (TCP) which are using to accept new coming
   * clients.
   *
   * @param workerSize the number of acceptor workers for the socket ({@code integer} value)
   */
  void setSocketAcceptorWorkers(int workerSize);

  /**
   * Determines if UDP channels can use KCP transportation for communication.
   *
   * @param enabledKcp sets it {@code true} if enabled, otherwise sets it {code false}
   */
  void setSocketAcceptorEnabledKcp(boolean enabledKcp);

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
   * @param socketConfiguration    a {@link SocketConfiguration} instance for TCP
   * @param webSocketConfiguration a {@link SocketConfiguration} instance for WebSocket
   */
  void setSocketConfiguration(SocketConfiguration socketConfiguration,
                              SocketConfiguration webSocketConfiguration);

  /**
   * Determines if UDP channels can use KCP transportation for communication.
   *
   * @param enabledKcp sets it {@code true} if enabled, otherwise sets it {code false}
   */
  void setSessionEnabledKcp(boolean enabledKcp);

  /**
   * Sets the maximum time in seconds which allows the session to get in IDLE state (Do not
   * perform any action, such as reading or writing data).
   *
   * @param seconds the maximum time in seconds ({@code integer} value) which allows the
   *                session to get in IDLE state
   */
  void setSessionMaxIdleTimeInSeconds(int seconds);

  /**
   * Sets a packet queue policy class for the network.
   *
   * @param clazz the implementation class of {@link PacketQueuePolicy} used to apply rules for
   *              the packet queue
   * @throws InstantiationException    it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws IllegalAccessException    it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws IllegalArgumentException  it is related to the illegal argument exception
   * @throws InvocationTargetException it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws NoSuchMethodException     it is caused by
   *                                   {@link Class#getDeclaredConstructor(Class[])}
   * @throws SecurityException         it is related to the security exception
   * @see PacketQueue
   * @see DefaultPacketQueuePolicy
   */
  void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException;

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
