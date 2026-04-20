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

package com.tenio.core.network.codec.decoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataType;
import com.tenio.common.data.zero.utility.ZeroUtility;
import com.tenio.core.network.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.codec.packet.PacketHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.msgpack.core.MessageTypeException;

@DisplayName("Unit Test Cases For BinaryPacketDecoderImpl")
class BinaryPacketDecoderImplTest {

  private BinaryPacketDecoderImpl decoder;

  @BeforeEach
  void setUp() {
    decoder = new BinaryPacketDecoderImpl();
  }

  @Test
  @DisplayName("Decode null data should throw an exception")
  void testDecodeNullData() {
    assertThrows(NullPointerException.class, () -> decoder.decode(null));
  }

  @Test
  @DisplayName("Decode invalid data should throw an exception")
  void testDecodeInvalidData() {
    byte[] binaries = new byte[] {1, 2, 3};
    assertThrows(MessageTypeException.class, () -> decoder.decode(binaries));
  }

  @Test
  @DisplayName("decode(PacketHeader, null) returns null")
  void testDecodeWithPacketHeaderAndNullBinariesReturnsNull() {
    PacketHeader header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    assertNull(decoder.decode(header, null));
  }

  @Test
  @DisplayName("decode(PacketHeader, empty array) returns null")
  void testDecodeWithPacketHeaderAndEmptyBinariesReturnsNull() {
    PacketHeader header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    assertNull(decoder.decode(header, new byte[0]));
  }

  @Test
  @DisplayName("decode with compressed=true and no compressor throws IllegalStateException")
  void testDecodeCompressedWithoutCompressorThrows() {
    PacketHeader header = PacketHeader.newInstance(true, true, false, false, DataType.ZERO);
    assertThrows(IllegalStateException.class, () -> decoder.decode(header, new byte[]{1, 2, 3}));
  }

  @Test
  @DisplayName("decode with encrypted=true and no encryptor throws IllegalStateException")
  void testDecodeEncryptedWithoutEncryptorThrows() {
    PacketHeader header = PacketHeader.newInstance(true, false, false, true, DataType.ZERO);
    assertThrows(IllegalStateException.class, () -> decoder.decode(header, new byte[]{1, 2, 3}));
  }

  @Test
  @DisplayName("setCompressor stores the compressor for use in decode")
  void testSetCompressorIsUsedDuringDecode() {
    BinaryPacketCompressor compressor = mock(BinaryPacketCompressor.class);
    when(compressor.uncompress(new byte[]{1, 2, 3})).thenReturn(new byte[]{1, 2, 3});

    decoder.setCompressor(compressor);
    PacketHeader header = PacketHeader.newInstance(true, true, false, false, DataType.ZERO);

    // The uncompressed bytes go to DataUtility, which may throw - we just verify the compressor
    // is invoked; the downstream exception is expected
    assertThrows(Exception.class, () -> decoder.decode(header, new byte[]{1, 2, 3}));
    verify(compressor).uncompress(new byte[]{1, 2, 3});
  }

  @Test
  @DisplayName("setEncryptor stores the encryptor for use in decode")
  void testSetEncryptorIsUsedDuringDecode() {
    BinaryPacketEncryptor encryptor = mock(BinaryPacketEncryptor.class);
    when(encryptor.decrypt(new byte[]{1, 2, 3})).thenReturn(new byte[]{1, 2, 3});

    decoder.setEncryptor(encryptor);
    PacketHeader header = PacketHeader.newInstance(true, false, false, true, DataType.ZERO);

    // The decrypted bytes go to DataUtility, which may throw - we just verify the encryptor
    // is invoked; the downstream exception is expected
    assertThrows(Exception.class, () -> decoder.decode(header, new byte[]{1, 2, 3}));
    verify(encryptor).decrypt(new byte[]{1, 2, 3});
  }

  @Test
  @DisplayName("decode(PacketHeader, validZeroBytes) succeeds and returns non-null (covers line 79)")
  void testDecodeWithValidZeroBytesReturnsNonNull() {
    byte[] validBytes = ZeroUtility.newZeroMap().putBoolean("ok", true).toBinaries();
    PacketHeader header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    assertNotNull(decoder.decode(header, validBytes));
  }

  @Test
  @DisplayName("decode(byte[]) with valid zero-encoded bytes succeeds (covers line 48)")
  void testDecodeBytesArrayWithValidZeroEncodedData() {
    PacketHeader header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    byte headerByte = com.tenio.core.network.codec.CodecUtility.encodeFirstHeaderByte(header);
    byte[] zeroBytes = ZeroUtility.newZeroMap().putBoolean("x", true).toBinaries();
    byte[] encoded = new byte[1 + zeroBytes.length];
    encoded[0] = headerByte;
    System.arraycopy(zeroBytes, 0, encoded, 1, zeroBytes.length);
    assertNotNull(decoder.decode(encoded));
  }

  @Test
  @DisplayName("decode with both compressed and encrypted set invokes compressor then encryptor")
  void testDecodeCompressedAndEncryptedBothSet() {
    BinaryPacketCompressor compressor = mock(BinaryPacketCompressor.class);
    BinaryPacketEncryptor encryptor = mock(BinaryPacketEncryptor.class);
    byte[] input = new byte[]{1, 2, 3};
    when(compressor.uncompress(input)).thenReturn(input);
    when(encryptor.decrypt(input)).thenReturn(input);

    decoder.setCompressor(compressor);
    decoder.setEncryptor(encryptor);

    PacketHeader header = PacketHeader.newInstance(true, true, false, true, DataType.ZERO);
    assertThrows(Exception.class, () -> decoder.decode(header, input));
    verify(compressor).uncompress(input);
    verify(encryptor).decrypt(input);
  }
}
