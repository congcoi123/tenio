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
 * This class supports creating instance for holding the network written data.
 */
public final class NetworkWriterStatistic {

  private volatile long writtenBytes;
  private volatile long writtenPackets;
  private volatile long writtenDroppedPacketsByPolicy;
  private volatile long writtenDroppedPacketsByFull;

  private NetworkWriterStatistic() {
    writtenBytes = 0L;
    writtenPackets = 0L;
    writtenDroppedPacketsByPolicy = 0L;
    writtenDroppedPacketsByFull = 0L;
  }

  public static NetworkWriterStatistic newInstance() {
    return new NetworkWriterStatistic();
  }

  public void updateWrittenBytes(long numberBytes) {
    writtenBytes += numberBytes;
  }

  public void updateWrittenPackets(long numberPackets) {
    writtenPackets += numberPackets;
  }

  public void updateWrittenDroppedPacketsByPolicy(long numberPackets) {
    writtenDroppedPacketsByPolicy += numberPackets;
  }

  public void updateWrittenDroppedPacketsByFull(long numberPackets) {
    writtenDroppedPacketsByFull += numberPackets;
  }

  public long getWrittenBytes() {
    return writtenBytes;
  }

  public long getWrittenPackets() {
    return writtenPackets;
  }

  public long getWrittenDroppedPacketsByPolicy() {
    return writtenDroppedPacketsByPolicy;
  }

  public long getWrittenDroppedPacketsByFull() {
    return writtenDroppedPacketsByFull;
  }

  public long getWrittenDroppedPackets() {
    return writtenDroppedPacketsByPolicy + writtenDroppedPacketsByFull;
  }
}
