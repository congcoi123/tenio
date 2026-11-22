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
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenio.core.network.codec.packet.PacketHeaderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For PacketHeaderType")
class PacketHeaderTypeTest {

  @Test
  @DisplayName("Test all enum values")
  void testAllEnumValues() {
    for (PacketHeaderType type : PacketHeaderType.values()) {
      assertEquals(type.name(), type.toString());
    }
  }

  @Test
  @DisplayName("getByValue returns the correct type for each enum constant")
  void testGetByValueReturnsCorrectType() {
    for (PacketHeaderType type : PacketHeaderType.values()) {
      assertEquals(type, PacketHeaderType.getByValue(type.getValue()));
    }
  }

  @Test
  @DisplayName("getByValue returns null for an unknown byte")
  void testGetByValueUnknownReturnsNull() {
    assertNull(PacketHeaderType.getByValue((byte) 0x00));
  }

  @Test
  @DisplayName("getValue returns the correct bit-mask byte for LENGTH_PREFIXED")
  void testGetValueLengthPrefixed() {
    assertEquals((byte) 0b10000000, PacketHeaderType.LENGTH_PREFIXED.getValue());
  }

  @Test
  @DisplayName("getValue returns the correct bit-mask byte for BIG_SIZE")
  void testGetValueBigSize() {
    assertEquals((byte) 0b01000000, PacketHeaderType.BIG_SIZE.getValue());
  }
}
