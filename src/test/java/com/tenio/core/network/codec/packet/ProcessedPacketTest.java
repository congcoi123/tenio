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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ProcessedPacket")
class ProcessedPacketTest {

  private ProcessedPacket packet;

  @BeforeEach
  void setUp() {
    packet = ProcessedPacket.newInstance();
  }

  @Test
  @DisplayName("newInstance returns a non-null ProcessedPacket")
  void testNewInstanceNotNull() {
    assertNotNull(packet);
  }

  @Test
  @DisplayName("initial data is null")
  void testInitialDataIsNull() {
    assertNull(packet.getData());
  }

  @Test
  @DisplayName("initial packetReadState is null")
  void testInitialPacketReadStateIsNull() {
    assertNull(packet.getPacketReadState());
  }

  @Test
  @DisplayName("setData and getData round-trip")
  void testSetAndGetData() {
    byte[] data = {1, 2, 3};
    packet.setData(data);
    assertArrayEquals(data, packet.getData());
  }

  @Test
  @DisplayName("setPacketReadState and getPacketReadState round-trip")
  void testSetAndGetPacketReadState() {
    packet.setPacketReadState(PacketReadState.WAIT_DATA);
    assertEquals(PacketReadState.WAIT_DATA, packet.getPacketReadState());
  }

  @Test
  @DisplayName("toString reports null when data is null")
  void testToStringWithNullData() {
    var str = packet.toString();
    assertTrue(str.contains("null"));
  }

  @Test
  @DisplayName("toString reports byte count when data is set")
  void testToStringWithData() {
    packet.setData(new byte[5]);
    var str = packet.toString();
    assertTrue(str.contains("5"));
  }

  @Test
  @DisplayName("toString contains packetReadState field")
  void testToStringContainsState() {
    packet.setPacketReadState(PacketReadState.WAIT_NEW_PACKET);
    var str = packet.toString();
    assertTrue(str.contains("WAIT_NEW_PACKET"));
  }
}
