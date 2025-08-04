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
import java.util.HashMap;
import java.util.Map;

/**
 * The definition of all packet header setting flags.
 * <p>
 * <b>NOTE:</b> it's safely to define up to 8 flags per byte
 */
public enum PacketHeaderType {

  /**
   * This slot is reserved.
   *
   * @since 0.6.7
   */
  RESERVED_1(1),
  /**
   * The packet needs data counting which show the total number of bytes for data.
   *
   * @since 0.6.7
   */
  COUNTING(2),
  /**
   * The data size is considered as big size.
   */
  BIG_SIZE(4),
  /**
   * The data is compressed.
   */
  COMPRESSION(8),
  /**
   * The data is encrypted.
   */
  ENCRYPTION(16),
  /**
   * The data is encoded/decoded in Zero type.
   *
   * @see DataType#ZERO
   * @since 0.6.7
   */
  ZERO(32),
  /**
   * The data is encoded/decoded in MsgPack type.
   *
   * @see DataType#MSG_PACK
   * @since 0.6.7
   */
  MSG_PACK(64),
  /**
   * This slot is reserved.
   *
   * @since 0.6.7
   */
  RESERVED_2(128);

  // Reverse-lookup map for getting a type from a value
  private static final Map<Integer, PacketHeaderType> lookup = new HashMap<>();

  static {
    for (PacketHeaderType type : PacketHeaderType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  private final int value;

  PacketHeaderType(final int value) {
    this.value = value;
  }

  /**
   * Retrieves the header type by using its value.
   *
   * @param value the {@code integer} value of header type
   * @return the corresponding {@link PacketHeaderType}
   */
  public static PacketHeaderType getByValue(int value) {
    return lookup.get(value);
  }

  /**
   * Retrieves the value of a header type.
   *
   * @return the {@code integer} value of header type
   */
  public final int getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
