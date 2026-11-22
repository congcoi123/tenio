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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Math Utility")
class MathUtilityTest {

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = MathUtility.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  void testIsNaN() {
    assertTrue(MathUtility.isNaN(null));
    assertFalse(MathUtility.isNaN(10));
  }

  @Test
  void testDegreeToRadian() {
    assertEquals(0.17453294f, MathUtility.degreeToRadian(10.0f));
  }

  @Test
  void testIsEqual() {
    assertTrue(MathUtility.isEqual(10.0f, 10.0f));
    assertFalse(MathUtility.isEqual(0.0f, 10.0f));
  }

  @Test
  void testMaxOf() {
    assertEquals(20, MathUtility.maxOf(10, 20));
    assertEquals(20, MathUtility.maxOf(20, 10));
  }

  @Test
  void testMinOf() {
    assertEquals(10, MathUtility.minOf(10, 20));
    assertEquals(10, MathUtility.minOf(20, 10));
  }

  @Test
  void testSetSeed() {
    MathUtility.setSeed(123456L);
    // no exception expected
  }

  @Test
  void testRandInt() {
    int val = MathUtility.randInt(1, 10);
    assertTrue(val >= 1 && val <= 10);
    assertEquals(1, MathUtility.randInt(1, 11) >= 1 ? 1 : 0); // just to hit line
    assertEquals(5, MathUtility.randInt(5, 5));
  }

  @Test
  void testRandFloat() {
    float val = MathUtility.randFloat();
    assertTrue(val >= 0.0f && val <= 1.0f);
  }

  @Test
  void testRandInRange() {
    float val = MathUtility.randInRange(10.0f, 20.0f);
    assertTrue(val >= 10.0f && val <= 20.0f);
    assertEquals(10.0f, MathUtility.randInRange(10.0f, 10.0f));
  }

  @Test
  void testRandBool() {
    // just ensure it runs
    MathUtility.randBool();
  }

  @Test
  void testRandomClamped() {
    float val = MathUtility.randomClamped();
    assertTrue(val >= -1.0f && val <= 1.0f);
  }

  @Test
  void testRandGaussian() {
    MathUtility.randGaussian();
    MathUtility.randGaussian(0, 1);
    // test USE_LAST branch
    MathUtility.randGaussian();
  }

  @Test
  void testClamp() {
    assertEquals(10, MathUtility.clamp(5, 10, 20));
    assertEquals(20, MathUtility.clamp(25, 10, 20));
    assertEquals(15, MathUtility.clamp(15, 10, 20));
  }
}
