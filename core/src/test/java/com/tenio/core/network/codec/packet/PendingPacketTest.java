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

package com.tenio.core.network.codec.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.DataType;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For PendingPacket")
class PendingPacketTest {

  private PendingPacket packet;

  @BeforeEach
  void setUp() {
    packet = PendingPacket.newInstance();
  }

  @Test
  @DisplayName("newInstance returns a non-null PendingPacket")
  void testNewInstanceNotNull() {
    assertNotNull(packet);
  }

  @Test
  @DisplayName("initial packetHeader is null")
  void testInitialPacketHeaderIsNull() {
    assertNull(packet.getPacketHeader());
  }

  @Test
  @DisplayName("initial buffer is null")
  void testInitialBufferIsNull() {
    assertNull(packet.getBuffer());
  }

  @Test
  @DisplayName("initial expectedLength is zero")
  void testInitialExpectedLengthIsZero() {
    assertEquals(0, packet.getExpectedLength());
  }

  @Test
  @DisplayName("setPacketHeader and getPacketHeader round-trip")
  void testSetAndGetPacketHeader() {
    var header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    packet.setPacketHeader(header);
    assertEquals(header, packet.getPacketHeader());
  }

  @Test
  @DisplayName("setBuffer and getBuffer round-trip")
  void testSetAndGetBuffer() {
    var buf = ByteBuffer.allocate(64);
    packet.setBuffer(buf);
    assertEquals(buf, packet.getBuffer());
  }

  @Test
  @DisplayName("setExpectedLength and getExpectedLength round-trip")
  void testSetAndGetExpectedLength() {
    packet.setExpectedLength(128);
    assertEquals(128, packet.getExpectedLength());
  }

  @Test
  @DisplayName("toString contains field names")
  void testToStringContainsFields() {
    var str = packet.toString();
    assertTrue(str.contains("packetHeader="));
    assertTrue(str.contains("byteBuffer="));
    assertTrue(str.contains("expectedLength="));
  }
}
