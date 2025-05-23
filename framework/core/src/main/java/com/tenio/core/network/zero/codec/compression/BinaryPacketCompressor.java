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

package com.tenio.core.network.zero.codec.compression;

import com.tenio.core.exception.PacketCompressorException;

/**
 * The APIs designed for compressing binary packets.
 */
public interface BinaryPacketCompressor {

  /**
   * Compresses an array of binary size into another smaller one.
   *
   * @param binary an array of {@code byte}
   * @return a new smaller size of {@code byte} array
   * @throws PacketCompressorException when an exception occurred during the compression process
   */
  byte[] compress(byte[] binary) throws PacketCompressorException;

  /**
   * Uncompressed an array of binary and reverts it to the original one.
   *
   * @param binary compressed array of {@code byte}
   * @return the original data of {@code byte} array
   * @throws PacketCompressorException when an exception occurred during the compression process
   */
  byte[] uncompress(byte[] binary) throws PacketCompressorException;
}
