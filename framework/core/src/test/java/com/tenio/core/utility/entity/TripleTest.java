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

package com.tenio.core.utility.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Triple")
class TripleTest {

  @Test
  @DisplayName("Test create and access components")
  void testCreateAndAccessComponents() {
    Triple<String, Integer, Boolean> triple = new Triple<>("first", 2, true);
    assertEquals("first", triple.first());
    assertEquals(2, triple.second());
    assertEquals(true, triple.third());
  }

  @Test
  @DisplayName("Test create with null values")
  void testCreateWithNullValues() {
    Triple<String, String, String> triple = new Triple<>(null, null, null);
    assertNull(triple.first());
    assertNull(triple.second());
    assertNull(triple.third());
  }

  @Test
  @DisplayName("Test equality when same values")
  void testEqualityWhenSameValues() {
    Triple<String, Integer, Boolean> a = new Triple<>("x", 1, false);
    Triple<String, Integer, Boolean> b = new Triple<>("x", 1, false);
    assertEquals(a, b);
  }

  @Test
  @DisplayName("Test inequality when different values")
  void testInequalityWhenDifferentValues() {
    Triple<String, Integer, Boolean> a = new Triple<>("x", 1, false);
    Triple<String, Integer, Boolean> b = new Triple<>("y", 1, false);
    assertNotEquals(a, b);
  }

  @Test
  @DisplayName("Test hash code consistency for same values")
  void testHashCodeConsistency() {
    Triple<String, Integer, Boolean> a = new Triple<>("x", 1, false);
    Triple<String, Integer, Boolean> b = new Triple<>("x", 1, false);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  @DisplayName("Test toString contains component values")
  void testToStringContainsValues() {
    Triple<String, Integer, Boolean> triple = new Triple<>("alpha", 42, true);
    String str = triple.toString();
    assertTrue(str.contains("alpha"));
    assertTrue(str.contains("42"));
    assertTrue(str.contains("true"));
  }
}
