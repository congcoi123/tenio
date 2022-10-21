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

package com.tenio.core.network.zero.codec.packet;

/**
 * The packet header contains all settings for a packet by combining some conditions.
 */
public final class PacketHeader {

  private final boolean binary;
  private final boolean compressed;
  private final boolean bigSized;
  private final boolean encrypted;

  private PacketHeader(boolean binary, boolean compressed, boolean bigSized, boolean encrypted) {
    this.binary = binary;
    this.compressed = compressed;
    this.bigSized = bigSized;
    this.encrypted = encrypted;
  }

  /**
   * Initialization.
   *
   * @param binary     sets to {@code true} if the data is written by binary, otherwise
   *                   {@code false}
   * @param compressed sets to {@code true} if the data is compressed, otherwise
   *                   {@code false}
   * @param bigSized   sets to {@code true} if the data size is considered big size,
   *                   otherwise returns {@code false}
   * @param encrypted  sets to {@code true} if the data is encrypted, otherwise
   *                   {@code false}
   * @return a new instance of {@link PacketHeader}
   */
  public static PacketHeader newInstance(boolean binary, boolean compressed, boolean bigSized,
                                         boolean encrypted) {
    return new PacketHeader(binary, compressed, bigSized, encrypted);
  }

  /**
   * Determines whether the data is written by binary.
   *
   * @return {@code true} if the data is written by binary, otherwise returns {@code false}
   */
  public boolean isBinary() {
    return binary;
  }

  /**
   * Determines whether the data is compressed.
   *
   * @return {@code true} if the data is compressed, otherwise returns {@code false}
   */
  public boolean isCompressed() {
    return compressed;
  }

  /**
   * Determines whether the data size is big.
   *
   * @return {@code true} if the data size is considered as big size, otherwise
   * {@code false}
   */
  public boolean isBigSized() {
    return bigSized;
  }

  /**
   * Determines whether the data is encrypted.
   *
   * @return {@code true} if the data is encrypted, otherwise returns {@code false}
   */
  public boolean isEncrypted() {
    return encrypted;
  }

  @Override
  public String toString() {
    return "PacketHeader{" +
        "binary=" + binary +
        ", compressed=" + compressed +
        ", bigSized=" + bigSized +
        ", encrypted=" + encrypted +
        '}';
  }
}
