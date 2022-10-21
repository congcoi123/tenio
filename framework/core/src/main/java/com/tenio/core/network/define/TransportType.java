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

package com.tenio.core.network.define;

import java.util.HashMap;
import java.util.Map;

/**
 * The definitions of all supported transportation types on the server.
 */
public enum TransportType {

  /**
   * UNKNOWN.
   */
  UNKNOWN("unknown"),
  /**
   * SOCKET.
   */
  TCP("tcp"),
  /**
   * DATAGRAM.
   */
  UDP("udp"),
  /**
   * WEBSOCKET.
   */
  WEB_SOCKET("websocket"),
  /**
   * HTTP.
   */
  HTTP("http");

  // Reverse-lookup map for getting a type from a value
  private static final Map<String, TransportType> lookup = new HashMap<>();

  static {
    for (var type : TransportType.values()) {
      lookup.put(type.getValue(), type);
    }
  }

  private final String value;

  TransportType(final String value) {
    this.value = value;
  }

  /**
   * Retrieves the transportation type by looking at its value.
   *
   * @param value the corresponding {@link String} value of transportation type
   * @return the corresponding {@link TransportType} if it is available, otherwise {@code null}
   */
  public static TransportType getByValue(String value) {
    return lookup.get(value);
  }

  /**
   * Retrieves the transportation type in text value.
   *
   * @return the transportation type in {@link String} value
   */
  public final String getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
