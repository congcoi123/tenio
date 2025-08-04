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

package com.tenio.common.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.msgpack.MsgPackUtility;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For MsgPack Utility")
class MsgPackUtilityTest {

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = MsgPackUtility.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  @DisplayName("Checking whether binaries data is a collection should work")
  void itShouldReturnCorrectDataCollection() {
    var msgpackArray = new boolean[] { true, false, true };
    var msgpackMap = MsgPackUtility.newMsgPackMap().putBoolean("a", true).putBooleanArray("b", msgpackArray);
    var checkMsgPackMap = MsgPackUtility.deserialize(msgpackMap.toBinaries());

    assert checkMsgPackMap != null;
    assertEquals(msgpackMap.getBoolean("a"), checkMsgPackMap.getBoolean("a"));
    assertEquals(Arrays.toString(msgpackMap.getBooleanArray("b")), Arrays.toString(checkMsgPackMap.getBooleanArray("b")));
  }

  @Test
  @DisplayName("Allow adding and fetching data to/from MsgPackArray")
  void addedDataInArrayShouldMatch() {
    var origin = new int[] { 10, 20, 30, 40, 50 };
    var carry = MsgPackUtility.newMsgPackMap().putIntegerArray("k", origin);
    var actuality = MsgPackUtility.deserialize(carry.toBinaries());

    assert actuality != null;
    assertAll("addedDataInArrayShouldMatch",
        () -> assertEquals(10, actuality.getIntegerArray("k")[0]),
        () -> assertEquals(20, actuality.getIntegerArray("k")[1]),
        () -> assertEquals(30, actuality.getIntegerArray("k")[2]),
        () -> assertEquals(40, actuality.getIntegerArray("k")[3]),
        () -> assertEquals(50, actuality.getIntegerArray("k")[4])
    );
  }

  @Test
  @DisplayName("Allow adding and fetching data to/from MsgPackMap")
  void putDataInMapShouldMatch() {
    var origin = MsgPackUtility.newMsgPackMap();
    origin.putBoolean("b", true)
        .putInteger("i", 1000)
        .putFloat("f", 101.1f)
        .putString("s", "msgpack")
        .putMsgPackMap("map", MsgPackMap.newInstance().putBoolean("mapb", true));
    var actuality = MsgPackUtility.deserialize(origin.toBinaries());

    assert actuality != null;
    assertAll("putDataInMapShouldMatch",
        () -> assertFalse(actuality.contains("out")),
        () -> assertTrue(actuality.getBoolean("b")),
        () -> assertEquals(1000, actuality.getInteger("i")),
        () -> assertEquals(101.1f, actuality.getFloat("f")),
        () -> assertEquals("msgpack", actuality.getString("s")),
        () -> assertEquals(origin.getMsgPackMap("map").getBoolean("mapb"), actuality.getMsgPackMap("map").getBoolean(
            "mapb"))
    );

    var readonlyMap = origin.getReadonlyMap();
    assertAll("readonlyDataInMapShouldMatch",
        () -> assertTrue((boolean) readonlyMap.get("b")),
        () -> assertEquals(1000, (int) readonlyMap.get("i")),
        () -> assertEquals(101.1f, (float) readonlyMap.get("f")),
        () -> assertEquals("msgpack", readonlyMap.get("s"))
    );
  }
}
