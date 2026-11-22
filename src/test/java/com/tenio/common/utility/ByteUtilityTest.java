/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Byte Utility")
class ByteUtilityTest {

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = ByteUtility.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  void testIntToBytes() {
    Object[][] testCases = new Object[][]{
            {1695609641, new String[]{"101", "16", "-13", "41"}},
            {66320, new String[]{"0", "1", "3", "16"}}
    };

    for (Object[] testCase : testCases) {
      int intValue = (int) testCase[0];
      String[] binaries = (String[]) testCase[1];

      byte[] byteArray = ByteUtility.intToBytes(intValue);

      assertEquals(binaries.length, byteArray.length);
      assertEquals(binaries[0], String.valueOf(byteArray[0]));
      assertEquals(binaries[1], String.valueOf(byteArray[1]));
      assertEquals(binaries[2], String.valueOf(byteArray[2]));
      assertEquals(binaries[3], String.valueOf(byteArray[3]));
    }
  }

  @Test
  void testBytesToInt() {
    byte[] bytes = new byte[] {65, 65, 65, 65}; // "AAAA"
    assertEquals(1094795585, ByteUtility.bytesToInt(bytes));
  }

  @Test
  void testShortToBytes() {
    byte[] actualShortToBytesResult = ByteUtility.shortToBytes((short) 1);
    assertEquals(2, actualShortToBytesResult.length);
    assertEquals((byte) 0, actualShortToBytesResult[0]);
    assertEquals((byte) 1, actualShortToBytesResult[1]);
  }

  @Test
  void testBytesToShort() {
    byte[] bytes = new byte[] {65, 65}; // "AA"
    assertEquals((short) 16705, ByteUtility.bytesToShort(bytes));
  }

  @Test
  void testResizeBytesArray() {
    byte[] actualResizeBytesArrayResult =
        ByteUtility.resizeBytesArray("AAAAAAAA" .getBytes(StandardCharsets.UTF_8), 1, 3);
    assertEquals(3, actualResizeBytesArrayResult.length);
    assertEquals('A', actualResizeBytesArrayResult[0]);
    assertEquals('A', actualResizeBytesArrayResult[1]);
    assertEquals('A', actualResizeBytesArrayResult[2]);
  }

  @Test
  void testResizeBytesArray2() {
    assertThrows(NegativeArraySizeException.class,
        () -> ByteUtility.resizeBytesArray("AAAAAAAA" .getBytes(StandardCharsets.UTF_8), 1, -1));
  }
}
