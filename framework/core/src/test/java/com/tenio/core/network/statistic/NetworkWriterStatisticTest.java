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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NetworkWriterStatisticTest {
  @Test
  void testNewInstance() {
    NetworkWriterStatistic actualNewInstanceResult = NetworkWriterStatistic.newInstance();
    actualNewInstanceResult.updateWrittenBytes(1L);
    actualNewInstanceResult.updateWrittenDroppedPacketsByFull(1L);
    actualNewInstanceResult.updateWrittenDroppedPacketsByPolicy(1L);
    actualNewInstanceResult.updateWrittenPackets(1L);
    assertEquals(1L, actualNewInstanceResult.getWrittenBytes());
    assertEquals(1L, actualNewInstanceResult.getWrittenDroppedPacketsByFull());
    assertEquals(1L, actualNewInstanceResult.getWrittenDroppedPacketsByPolicy());
    assertEquals(1L, actualNewInstanceResult.getWrittenPackets());
  }

  @Test
  void testNewInstance2() {
    NetworkWriterStatistic actualNewInstanceResult = NetworkWriterStatistic.newInstance();
    assertEquals(0L, actualNewInstanceResult.getWrittenBytes());
    assertEquals(0L, actualNewInstanceResult.getWrittenPackets());
    assertEquals(0L, actualNewInstanceResult.getWrittenDroppedPacketsByPolicy());
    assertEquals(0L, actualNewInstanceResult.getWrittenDroppedPacketsByFull());
  }

  @Test
  void testGetWrittenDroppedPackets() {
    assertEquals(0L, NetworkWriterStatistic.newInstance().getWrittenDroppedPackets());
  }
}
