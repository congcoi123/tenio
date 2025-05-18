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

package com.tenio.core.network.statistic;

import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;

/**
 * Tracks and manages network writing statistics for the server.
 * This class provides thread-safe counters for monitoring bytes written,
 * packets sent, and dropped packets due to policy violations or queue overflow.
 *
 * <p>Key features:
 * <ul>
 *   <li>Thread-safe counters</li>
 *   <li>Bytes written tracking</li>
 *   <li>Packet count monitoring</li>
 *   <li>Dropped packet statistics (policy and queue overflow)</li>
 *   <li>Singleton instance management</li>
 * </ul>
 *
 * @see NetworkReaderStatistic
 * @see PacketQueue
 * @see PacketQueuePolicy
 * @since 0.3.0
 */
public final class NetworkWriterStatistic {

  private volatile long writtenBytes;
  private volatile long writtenPackets;
  private volatile long writtenDroppedPacketsByPolicy;
  private volatile long writtenDroppedPacketsByFull;

  private NetworkWriterStatistic() {
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
    writtenBytes += numberBytes;
  }

  /**
   * Updates the number of sent packets to clients side.
   *
   * @param numberPackets {@code long} value, the number of sent packets to clients side
   */
  public void updateWrittenPackets(long numberPackets) {
    writtenPackets += numberPackets;
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
    writtenDroppedPacketsByPolicy += numberPackets;
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
    writtenDroppedPacketsByFull += numberPackets;
  }

  /**
   * Retrieves the current number of sending bytes data to clients side.
   *
   * @return {@code long} value, the current number of sending bytes data to clients side
   */
  public long getWrittenBytes() {
    return writtenBytes;
  }

  /**
   * Retrieves the current number of sending packets to clients side.
   *
   * @return {@code long} value, the current number of sending packets to clients side
   */
  public long getWrittenPackets() {
    return writtenPackets;
  }

  /**
   * Retrieves the current number of dropped packets which violated policies and not be able to
   * send to clients side.
   *
   * @return {@code long} value, the number of dropped packets which violated policies
   * @see PacketQueuePolicy
   */
  public long getWrittenDroppedPacketsByPolicy() {
    return writtenDroppedPacketsByPolicy;
  }

  /**
   * Retrieves the current number of dropped packets which cannot append to the full queue and
   * not be able to send to clients side.
   *
   * @return the number of dropped packets which cannot append to a full queue
   * @see PacketQueuePolicy
   */
  public long getWrittenDroppedPacketsByFull() {
    return writtenDroppedPacketsByFull;
  }

  /**
   * Retrieves the current number of dropped packets which are not able to send to clients side.
   *
   * @return the number of dropped packets which are not able to send to clients side
   * @see #getWrittenDroppedPacketsByPolicy
   * @see #getWrittenDroppedPacketsByFull
   */
  public long getWrittenDroppedPackets() {
    return writtenDroppedPacketsByPolicy + writtenDroppedPacketsByFull;
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
