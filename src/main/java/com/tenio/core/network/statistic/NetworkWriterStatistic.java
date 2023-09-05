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

package com.tenio.core.network.statistic;

import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class supports creating an instance for holding the network written data to clients side.
 */
public final class NetworkWriterStatistic {

  private final AtomicLong writtenBytes;
  private final AtomicLong writtenPackets;
  private final AtomicLong writtenDroppedPacketsByPolicy;
  private final AtomicLong writtenDroppedPacketsByFull;

  private NetworkWriterStatistic() {
    writtenBytes = new AtomicLong();
    writtenPackets = new AtomicLong();
    writtenDroppedPacketsByPolicy = new AtomicLong();
    writtenDroppedPacketsByFull = new AtomicLong();
  }

  /**
   * Initialization.
   *
   * @return a new instance of {@link NetworkWriterStatistic}
   */
  public static NetworkWriterStatistic newInstance() {
    return new NetworkWriterStatistic();
  }

  /**
   * Updates the number of sent bytes data to clients side.
   *
   * @param numberBytes {@code long} value, the number of sent bytes data to clients side
   */
  public void updateWrittenBytes(long numberBytes) {
    writtenBytes.addAndGet(numberBytes);
  }

  /**
   * Updates the number of sent packets to clients side.
   *
   * @param numberPackets {@code long} value, the number of sent packets to clients side
   */
  public void updateWrittenPackets(long numberPackets) {
    writtenPackets.addAndGet(numberPackets);
  }

  /**
   * Updates the number of dropped packets which violated policies and not be able to send to
   * clients side.
   *
   * @param numberPackets {@code long} value, the number of dropped packets which violated
   *                      policies
   * @see PacketQueuePolicy
   */
  public void updateWrittenDroppedPacketsByPolicy(long numberPackets) {
    writtenDroppedPacketsByPolicy.addAndGet(numberPackets);
  }

  /**
   * Updates the number of dropped packets which cannot append to a full queue and not be able to
   * send to clients side.
   *
   * @param numberPackets {@code long} value, the number of dropped packets which cannot
   *                      append to the full queue
   * @see PacketQueuePolicy
   */
  public void updateWrittenDroppedPacketsByFull(long numberPackets) {
    writtenDroppedPacketsByFull.addAndGet(numberPackets);
  }

  /**
   * Retrieves the current number of sending bytes data to clients side.
   *
   * @return {@code long} value, the current number of sending bytes data to clients side
   */
  public long getWrittenBytes() {
    return writtenBytes.longValue();
  }

  /**
   * Retrieves the current number of sending packets to clients side.
   *
   * @return {@code long} value, the current number of sending packets to clients side
   */
  public long getWrittenPackets() {
    return writtenPackets.longValue();
  }

  /**
   * Retrieves the current number of dropped packets which violated policies and not be able to
   * send to clients side.
   *
   * @return {@code long} value, the number of dropped packets which violated policies
   * @see PacketQueuePolicy
   */
  public long getWrittenDroppedPacketsByPolicy() {
    return writtenDroppedPacketsByPolicy.longValue();
  }

  /**
   * Retrieves the current number of dropped packets which cannot append to the full queue and
   * not be able to send to clients side.
   *
   * @return the number of dropped packets which cannot append to a full queue
   * @see PacketQueuePolicy
   */
  public long getWrittenDroppedPacketsByFull() {
    return writtenDroppedPacketsByFull.longValue();
  }

  /**
   * Retrieves the current number of dropped packets which are not able to send to clients side.
   *
   * @return the number of dropped packets which are not able to send to clients side
   * @see #getWrittenDroppedPacketsByPolicy
   * @see #getWrittenDroppedPacketsByFull
   */
  public long getWrittenDroppedPackets() {
    return writtenDroppedPacketsByPolicy.longValue() + writtenDroppedPacketsByFull.longValue();
  }

  @Override
  public String toString() {
    return "NetworkWriterStatistic{" +
        "writtenBytes=" + writtenBytes +
        ", writtenPackets=" + writtenPackets +
        ", writtenDroppedPacketsByPolicy=" + writtenDroppedPacketsByPolicy +
        ", writtenDroppedPacketsByFull=" + writtenDroppedPacketsByFull +
        '}';
  }
}
