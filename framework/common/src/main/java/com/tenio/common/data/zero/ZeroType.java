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

package com.tenio.common.data.zero;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Self-definition data types.
 */
public enum ZeroType {

  /**
   * Null Value.
   */
  NULL(0),
  /**
   * {@link Boolean} Value.
   */
  BOOLEAN(1),
  /**
   * Byte Value.
   */
  BYTE(2),
  /**
   * {@link Short} Value.
   */
  SHORT(3),
  /**
   * {@link Integer} Value.
   */
  INTEGER(4),
  /**
   * {@link Long} Value.
   */
  LONG(5),
  /**
   * {@link Float} Value.
   */
  FLOAT(6),
  /**
   * {@link Double} Value.
   */
  DOUBLE(7),
  /**
   * {@link String} Value.
   */
  STRING(8),
  /**
   * Collection of {@link Boolean} Value.
   *
   * @see Collection
   */
  BOOLEAN_ARRAY(9),
  /**
   * Byte Array Value.
   */
  BYTE_ARRAY(10),
  /**
   * Collection of {@link Short} Value.
   *
   * @see Collection
   */
  SHORT_ARRAY(11),
  /**
   * Collection of {@link Integer} Value.
   *
   * @see Collection
   */
  INTEGER_ARRAY(12),
  /**
   * Collection of {@link Long} Value.
   *
   * @see Collection
   */
  LONG_ARRAY(13),
  /**
   * Collection of {@link Float} Value.
   *
   * @see Collection
   */
  FLOAT_ARRAY(14),
  /**
   * Collection of {@link Double} Value.
   *
   * @see Collection
   */
  DOUBLE_ARRAY(15),
  /**
   * Collection of {@link String} Value.
   *
   * @see Collection
   */
  STRING_ARRAY(16),
  /**
   * {@link ZeroArray} Value.
   */
  ZERO_ARRAY(17),
  /**
   * {@link ZeroMap} Value.
   */
  ZERO_MAP(18);

  // Reverse-lookup map for getting a type from a value
  private static final Map<Integer, ZeroType> lookup = new HashMap<>();

  static {
    for (ZeroType type : ZeroType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  private final int value;

  ZeroType(int value) {
    this.value = value;
  }

  /**
   * Retrieves a type by using its value.
   *
   * @param value the value in <code>integer</code> number
   * @return the corresponding {@link ZeroType} if available, otherwise <code>null</code>
   */
  public static ZeroType getByValue(int value) {
    return lookup.get(value);
  }

  /**
   * Fetches the type's value.
   *
   * @return the type's value in <code>integer</code> type
   */
  public final int getValue() {
    return this.value;
  }

  @Override
  public final String toString() {
    return this.name();
  }
}
