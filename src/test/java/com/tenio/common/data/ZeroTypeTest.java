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

package com.tenio.common.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.common.data.zero.ZeroType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Unit Test Cases For Zero Type")
class ZeroTypeTest {

  @ParameterizedTest
  @CsvSource({
      "NULL, 0",
      "BOOLEAN, 1",
      "BYTE, 2",
      "SHORT, 3",
      "INTEGER, 4",
      "LONG, 5",
      "FLOAT, 6",
      "DOUBLE, 7",
      "STRING, 8",
      "BOOLEAN_ARRAY, 9",
      "BYTE_ARRAY, 10",
      "SHORT_ARRAY, 11",
      "INTEGER_ARRAY, 12",
      "LONG_ARRAY, 13",
      "FLOAT_ARRAY, 14",
      "DOUBLE_ARRAY, 15",
      "STRING_ARRAY, 16",
      "ZERO_ARRAY, 17",
      "ZERO_MAP, 18"
  })
  @DisplayName("Test All Enumerated Values")
  void testAllEnumValues(String name, int value) {
    ZeroType zeroType = ZeroType.valueOf(name);
    assertEquals(value, zeroType.getValue());
    assertEquals(name, zeroType.toString());
  }
}
