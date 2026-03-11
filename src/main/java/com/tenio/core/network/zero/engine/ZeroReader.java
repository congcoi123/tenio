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

package com.tenio.core.network.zero.engine;

import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;

/**
 * The engine supports reading binaries data from sockets.
 */
public interface ZeroReader extends ZeroEngine {

  /**
   * Declares the server IP address.
   *
   * @param serverAddress the {@link String} value of server IP address
   */
  void setServerAddress(String serverAddress);

  /**
   * Declares UDP configuration for the network.
   *
   * @param udpChannelConfiguration an instance of {@link SocketConfiguration} for UDP
   */
  void setUdpChannelConfiguration(SocketConfiguration udpChannelConfiguration);

  /**
   * Retrieves a network reader statistic instance which takes responsibility recording the
   * receiving data from clients.
   *
   * @return a {@link NetworkReaderStatistic} instance
   */
  NetworkReaderStatistic getNetworkReaderStatistic();

  /**
   * Sets a network reader statistic instance which takes responsibility recording the
   * receiving data from clients.
   *
   * @param networkReaderStatistic a {@link NetworkReaderStatistic} instance
   */
  void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic);

  /**
   * Sets a datagram packet policy.
   *
   * @param datagramPacketPolicy instance of {@link DatagramPacketPolicy}
   * @since 0.6.7
   */
  void setDatagramPacketPolicy(DatagramPacketPolicy datagramPacketPolicy);
}
