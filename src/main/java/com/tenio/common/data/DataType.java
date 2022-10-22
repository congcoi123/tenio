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

package com.tenio.common.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Self-definition data types.
 */
public enum DataType {

  /**
   * Uses the internal Zero serialization mechanism.
   */
  ZERO("zero"),
  /**
   * Uses the MsgPack tool.
   */
  MSG_PACK("msgpack");

  // Reverse-lookup map for getting a type from a value
  private static final Map<String, DataType> lookup = new HashMap<>();

  static {
    for (var type : DataType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  private final String value;

  DataType(String value) {
    this.value = value;
  }

  /**
   * Retrieves a type by using its value.
   *
   * @param value the value in <code>String</code>
   * @return the corresponding {@link DataType} if available, otherwise <code>null</code>
   */
  public static DataType getByValue(String value) {
    return lookup.get(value);
  }

  /**
   * Fetches the type's value.
   *
   * @return the type's value in <code>String</code> type
   */
  public final String getValue() {
    return this.value;
  }

  @Override
  public final String toString() {
    return getValue();
  }
}
