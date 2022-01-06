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

package com.tenio.common.data.element;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Common Object Array")
class CommonObjectArrayTest {

  private final CommonObjectArray dummyObjectArray = CommonObjectArray.newInstance();
  private final CommonObjectArray commonObjectArray = CommonObjectArray.newInstance();

  @BeforeEach
  void initialization() {
    commonObjectArray.put(100.0).put(100.0F).put(100L).put(100).put(true).put("test")
        .put(dummyObjectArray).put(dummyObjectArray);
  }

  @Test
  @DisplayName("Allow fetching all inserted data from the array")
  void itShouldFetchAllInsertedData() {
    assertAll("itShouldFetchAllInsertedData",
        () -> assertEquals(commonObjectArray.getDouble(0), 100.0),
        () -> assertEquals(commonObjectArray.getFloat(1), 100.0F),
        () -> assertEquals(commonObjectArray.getLong(2), 100L),
        () -> assertEquals(commonObjectArray.getInt(3), 100),
        () -> assertTrue(commonObjectArray.getBoolean(4)),
        () -> assertEquals(commonObjectArray.getString(5), "test"),
        () -> assertEquals(commonObjectArray.getCommonObjectArray(6), dummyObjectArray),
        () -> assertTrue(commonObjectArray.getObject(7) instanceof CommonObjectArray)
    );
  }
}
