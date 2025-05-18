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

package com.tenio.core.exception;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import java.io.Serial;

/**
 * Exception thrown when packet compression operations fail.
 * This exception indicates that an error occurred during the compression
 * or decompression of network packets, which could be due to various
 * reasons such as invalid data, compression algorithm failures, or
 * resource constraints.
 *
 * <p>Key features:
 * <ul>
 *   <li>Compression error detection</li>
 *   <li>Detailed error messages</li>
 *   <li>Thread-safe exception handling</li>
 * </ul>
 *
 * <p>Common causes:
 * <ul>
 *   <li>Invalid or corrupted packet data</li>
 *   <li>Unsupported compression algorithm</li>
 *   <li>Memory constraints during compression</li>
 *   <li>Compression level configuration issues</li>
 * </ul>
 *
 * <p>Note: This exception should be handled appropriately to ensure
 * proper packet processing and system stability.
 *
 * @see Packet
 * @since 0.3.0
 */
public final class PacketCompressorException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 4134024704906630426L;

  /**
   * Creates a new instance of the exception with a detailed error message.
   *
   * @param message a detailed description of the compression failure
   */
  public PacketCompressorException(String message) {
    super(message);
  }
}
