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

package com.tenio.core.network.codec.decoder;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import com.tenio.core.network.codec.packet.PacketHeader;

/**
 * The APIs designed for decoding binary packets.
 */
public interface BinaryPacketDecoder {

  /**
   * Decodes binaries data.
   *
   * @param binaries the receiving {@code byte} data
   * @return an instance of {@link DataCollection}, or {@code null} if the binaries' length is 0
   * @throws RuntimeException whenever an issue occurred
   * @since 0.6.7
   */
  DataCollection decode(byte[] binaries) throws RuntimeException;

  /**
   * Decodes binaries data.
   *
   * @param packetHeader instance of {@link PacketHeader}
   * @param binaries     the receiving {@code byte} data
   * @return an instance of {@link DataCollection}, or {@code null} if the binaries' length is 0
   * @throws RuntimeException whenever an issue occurred
   * @since 0.6.7
   */
  DataCollection decode(PacketHeader packetHeader, byte[] binaries) throws RuntimeException;

  /**
   * Sets the compressor for compressing/uncompressing packets.
   *
   * @param compressor the {@link BinaryPacketCompressor} instance
   */
  void setCompressor(BinaryPacketCompressor compressor);

  /**
   * Sets the encryptor for encrypting/encrypting packets.
   *
   * @param encryptor the {@link BinaryPacketEncryptor} instance
   */
  void setEncryptor(BinaryPacketEncryptor encryptor);
}
