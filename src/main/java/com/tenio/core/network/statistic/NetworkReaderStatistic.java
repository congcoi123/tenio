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

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class supports creating an instance for holding the network reading data from clients side.
 */
public final class NetworkReaderStatistic {

  private final AtomicLong readBytes;
  private final AtomicLong readPackets;
  private final AtomicLong readDroppedPackets;

  private NetworkReaderStatistic() {
    readBytes = new AtomicLong();
    readPackets = new AtomicLong();
    readDroppedPackets = new AtomicLong();
  }

  /**
   * Initialization.
   *
   * @return a new instance of {@link NetworkReaderStatistic}
   */
  public static NetworkReaderStatistic newInstance() {
    return new NetworkReaderStatistic();
  }

  /**
   * Updates the current number of received bytes from clients side.
   *
   * @param numberBytes the additional bytes received from client sides ({@code long} value)
   */
  public void updateReadBytes(long numberBytes) {
    readBytes.addAndGet(numberBytes);
  }

  /**
   * Updates the current number of received packets from clients side.
   *
   * @param numberPackets the additional packets received from client sides ({@code long}
   *                      value)
   */
  public void updateReadPackets(long numberPackets) {
    readPackets.addAndGet(numberPackets);
  }

  /**
   * Updates the current number of refused packets from clients side which violated the policies.
   *
   * @param numberPackets the additional packets refused to handle from clients side
   *                      ({@code long} value)
   */
  public void updateReadDroppedPackets(long numberPackets) {
    readDroppedPackets.addAndGet(numberPackets);
  }

  /**
   * Retrieves the current number of received bytes data from client sides.
   *
   * @return the current number of received bytes data ({@code long} value)
   */
  public long getReadBytes() {
    return readBytes.longValue();
  }

  /**
   * Retrieves the current number of received packets from clients side.
   *
   * @return the current number of received packets ({@code long} value)
   */
  public long getReadPackets() {
    return readPackets.longValue();
  }

  /**
   * Retrieves the current number of dropped packets which are refused to handle.
   *
   * @return the current number of dropped packets ({@code long} value)
   */
  public long getReadDroppedPackets() {
    return readDroppedPackets.longValue();
  }

  @Override
  public String toString() {
    return "NetworkReaderStatistic{" +
        "readBytes=" + readBytes +
        ", readPackets=" + readPackets +
        ", readDroppedPackets=" + readDroppedPackets +
        '}';
  }
}
