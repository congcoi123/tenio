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

package com.tenio.core.network.zero.engine.implement;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.engine.ZeroWriter;
import com.tenio.core.network.zero.engine.manager.SessionTicketsQueueManager;
import com.tenio.core.network.zero.engine.writer.WriterHandler;
import com.tenio.core.network.zero.engine.writer.implement.DatagramWriterHandler;
import com.tenio.core.network.zero.engine.writer.implement.SocketWriterHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The implementation for writer engine.
 *
 * @see ZeroWriter
 */
public final class ZeroWriterImpl extends AbstractZeroEngine implements ZeroWriter {

  private final AtomicInteger id;
  private SessionTicketsQueueManager sessionTicketsQueueManager;
  private NetworkWriterStatistic networkWriterStatistic;
  private BinaryPacketEncoder binaryPacketEncoder;

  private ZeroWriterImpl(EventManager eventManager) {
    super(eventManager);
    id = new AtomicInteger(0);
    setName("writer");
  }

  /**
   * Creates a new instance of the socket writer.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link ZeroWriter}
   */
  public static ZeroWriter newInstance(EventManager eventManager) {
    return new ZeroWriterImpl(eventManager);
  }

  private WriterHandler createSocketWriterHandler() {
    var socketWriterHandler = SocketWriterHandler.newInstance();
    socketWriterHandler.setNetworkWriterStatistic(networkWriterStatistic);
    socketWriterHandler.setSessionTicketsQueueManager(sessionTicketsQueueManager);
    socketWriterHandler.allocateBuffer(getMaxBufferSize());

    return socketWriterHandler;
  }

  private WriterHandler createDatagramWriterHandler() {
    var datagramWriterHandler = DatagramWriterHandler.newInstance();
    datagramWriterHandler.setNetworkWriterStatistic(networkWriterStatistic);
    datagramWriterHandler.setSessionTicketsQueueManager(sessionTicketsQueueManager);
    datagramWriterHandler.allocateBuffer(getMaxBufferSize());

    return datagramWriterHandler;
  }

  private void writing(BlockingQueue<Session> sessionTicketsQueue,
                       WriterHandler socketWriterHandler,
                       WriterHandler datagramWriterHandler) {
    try {
      Session session = sessionTicketsQueue.take();
      processSessionQueue(session, socketWriterHandler, datagramWriterHandler);
    } catch (Throwable cause) {
      if (isErrorEnabled()) {
        error(cause, "Interruption occurred when process a session and its packet");
      }
    }
  }

  private void processSessionQueue(Session session, WriterHandler socketWriterHandler,
                                   WriterHandler datagramWriterHandler) {
    // ignore a null or inactivated session
    if (session == null) {
      return;
    }

    // now we can iterate packets from queue to proceed
    var packetQueue = session.fetchPacketQueue();
    // ignore the empty queue
    if (packetQueue == null || packetQueue.isEmpty()) {
      return;
    }

    // when the session is in-activated, just ignore its packets
    if (!session.isActivated()) {
      packetQueue.take();
      return;
    }

    var packet = packetQueue.peek();
    // ignore the null packet and remove it from queue
    if (packet == null) {
      if (!packetQueue.isEmpty()) {
        packetQueue.take();
      }

      return;
    }

    if (packet.isTcp()) {
      socketWriterHandler.send(packetQueue, session, packet);
    } else if (packet.isUdp()) {
      datagramWriterHandler.send(packetQueue, session, packet);
    }
  }

  @Override
  public void enqueuePacket(Packet packet) {
    // retrieve all recipient sessions from the packet
    var recipients = packet.getRecipients();
    if (recipients == null) {
      return;
    }

    if (packet.isTcp()) {
      packet = binaryPacketEncoder.encode(packet);
    }

    // when there is only one recipient, no need to create clone packets
    if (recipients.size() == 1) {
      enqueuePacket(recipients.iterator().next(), packet);
    } else {
      var sessionIterator = recipients.iterator();

      // one session needs one packet in its queue, need to clone the packet
      while (sessionIterator.hasNext()) {
        Session session = sessionIterator.next();
        enqueuePacket(session, packet.deepCopy());
      }
    }

  }

  private void enqueuePacket(Session session, Packet packet) {
    // check the session state one more time
    if (!session.isActivated()) {
      return;
    }

    // loops through the packet queue and handles its packets
    var packetQueue = session.fetchPacketQueue();
    if (packetQueue != null) {
      try {
        // put new item into the queue
        packetQueue.put(packet);

        // duplicated entries are expected
        sessionTicketsQueueManager.getQueueByElementId(session.getId()).add(session);

        packet.setRecipients(null);
      } catch (PacketQueuePolicyViolationException exception) {
        session.addDroppedPackets(1);
        networkWriterStatistic.updateWrittenDroppedPacketsByPolicy(1);
      } catch (PacketQueueFullException exception) {
        session.addDroppedPackets(1);
        networkWriterStatistic.updateWrittenDroppedPacketsByFull(1);
      }
    }
  }

  @Override
  public NetworkWriterStatistic getNetworkWriterStatistic() {
    return networkWriterStatistic;
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    this.networkWriterStatistic = networkWriterStatistic;
  }

  @Override
  public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
    binaryPacketEncoder = packetEncoder;
  }

  @Override
  public void onInitialized() {
    sessionTicketsQueueManager = new SessionTicketsQueueManager(getThreadPoolSize());
  }

  @Override
  public void onStarted() {
    // do nothing
  }

  @Override
  public void onRunning() {
    var socketWriterHandler = createSocketWriterHandler();
    var datagramWriterHandler = createDatagramWriterHandler();
    var sessionTicketsQueue = sessionTicketsQueueManager.getQueueByIndex(id.getAndIncrement());

    while (!Thread.currentThread().isInterrupted()) {
      if (isActivated()) {
        writing(sessionTicketsQueue, socketWriterHandler, datagramWriterHandler);
      }
    }
  }

  @Override
  public void onShutdown() {
    sessionTicketsQueueManager.clear();
  }

  @Override
  public void onDestroyed() {
  }
}
