/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.entity.packet;

import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.policy.DefaultPacketQueuePolicy;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;

/**
 * The queue using for holding packets to send to clients side.
 *
 * @see Packet
 * @see PacketQueuePolicy
 */
public interface PacketQueue {

  /**
   * Retrieves the last-in packet in the queue.
   *
   * @return the last-in {@link Packet} in the queue
   */
  Packet peek();

  /**
   * Retrieves the last-in packet in the queue and removes it as well.
   *
   * @return the last-in {@link Packet} in the queue
   */
  Packet take();

  /**
   * Determines whether the queue is empty.
   *
   * @return {@code true} if the queue is empty, otherwise returns {@code false}
   */
  boolean isEmpty();

  /**
   * Determines whether the queue is full.
   *
   * @return {@code true} if the queue is full, otherwise returns {@code false}
   */
  boolean isFull();

  /**
   * Retrieves the current size of queue.
   *
   * @return the current {@code integer} size of queue
   */
  int getSize();

  /**
   * Retrieves the maximum allowed size of queue.
   *
   * @return the maximum {@code integer} allowed size of queue
   */
  int getMaxSize();

  /**
   * Sets the maximum allowed size of queue.
   *
   * @param maxSize the maximum {@code integer} allowed size of queue
   */
  void setMaxSize(int maxSize);

  /**
   * Sets a set of rules for managing packets in queue.
   *
   * @param packetQueuePolicy the implementation of {@link PacketQueuePolicy} used to manage
   *                          packets in the queue
   * @see DefaultPacketQueuePolicy
   */
  void setPacketQueuePolicy(PacketQueuePolicy packetQueuePolicy);

  /**
   * Retrieves the current usage of queue.
   *
   * @return the current {@code float} usage of queue. Its range is between 0 and 100 percent
   */
  float getPercentageUsed();

  /**
   * Puts a new packet into queue.
   *
   * @param packet an appended {@link Packet}
   * @throws PacketQueueFullException            when the queue is full and not be able to hold
   *                                             more packets
   * @throws PacketQueuePolicyViolationException when a new appended packet violates the queue
   *                                             policies
   * @see DefaultPacketQueuePolicy
   */
  void put(Packet packet) throws PacketQueueFullException, PacketQueuePolicyViolationException;

  /**
   * Clears all packets from the queue.
   */
  void clear();
}
