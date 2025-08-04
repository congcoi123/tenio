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

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;

/**
 * The engine supports writing binaries data to sockets.
 */
public interface ZeroWriter extends ZeroEngine {

  /**
   * Enqueue a packet from the packet queue to process.
   *
   * @param packet the processing {@link Packet}
   * @see PacketQueue
   */
  void enqueuePacket(Packet packet);

  /**
   * Retrieves a network writer statistic instance which takes responsibility recording the
   * sending data from the network.
   *
   * @return a {@link NetworkWriterStatistic} instance
   */
  NetworkWriterStatistic getNetworkWriterStatistic();

  /**
   * Sets a network writer statistic instance which takes responsibility recording the
   * sending data from the network.
   *
   * @param networkWriterStatistic a {@link NetworkWriterStatistic} instance
   */
  void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic);

  /**
   * Sets an instance of packet encoder to encode packets for sending to clients.
   *
   * @param packetEncoder an instance of {@link BinaryPacketEncoder}
   */
  void setPacketEncoder(BinaryPacketEncoder packetEncoder);
}
