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

package com.tenio.core.network.zero.engine.writer;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * The APIs designed for writing binary to socket.
 */
public interface WriterHandler {

  /**
   * Send a packet to a session.
   *
   * @param packetQueue the packet queue
   * @param session     the session
   * @param packet      the packet
   */
  void send(PacketQueue packetQueue, Session session, Packet packet);

  /**
   * Retrieves the blocking queue of sessions.
   *
   * @return the blocking queue
   */
  BlockingQueue<Session> getSessionTicketsQueue();

  /**
   * Set the blocking queue of sessions.
   *
   * @param sessionTicketsQueue the blocking queue
   */
  void setSessionTicketsQueue(BlockingQueue<Session> sessionTicketsQueue);

  /**
   * Retrieves the network writer statistic.
   *
   * @return the network writer statistic object
   */
  NetworkWriterStatistic getNetworkWriterStatistic();

  /**
   * Set the network writer statistic object.
   *
   * @param networkWriterStatistic the network writer statistic object
   */
  void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic);

  /**
   * Retrieves the byte buffer.
   *
   * @return the byte buffer
   */
  ByteBuffer getBuffer();

  /**
   * a
   * Allocates a byte buffer.
   *
   * @param capacity the capacity
   */
  void allocateBuffer(int capacity);
}
