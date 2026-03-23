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

package com.tenio.core.network.codec.packet;

import java.util.HashMap;
import java.util.Map;

/**
 * The definition of all packet header setting flags.
 * <p>
 * <b>NOTE:</b> it's safe to define up to 8 flags per byte.
 */
public enum PacketHeaderType {

  /**
   * The packet needs data length prefixed value which show the total number of bytes for data.
   *
   * @since 0.6.7
   */
  LENGTH_PREFIXED((byte) 0b10000000),
  /**
   * The data size is considered as big size.
   */
  BIG_SIZE((byte) 0b01000000),
  /**
   * The data is compressed.
   */
  COMPRESSION((byte) 0b00100000),
  /**
   * The data is encrypted.
   */
  ENCRYPTION((byte) 0b00010000),
  /**
   * This slot is reserved.
   *
   * @since 0.6.7
   */
  RESERVED_1((byte) 0b00001000),
  /**
   * This slot is reserved.
   *
   * @since 0.6.7
   */
  RESERVED_2((byte) 0b00000100);

  // Reverse-lookup map for getting a type from an exact value.
  private static final Map<Byte, PacketHeaderType> lookup = new HashMap<>();

  static {
    for (PacketHeaderType type : PacketHeaderType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  private final byte value;

  PacketHeaderType(final byte value) {
    this.value = value;
  }

  /**
   * Retrieves the header type by its exact value.
   *
   * @param value the {@code byte} value of a header type
   * @return the corresponding {@link PacketHeaderType}, or {@code null} if no exact match exists
   */
  public static PacketHeaderType getByValue(final byte value) {
    return lookup.get(value);
  }

  /**
   * Retrieves the value of a header type.
   *
   * @return the {@code byte} value of a header type
   */
  public byte getValue() {
    return value;
  }

  @Override
  public String toString() {
    return name();
  }
}
