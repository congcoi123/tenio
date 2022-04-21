/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero.codec.decoder;

import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncryptor;

/**
 * The APIs designed for decoding binary packets.
 */
public interface BinaryPacketDecoder {

  /**
   * Decodes data from clients side sent by session.
   *
   * @param session the receiving {@link Session}
   * @param data    the receiving {@code byte} data
   * @throws RuntimeException whenever an issue occurred
   */
  void decode(Session session, byte[] data) throws RuntimeException;

  /**
   * Sets the listener for packet decoder handler.
   *
   * @param resultListener the {@link PacketDecoderResultListener} instance
   */
  void setResultListener(PacketDecoderResultListener resultListener);

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
