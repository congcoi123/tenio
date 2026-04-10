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

package com.tenio.core.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.DataType;
import com.tenio.core.network.codec.packet.PacketHeader;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For CodecUtility")
class CodecUtilityTest {

  @Test
  @DisplayName("Private constructor throws UnsupportedOperationException via reflection")
  void testPrivateConstructorThrows() throws Exception {
    Constructor<CodecUtility> ctor = CodecUtility.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    assertThrows(Exception.class, ctor::newInstance);
  }

  @Test
  @DisplayName("encode/decode round-trip with all flags false and ZERO type")
  void testRoundTripAllFlagsFalseZeroType() {
    PacketHeader original = PacketHeader.newInstance(false, false, false, false, DataType.ZERO);
    byte encoded = CodecUtility.encodeFirstHeaderByte(original);
    PacketHeader decoded = CodecUtility.decodeFirstHeaderByte(encoded);

    assertFalse(decoded.hasLengthPrefixed());
    assertFalse(decoded.isCompressed());
    assertFalse(decoded.isBigSized());
    assertFalse(decoded.isEncrypted());
    assertEquals(DataType.ZERO, decoded.getDataType());
  }

  @Test
  @DisplayName("encode/decode round-trip with all flags true and MSG_PACK type")
  void testRoundTripAllFlagsTrueMsgPackType() {
    PacketHeader original = PacketHeader.newInstance(true, true, true, true, DataType.MSG_PACK);
    byte encoded = CodecUtility.encodeFirstHeaderByte(original);
    PacketHeader decoded = CodecUtility.decodeFirstHeaderByte(encoded);

    assertTrue(decoded.hasLengthPrefixed());
    assertTrue(decoded.isCompressed());
    assertTrue(decoded.isBigSized());
    assertTrue(decoded.isEncrypted());
    assertEquals(DataType.MSG_PACK, decoded.getDataType());
  }

  @Test
  @DisplayName("encode/decode round-trip with only lengthPrefixed true")
  void testRoundTripOnlyLengthPrefixed() {
    PacketHeader original = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    byte encoded = CodecUtility.encodeFirstHeaderByte(original);
    PacketHeader decoded = CodecUtility.decodeFirstHeaderByte(encoded);

    assertTrue(decoded.hasLengthPrefixed());
    assertFalse(decoded.isCompressed());
    assertFalse(decoded.isBigSized());
    assertFalse(decoded.isEncrypted());
    assertEquals(DataType.ZERO, decoded.getDataType());
  }

  @Test
  @DisplayName("encode/decode round-trip with only compressed true")
  void testRoundTripOnlyCompressed() {
    PacketHeader original = PacketHeader.newInstance(false, true, false, false, DataType.ZERO);
    byte encoded = CodecUtility.encodeFirstHeaderByte(original);
    PacketHeader decoded = CodecUtility.decodeFirstHeaderByte(encoded);

    assertFalse(decoded.hasLengthPrefixed());
    assertTrue(decoded.isCompressed());
    assertFalse(decoded.isBigSized());
    assertFalse(decoded.isEncrypted());
  }

  @Test
  @DisplayName("decodeFirstHeaderByte throws on unsupported data type bits")
  void testDecodeUnsupportedDataTypeThrows() {
    // Bits 0b00000010 correspond to value 2 which is not a valid DataType (only 0 and 1 exist)
    byte invalidByte = 0b00000010;
    assertThrows(IllegalArgumentException.class,
        () -> CodecUtility.decodeFirstHeaderByte(invalidByte));
  }
}
