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
import com.tenio.core.network.codec.encryption.BinaryPacketEncryptor;
import java.io.Serial;

/**
 * Exception thrown when an error occurs during packet encryption or decryption.
 * This exception is used to wrap and propagate errors that occur during the encryption
 * or decryption of network packets.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Invalid encryption key or initialization vector</li>
 *   <li>Corrupted or tampered encrypted data</li>
 *   <li>Encryption algorithm errors</li>
 *   <li>Incompatible encryption formats</li>
 * </ul>
 *
 * <p>Note: This exception preserves the original cause of the encryption error
 * through the cause chain, making it easier to diagnose the root cause of
 * encryption failures.
 *
 * @see BinaryPacketEncryptor
 * @see Packet
 * @since 0.3.0
 */
public final class PacketEncryptorException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -4260223574425695248L;

  /**
   * Creates a new exception.
   *
   * @param message a warning {@link String} message
   * @see BinaryPacketEncryptor
   */
  public PacketEncryptorException(String message) {
    super(message);
  }
}
