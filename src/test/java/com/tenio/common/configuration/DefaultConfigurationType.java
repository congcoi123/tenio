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

package com.tenio.common.configuration;

import java.util.HashMap;
import java.util.Map;

enum DefaultConfigurationType implements ConfigurationType {
  BOOLEAN("BOOLEAN"),
  INTEGER("INTEGER"),
  FLOAT("FLOAT"),
  STRING("STRING"),
  OBJECT("OBJECT"),
  NOT_DEFINED("NOT_DEFINED"),
  NULL_DEFINED("NULL_DEFINED");

  // Reverse-lookup map for getting a type from a value
  private static final Map<String, DefaultConfigurationType> lookup = new HashMap<>();

  static {
    for (var configurationType : DefaultConfigurationType.values()) {
      lookup.put(configurationType.getValue(), configurationType);
    }
  }

  private final String value;

  DefaultConfigurationType(final String value) {
    this.value = value;
  }

  public static DefaultConfigurationType getByValue(String value) {
    return lookup.get(value);
  }

  public final String getValue() {
    return value;
  }

  @Override
  public final String toString() {
    return name();
  }
}
