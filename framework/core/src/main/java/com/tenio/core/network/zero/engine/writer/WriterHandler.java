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

package com.tenio.core.network.zero.engine.writer;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.SessionTicketsQueueManager;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * Interface for handling network packet writing operations.
 * This interface defines the core functionality for managing packet transmission,
 * session management, and network statistics tracking.
 *
 * <p>Key features:
 * <ul>
 *   <li>Packet transmission management</li>
 *   <li>Session queue handling</li>
 *   <li>Network statistics tracking</li>
 *   <li>Buffer management</li>
 * </ul>
 *
 * <p>Note: This interface is typically implemented by specific writer handlers
 * for different network protocols (TCP, WebSocket, UDP, etc.).
 *
 * @see PacketQueue
 * @see Session
 * @see Packet
 * @see NetworkWriterStatistic
 * @see ByteBuffer
 * @since 0.3.0
 */
public interface WriterHandler {

  /**
   * Sends a packet to a session.
   *
   * @param packetQueue the {@link PacketQueue}
   * @param session     the {@link Session}
   * @param packet      the {@link Packet}
   */
  void send(PacketQueue packetQueue, Session session, Packet packet);

  /**
   * Retrieves a blocking queue of all sessions.
   *
   * @param sessionId the session id of which is used to determine which queue it should belong to
   * @return the blocking queue of all {@link Session}s
   * @see BlockingQueue
   * @see SessionTicketsQueueManager
   */
  BlockingQueue<Session> getSessionTicketsQueue(long sessionId);

  /**
   * Sets a blocking queues manager.
   *
   * @param sessionTicketsQueueManager an instance of {@link SessionTicketsQueueManager} which
   *                                   manages blocking queues of all {@link Session}s
   * @see BlockingQueue
   */
  void setSessionTicketsQueueManager(SessionTicketsQueueManager sessionTicketsQueueManager);

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
   * Retrieves a byte buffer which a socket can read/write binaries data from/down.
   *
   * @return an instance of {@link ByteBuffer}
   */
  ByteBuffer getBuffer();

  /**
   * Allocates a byte buffer capacity.
   *
   * @param capacity the capacity of a byte buffer ({@code Integer} value)
   * @see ByteBuffer
   */
  void allocateBuffer(int capacity);
}
