/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.entity.outbound.packet.implement;

import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.outbound.packet.policy.OutboundQueuePolicy;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The implementation for outbound queue.
 *
 * @see OutboundQueue
 */
public final class OutboundQueueImpl implements OutboundQueue {

  private final Queue<Packet> queue;
  private volatile int snapshotSize;
  private OutboundQueuePolicy outboundQueuePolicy;
  private int maxSize;

  /**
   * Constructor.
   */
  private OutboundQueueImpl() {
    queue = new LinkedList<>();
  }

  /**
   * Creates a new instance of an outbound queue.
   *
   * @return a new instance of {@link OutboundQueue}
   */
  public static OutboundQueueImpl newInstance() {
    return new OutboundQueueImpl();
  }

  @Override
  public Packet peek() {
    synchronized (queue) {
      return queue.peek();
    }
  }

  @Override
  public Packet take() {
    synchronized (queue) {
      Packet packet = queue.poll();
      snapshotSize = queue.size();
      return packet;
    }
  }

  @Override
  public boolean isSnapshotEmpty() {
    return snapshotSize == 0;
  }

  @Override
  public boolean isEmpty() {
    synchronized (queue) {
      snapshotSize = queue.size();
      return isSnapshotEmpty();
    }
  }

  @Override
  public boolean isSnapshotFull() {
    return snapshotSize >= maxSize;
  }

  @Override
  public boolean isFull() {
    synchronized (queue) {
      snapshotSize = queue.size();
      return isSnapshotFull();
    }
  }

  @Override
  public int getSnapshotSize() {
    return snapshotSize;
  }

  @Override
  public int getSize() {
    synchronized (queue) {
      snapshotSize = queue.size();
      return getSnapshotSize();
    }
  }

  @Override
  public void configureMaxSize(int maxSize) {
    if (maxSize <= 0) {
      maxSize = Integer.MAX_VALUE;
    }
    this.maxSize = maxSize;
  }

  @Override
  public void configureOutboundQueuePolicy(OutboundQueuePolicy outboundQueuePolicy) {
    this.outboundQueuePolicy = outboundQueuePolicy;
  }

  @Override
  public float getPercentageUsed() {
    return maxSize == 0 ? 0.0f : (((float) snapshotSize * 100) / maxSize);
  }

  @Override
  public void put(Packet packet) {
    outboundQueuePolicy.applyPolicy(this, packet);
    synchronized (queue) {
      queue.add(packet);
      snapshotSize = queue.size();
    }
  }

  @Override
  public void clear() {
    synchronized (queue) {
      queue.clear();
      snapshotSize = 0;
    }
  }

  @Override
  public String toString() {
    return "OutboundQueue{" +
        "queue=" + queue +
        ", outboundQueuePolicy=" + outboundQueuePolicy +
        ", maxSize=" + maxSize +
        ", snapshotSize=" + snapshotSize +
        '}';
  }
}
