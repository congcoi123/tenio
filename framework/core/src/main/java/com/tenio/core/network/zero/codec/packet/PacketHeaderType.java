/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

import java.util.HashMap;
import java.util.Map;

/**
 * The definition of all packet header types.
 */
public enum PacketHeaderType {

  BINARY(1),
  BIG_SIZE(4),
  COMPRESSION(8),
  ENCRYPTION(16);

  // Reverse-lookup map for getting a type from a value
  private static final Map<Integer, PacketHeaderType> lookup =
      new HashMap<Integer, PacketHeaderType>();

  static {
    for (var type : PacketHeaderType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  private final int value;

  PacketHeaderType(final int value) {
    this.value = value;
  }

  public static PacketHeaderType getByValue(int value) {
    return lookup.get(value);
  }

  public final int getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
