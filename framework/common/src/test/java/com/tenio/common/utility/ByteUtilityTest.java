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

package com.tenio.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.custom.StringArrayConverter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

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

  @ParameterizedTest
  @CsvSource({
      "1695609641, '101, 16, -13, 41'",
      "66320, '0, 1, 3, 16'"
  })
  void testIntToBytes(int intValue, @ConvertWith(StringArrayConverter.class) String[] binary) {
    byte[] byteArray = ByteUtility.intToBytes(intValue);
    assertEquals(binary.length, byteArray.length);
    assertEquals(binary[0], String.valueOf(byteArray[0]));
    assertEquals(binary[1], String.valueOf(byteArray[1]));
    assertEquals(binary[2], String.valueOf(byteArray[2]));
    assertEquals(binary[3], String.valueOf(byteArray[3]));
  }

  @Test
  void testBytesToInt() {
    assertEquals(1094795585, ByteUtility.bytesToInt("AAAAAAAA" .getBytes(StandardCharsets.UTF_8)));
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
    assertEquals((short) 16705,
        ByteUtility.bytesToShort("AAAAAAAA" .getBytes(StandardCharsets.UTF_8)));
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
