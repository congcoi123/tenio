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

package com.tenio.common.data.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Common Map")
class CommonMapTest {

  private final CommonMap commonMap = CommonMap.newInstance();
  private final CommonMap dummyMap = CommonMap.newInstance();
  private final CommonArray dummyArray = CommonArray.newInstance();

  @BeforeEach
  void initialization() {
    commonMap.add("double", 100.0).add("float", 100.0F).add("long", 100L).add("integer", 100)
        .add("string", "test").add("boolean", true).add("common object", dummyMap)
        .add("common object array", dummyArray).add("object", dummyMap);
  }

  @Test
  @DisplayName("Allow fetching all inserted data")
  void itShouldFetchAllInsertedData() {
    assertAll("itShouldFetchAllInsertedData",
        () -> assertEquals(100.0, commonMap.getDouble("double")),
        () -> assertEquals(100.0f, commonMap.getFloat("float")),
        () -> assertEquals(100L, commonMap.getLong("long")),
        () -> assertEquals(100, commonMap.getInt("integer")),
        () -> assertEquals("test", commonMap.getString("string")),
        () -> assertTrue(commonMap.getBoolean("boolean")),
        () -> assertEquals(dummyMap, commonMap.getCommonObject("common object")),
        () -> assertEquals(dummyArray, commonMap.getCommonObjectArray("common object array")),
        () -> assertEquals(dummyMap, commonMap.getObject("object")));
  }

  @Test
  @DisplayName("Allow checking existed data")
  void itShouldCheckTheExistedData() {
    assertAll("itShouldCheckTheExistedData",
        () -> assertTrue(commonMap.contains("double")),
        () -> assertFalse(commonMap.contains("short")));
  }

  @Test
  @DisplayName("An exception should be thrown when a readonly map is tried to modify")
  void itShouldThrowExceptionWhenTryToModifyReadonlyArray() {
    var readonlyMap = commonMap.getReadonlyMap();
    Assertions.assertThrows(UnsupportedOperationException.class,
        () -> readonlyMap.put("new key", "new value"));
  }
}
