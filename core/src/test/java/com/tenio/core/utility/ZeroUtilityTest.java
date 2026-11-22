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

package com.tenio.core.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroUtility")
class ZeroUtilityTest {

  @Test
  @DisplayName("Private constructor throws UnsupportedOperationException via reflection")
  void testPrivateConstructorThrows() throws Exception {
    Constructor<ZeroUtility> ctor = ZeroUtility.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    assertThrows(Exception.class, ctor::newInstance);
  }

  @Test
  @DisplayName("convertBytesToMB converts 1 MB correctly")
  void testConvertOneMB() {
    assertEquals(1.0f, ZeroUtility.convertBytesToMB(1024L * 1024L), 0.0001f);
  }

  @Test
  @DisplayName("convertBytesToMB converts 0 bytes to 0.0")
  void testConvertZeroBytes() {
    assertEquals(0.0f, ZeroUtility.convertBytesToMB(0L), 0.0001f);
  }

  @Test
  @DisplayName("convertBytesToMB converts 512 KB to 0.5 MB")
  void testConvertHalfMB() {
    assertEquals(0.5f, ZeroUtility.convertBytesToMB(512L * 1024L), 0.0001f);
  }

  @Test
  @DisplayName("formatWithUnderscores formats 1_000 correctly")
  void testFormatThousand() {
    assertEquals("1_000", ZeroUtility.formatWithUnderscores(1000L));
  }

  @Test
  @DisplayName("formatWithUnderscores formats 1_000_000 correctly")
  void testFormatMillion() {
    assertEquals("1_000_000", ZeroUtility.formatWithUnderscores(1_000_000L));
  }

  @Test
  @DisplayName("formatWithUnderscores formats values under 1000 without separator")
  void testFormatSmallValue() {
    assertEquals("999", ZeroUtility.formatWithUnderscores(999L));
  }

  @Test
  @DisplayName("formatWithUnderscores formats 0 as '0'")
  void testFormatZero() {
    assertEquals("0", ZeroUtility.formatWithUnderscores(0L));
  }
}
