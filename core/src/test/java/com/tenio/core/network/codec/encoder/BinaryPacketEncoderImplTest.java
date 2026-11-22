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

package com.tenio.core.network.codec.encoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.tenio.common.data.DataType;
import com.tenio.core.network.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.outbound.packet.implement.PacketImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BinaryPacketEncoderImpl")
class BinaryPacketEncoderImplTest {

  private BinaryPacketEncoderImpl encoder;

  @BeforeEach
  void setUp() {
    encoder = new BinaryPacketEncoderImpl();
  }

  @Test
  @DisplayName("Try to encode an empty packet should throw exception")
  void testEncodeNullPacket() {
    var packet = PacketImpl.newInstance();
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(packet));
    packet.setData(new byte[0]);
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding a valid packet")
  void testEncodeValidPacket() {
    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[] {1, 2, 3});
    assertNotNull(encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding a packet with length prefix short header")
  void testEncodePacketWithLengthPrefixShortHeader() {
    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[] {1, 2, 3});
    when(packet.hasLengthPrefixed()).thenReturn(true);
    assertNotNull(encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding a packet with length prefix and large data uses int header")
  void testEncodePacketWithLengthPrefixLargeData() {
    int largeSize = Short.MAX_VALUE * 2 + 2; // > MAX_BYTES_FOR_NORMAL_SIZE
    byte[] largeData = new byte[largeSize];
    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(DataType.ZERO);
    when(packet.getData()).thenReturn(largeData);
    when(packet.hasLengthPrefixed()).thenReturn(true);
    assertNotNull(encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding a packet requiring encryption without encryptor throws")
  void testEncodeEncryptedPacketNoEncryptorThrows() {
    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[] {1, 2, 3});
    when(packet.needsEncrypted()).thenReturn(true);
    assertThrows(IllegalStateException.class, () -> encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding a packet with compression threshold below data size without compressor throws")
  void testEncodeCompressedPacketNoCompressorThrows() {
    encoder.setCompressionThresholdBytes(2);
    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[] {1, 2, 3});
    when(packet.needsEncrypted()).thenReturn(false);
    assertThrows(IllegalStateException.class, () -> encoder.encode(packet));
  }

  @Test
  @DisplayName("Test setCompressor and setEncryptor do not throw")
  void testSetCompressorAndEncryptor() {
    encoder.setCompressor(mock(BinaryPacketCompressor.class));
    encoder.setEncryptor(mock(BinaryPacketEncryptor.class));
    // no exception expected
  }

  @Test
  @DisplayName("Test setCompressionThresholdBytes does not throw")
  void testSetCompressionThresholdBytes() {
    encoder.setCompressionThresholdBytes(1024);
    // no exception expected
  }

  @Test
  @DisplayName("Test encoding with successful encryption produces non-null packet")
  void testEncodeWithSuccessfulEncryption() throws Exception {
    BinaryPacketEncryptor encryptor = mock(BinaryPacketEncryptor.class);
    when(encryptor.encrypt(new byte[]{1, 2, 3})).thenReturn(new byte[]{4, 5, 6});
    encoder.setEncryptor(encryptor);

    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(packet.needsEncrypted()).thenReturn(true);

    assertNotNull(encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding with encryption exception falls back to unencrypted")
  void testEncodeWithEncryptionException() throws Exception {
    BinaryPacketEncryptor encryptor = mock(BinaryPacketEncryptor.class);
    when(encryptor.encrypt(new byte[]{1, 2, 3})).thenThrow(new RuntimeException("encrypt error"));
    encoder.setEncryptor(encryptor);

    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(packet.needsEncrypted()).thenReturn(true);

    assertNotNull(encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding with successful compression produces non-null packet")
  void testEncodeWithSuccessfulCompression() throws Exception {
    BinaryPacketCompressor compressor = mock(BinaryPacketCompressor.class);
    byte[] compressedData = new byte[]{7, 8, 9};
    when(compressor.compress(new byte[]{1, 2, 3})).thenReturn(compressedData);
    encoder.setCompressor(compressor);
    encoder.setCompressionThresholdBytes(1);

    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(packet.needsEncrypted()).thenReturn(false);

    assertNotNull(encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding with compression exception falls back to uncompressed")
  void testEncodeWithCompressionException() throws Exception {
    BinaryPacketCompressor compressor = mock(BinaryPacketCompressor.class);
    when(compressor.compress(new byte[]{1, 2, 3})).thenThrow(new RuntimeException("compress error"));
    encoder.setCompressor(compressor);
    encoder.setCompressionThresholdBytes(1);

    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(com.tenio.common.data.DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[]{1, 2, 3});
    when(packet.needsEncrypted()).thenReturn(false);

    assertNotNull(encoder.encode(packet));
  }
}
