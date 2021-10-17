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

package com.tenio.core.network.statistic;

/**
 * This class supports creating instance for holding the network read data.
 */
public final class NetworkReaderStatistic {

  private volatile long readBytes;
  private volatile long readPackets;
  private volatile long readDroppedPackets;

  private NetworkReaderStatistic() {
    readBytes = 0L;
    readPackets = 0L;
    readDroppedPackets = 0L;
  }

  public static NetworkReaderStatistic newInstance() {
    return new NetworkReaderStatistic();
  }

  public void updateReadBytes(long numberBytes) {
    readBytes += numberBytes;
  }

  public void updateReadPackets(long numberPackets) {
    readPackets += numberPackets;
  }

  public void updateReadDroppedPackets(long numberPackets) {
    readDroppedPackets += numberPackets;
  }

  public long getReadBytes() {
    return readBytes;
  }

  public long getReadPackets() {
    return readPackets;
  }

  public long getReadDroppedPackets() {
    return readDroppedPackets;
  }
}
