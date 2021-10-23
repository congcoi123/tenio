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

package com.tenio.core.network.zero.engine.implement;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.engine.ZeroWriter;
import com.tenio.core.network.zero.engine.listener.ZeroWriterListener;
import com.tenio.core.network.zero.engine.writer.DatagramWriterHandler;
import com.tenio.core.network.zero.engine.writer.SocketWriterHandler;
import com.tenio.core.network.zero.engine.writer.WriterHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The implementation for writer engine.
 *
 * @see ZeroWriter
 */
public final class ZeroWriterImpl extends AbstractZeroEngine
    implements ZeroWriter, ZeroWriterListener {

  private BlockingQueue<Session> sessionTicketsQueue;
  private NetworkWriterStatistic networkWriterStatistic;
  private BinaryPacketEncoder binaryPacketEncoder;

  private ZeroWriterImpl(EventManager eventManager) {
    super(eventManager);

    sessionTicketsQueue = new LinkedBlockingQueue<Session>();

    setName("writer");
  }

  public static ZeroWriter newInstance(EventManager eventManager) {
    return new ZeroWriterImpl(eventManager);
  }

  private WriterHandler createSocketWriterHandler() {
    var socketWriterHandler = SocketWriterHandler.newInstance();
    socketWriterHandler.setNetworkWriterStatistic(networkWriterStatistic);
    socketWriterHandler.setSessionTicketsQueue(sessionTicketsQueue);
    socketWriterHandler.allocateBuffer(getMaxBufferSize());

    return socketWriterHandler;
  }

  private WriterHandler createDatagramWriterHandler() {
    var datagramWriterHandler = DatagramWriterHandler.newInstance();
    datagramWriterHandler.setNetworkWriterStatistic(networkWriterStatistic);
    datagramWriterHandler.setSessionTicketsQueue(sessionTicketsQueue);
    datagramWriterHandler.allocateBuffer(getMaxBufferSize());

    return datagramWriterHandler;
  }

  private void writableLoop(WriterHandler socketWriterHandler,
                            WriterHandler datagramWriterHandler) {
    try {
      var session = sessionTicketsQueue.take();
      processSessionQueue(session, socketWriterHandler, datagramWriterHandler);
    } catch (InterruptedException e) {
      error(e, "Interruption occurred when process a session and its packet");
    }
  }

  private void processSessionQueue(Session session, WriterHandler socketWriterHandler,
                                   WriterHandler datagramWriterHandler) {

    // ignore the null session
    if (session == null) {
      return;
    }

    // now we can iterate packets from queue to proceed
    var packetQueue = session.getPacketQueue();
    synchronized (packetQueue) {
      // ignore the empty queue
      if (packetQueue.isEmpty()) {
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
        enqueuePacket(session, packet.clone());
      }
    }

  }

  private void enqueuePacket(Session session, Packet packet) {
    // check the session state one more time
    if (!session.isActivated()) {
      return;
    }

    // loops through the packet queue and handles its packets
    var packetQueue = session.getPacketQueue();
    if (packetQueue != null) {
      synchronized (packetQueue) {
        try {
          // get the current state first
          boolean isEmpty = packetQueue.isEmpty();
          // now can put new item into the queue
          packetQueue.put(packet);

          // only need when the packet queue is empty or the session was not in the
          // tickets queue
          if (isEmpty || !sessionTicketsQueue.contains(session)) {
            sessionTicketsQueue.add(session);
          }

          packet.setRecipients(null);
        } catch (PacketQueuePolicyViolationException e) {
          session.addDroppedPackets(1);
          networkWriterStatistic.updateWrittenDroppedPacketsByPolicy(1);
        } catch (PacketQueueFullException e) {
          session.addDroppedPackets(1);
          networkWriterStatistic.updateWrittenDroppedPacketsByFull(1);
        }
      }
    }
  }

  @Override
  public void continueWriteInterestOp(Session session) {
    if (session != null) {
      sessionTicketsQueue.add(session);
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
    // do nothing
  }

  @Override
  public void onStarted() {
    // do nothing
  }

  @Override
  public void onRunning() {
    var socketWriterHandler = createSocketWriterHandler();
    var datagramWriterHandler = createDatagramWriterHandler();

    while (true) {
      if (isActivated()) {
        writableLoop(socketWriterHandler, datagramWriterHandler);
      }
    }
  }

  @Override
  public void onShutdown() {
    sessionTicketsQueue.clear();
  }

  @Override
  public void onDestroyed() {
    sessionTicketsQueue = null;
  }
}
