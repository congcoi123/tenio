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

package com.tenio.core.network.codec.packet;

import com.tenio.common.data.DataType;

/**
 * The packet header contains all settings for a packet by combining some conditions.
 */
public final class PacketHeader {

  private final boolean counting;
  private final boolean compressed;
  private final boolean bigSized;
  private final boolean encrypted;
  private final boolean zero;
  private final boolean msgpack;

  private PacketHeader(boolean counting, boolean compressed, boolean bigSized,
                       boolean encrypted, boolean zero, boolean msgpack) {
    this.counting = counting;
    this.compressed = compressed;
    this.bigSized = bigSized;
    this.encrypted = encrypted;
    this.zero = zero;
    this.msgpack = msgpack;
  }

  /**
   * Initialization. Only the one of zero or msgpack flag must be enabled.
   * <p>
   * Scenarios:
   * <ul>
   * <li>If 2 values are {@code true}, it throws an exception</li>
   * <li>If 2 values are {@code false}, the zero flag is enabled by default</li>
   * </ul>
   *
   * @param counting sets to {@code true} if the packet needs to include the total number of
   *                    bytes for data in the header, otherwise {@code false}
   * @param compressed  sets to {@code true} if the data is compressed, otherwise
   *                    {@code false}
   * @param bigSized    sets to {@code true} if the data size is considered big size,
   *                    otherwise returns {@code false}
   * @param encrypted   sets to {@code true} if the data is encrypted, otherwise
   *                    {@code false}
   * @param zero        sets to {@code true} if the data is encoded/decoded in Zero Type.
   * @param msgpack     sets to {@code true} if the data is encoded/decoded in MsgPack Type.
   * @return a new instance of {@link PacketHeader}
   * @see DataType
   */
  public static PacketHeader newInstance(boolean counting, boolean compressed, boolean bigSized,
                                         boolean encrypted, boolean zero, boolean msgpack) {
    if (zero && msgpack) {
      throw new IllegalArgumentException("Only one of zero or msgpack flag should be enabled");
    }
    if (!zero && !msgpack) {
      throw new IllegalArgumentException("Either zero or msgpack flag should be enabled");
    }
    return new PacketHeader(counting, compressed, bigSized, encrypted, zero, msgpack);
  }

  /**
   * Determines whether the packet needs data counting.
   *
   * @return {@code true} if the packet needs data counting, otherwise returns {@code false}
   * @since 0.6.7
   */
  public boolean needsCounting() {
    return counting;
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

  /**
   * Determines whether the data is encoded/decoded in Zero type.
   *
   * @return {@code true} if the data is encoded/decoded in Zero type, otherwise returns {@code false}
   * @see DataType#ZERO
   * @since 0.6.7
   */
  public boolean isZero() {
    return zero;
  }

  /**
   * Determines whether the data is encoded/decoded in MsgPack type.
   *
   * @return {@code true} if the data is encoded/decoded in MsgPack type, otherwise returns {@code false}
   * @see DataType#MSG_PACK
   * @since 0.6.7
   */
  public boolean isMsgpack() {
    return msgpack;
  }

  @Override
  public String toString() {
    return "PacketHeader{" +
        "counting=" + counting +
        ", compressed=" + compressed +
        ", bigSized=" + bigSized +
        ", encrypted=" + encrypted +
        ", zero=" + zero +
        ", msgpack=" + msgpack +
        '}';
  }
}
