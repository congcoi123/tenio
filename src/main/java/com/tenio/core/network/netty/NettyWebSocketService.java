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

package com.tenio.core.network.netty;

import com.tenio.common.data.DataType;
import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.service.Service;
import java.nio.ByteBuffer;

/**
 * The websockets handler is provided by the <a href="https://netty.io/">Netty</a> library.
 */
public interface NettyWebSocketService extends Service {

  /**
   * Sets size of {@link ByteBuffer} using for the WebSocket to write binaries data down.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for writing
   *                   binaries data
   */
  void setSenderBufferSize(int bufferSize);

  /**
   * Sets size of {@link ByteBuffer} using for the WebSocket to read binaries data from.
   *
   * @param bufferSize the size of {@link ByteBuffer} ({@code integer} value) for reading
   *                   binaries data
   */
  void setReceiverBufferSize(int bufferSize);

  /**
   * Sets the number of producer workers for the WebSocket.
   *
   * @param workerSize the number ({@code integer} value) of producer workers for the WebSocket
   */
  void setProducerWorkerSize(int workerSize);

  /**
   * Sets the number of consumer workers for the WebSocket.
   *
   * @param workerSize the number ({@code integer} value) of consumer workers for the WebSocket
   */
  void setConsumerWorkerSize(int workerSize);

  /**
   * Sets an instance for the connection filter.
   *
   * @param connectionFilter an instance of {@link ConnectionFilter}
   */
  void setConnectionFilter(ConnectionFilter connectionFilter);

  /**
   * Set the data serialization type.
   *
   * @param dataType the {@link DataType} value
   */
  void setDataType(DataType dataType);

  /**
   * Sets a session manager instance.
   *
   * @param sessionManager a {@link SessionManager} instance
   */
  void setSessionManager(SessionManager sessionManager);

  /**
   * Sets a network reader statistic instance which takes responsibility recording the
   * receiving data from clients.
   *
   * @param networkReaderStatistic a {@link NetworkReaderStatistic} instance
   */
  void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic);

  /**
   * Sets a network writer statistic instance which takes responsibility recording the
   * sending data from the network.
   *
   * @param networkWriterStatistic a {@link NetworkWriterStatistic} instance
   */
  void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic);

  /**
   * Declares a socket configurations for the WebSocket.
   *
   * @param socketConfig a instance of {@link SocketConfig}
   */
  void setWebSocketConfig(SocketConfig socketConfig);

  /**
   * Determines whether the WebSocket is able to use the SSL.
   *
   * @param usingSsl sets to {@code true} in case of using SSL, otherwise returns {@code false}
   */
  void setUsingSsl(boolean usingSsl);

  /**
   * Writes down (binaries) data to socket/channel in order to send them to client side.
   *
   * @param packet an instance of {@link Packet} using to carry conveying information
   */
  void write(Packet packet);
}
