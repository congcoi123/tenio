/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.configuration;

import com.tenio.common.configuration.ConfigurationType;
import java.util.HashMap;
import java.util.Map;

public enum TestConfigurationType implements ConfigurationType {

  CUSTOM_VALUE_1("custom-value-1"),
  CUSTOM_VALUE_2("custom-value-2"),
  CUSTOM_VALUE_3("custom-value-3"),
  CUSTOM_VALUE_4("custom-value-4");

  // Reverse-lookup map for getting a type from a value
  private static final Map<String, TestConfigurationType> lookup =
      new HashMap<String, TestConfigurationType>();

  static {
    for (var configurationType : TestConfigurationType.values()) {
      lookup.put(configurationType.getValue(), configurationType);
    }
  }

  private final String value;

  TestConfigurationType(final String value) {
    this.value = value;
  }

  public static TestConfigurationType getByValue(String value) {
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
