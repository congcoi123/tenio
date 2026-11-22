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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.DataType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For PacketHeader")
class PacketHeaderTest {

  @Test
  @DisplayName("newInstance returns a non-null PacketHeader")
  void testNewInstanceNotNull() {
    assertNotNull(PacketHeader.newInstance(false, false, false, false, DataType.ZERO));
  }

  @Test
  @DisplayName("newInstance with null DataType throws IllegalArgumentException")
  void testNewInstanceNullDataTypeThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> PacketHeader.newInstance(false, false, false, false, null));
  }

  @Test
  @DisplayName("hasLengthPrefixed returns true when set")
  void testHasLengthPrefixed() {
    var header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    assertTrue(header.hasLengthPrefixed());
  }

  @Test
  @DisplayName("hasLengthPrefixed returns false when not set")
  void testHasLengthPrefixedFalse() {
    var header = PacketHeader.newInstance(false, false, false, false, DataType.ZERO);
    assertFalse(header.hasLengthPrefixed());
  }

  @Test
  @DisplayName("isCompressed returns true when set")
  void testIsCompressed() {
    var header = PacketHeader.newInstance(false, true, false, false, DataType.ZERO);
    assertTrue(header.isCompressed());
  }

  @Test
  @DisplayName("isBigSized returns true when set")
  void testIsBigSized() {
    var header = PacketHeader.newInstance(false, false, true, false, DataType.ZERO);
    assertTrue(header.isBigSized());
  }

  @Test
  @DisplayName("isEncrypted returns true when set")
  void testIsEncrypted() {
    var header = PacketHeader.newInstance(false, false, false, true, DataType.ZERO);
    assertTrue(header.isEncrypted());
  }

  @Test
  @DisplayName("getDataType returns the DataType passed at creation")
  void testGetDataType() {
    var header = PacketHeader.newInstance(false, false, false, false, DataType.MSG_PACK);
    assertEquals(DataType.MSG_PACK, header.getDataType());
  }

  @Test
  @DisplayName("toString contains all field values")
  void testToStringContainsFields() {
    var header = PacketHeader.newInstance(true, true, true, true, DataType.ZERO);
    var str = header.toString();
    assertTrue(str.contains("lengthPrefixed=true"));
    assertTrue(str.contains("compressed=true"));
    assertTrue(str.contains("bigSized=true"));
    assertTrue(str.contains("encrypted=true"));
    assertTrue(str.contains("ZERO"));
  }
}
