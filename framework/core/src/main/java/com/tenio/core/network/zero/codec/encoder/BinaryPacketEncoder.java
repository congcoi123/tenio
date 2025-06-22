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

package com.tenio.core.network.zero.codec.encoder;

import com.tenio.core.exception.PacketCompressorException;
import com.tenio.core.exception.PacketEncryptorException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncryptor;

/**
 * Provides APIs for encoding network packets into binary format.
 * This interface defines the contract for converting packet data into a binary
 * representation suitable for network transmission, with support for compression
 * and encryption.
 *
 * <p>Key features:
 * <ul>
 *   <li>Binary packet encoding with compression support</li>
 *   <li>Encryption integration for secure transmission</li>
 *   <li>Configurable compression thresholds</li>
 *   <li>Support for different packet types</li>
 *   <li>Efficient binary data handling</li>
 *   <li>Extensible encoding pipeline</li>
 * </ul>
 *
 * <p>Thread safety: Implementations of this interface should be thread-safe
 * as they may be accessed from multiple threads concurrently. The encoding
 * process should be atomic and not interfere with other encoding operations.
 *
 * <p>Performance considerations:
 * <ul>
 *   <li>Compression should be used judiciously based on packet size</li>
 *   <li>Encryption overhead should be considered for real-time applications</li>
 *   <li>Memory usage should be optimized for high-throughput scenarios</li>
 *   <li>Encoding should be efficient to minimize latency</li>
 * </ul>
 *
 * @see Packet
 * @see BinaryPacketCompressor
 * @see BinaryPacketEncryptor
 * @since 0.3.0
 */
public interface BinaryPacketEncoder {

  /**
   * Encodes a packet for transmission to clients.
   * This method performs the complete encoding process including
   * compression and encryption if configured.
   *
   * @param packet the incoming {@link Packet} to be encoded
   * @return the encoded {@link Packet} ready for transmission
   * @throws PacketCompressorException if compression fails
   * @throws PacketEncryptorException if encryption fails
   */
  Packet encode(Packet packet);

  /**
   * Sets the compressor for packet compression/decompression.
   * The compressor is used to reduce packet size before transmission.
   *
   * @param compressor the {@link BinaryPacketCompressor} instance to use
   */
  void setCompressor(BinaryPacketCompressor compressor);

  /**
   * Sets the encryptor for packet encryption/decryption.
   * The encryptor is used to secure packet data during transmission.
   *
   * @param encryptor the {@link BinaryPacketEncryptor} instance to use
   */
  void setEncryptor(BinaryPacketEncryptor encryptor);

  /**
   * Sets the compression threshold in bytes.
   * Packets smaller than this threshold will not be compressed
   * to avoid compression overhead for small packets.
   *
   * @param numberBytes the minimum packet size in bytes to trigger compression
   */
  void setCompressionThresholdBytes(int numberBytes);
}
