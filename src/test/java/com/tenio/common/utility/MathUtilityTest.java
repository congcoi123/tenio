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

package com.tenio.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MathUtilityTest {

  @Test
  void testIsNaN() {
    assertFalse(MathUtility.<Object>isNaN("Val"));
    assertTrue(MathUtility.<Object>isNaN(null));
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
  void testSetSeed() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by setSeed(long)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.setSeed(42L);
  }

  @Test
  void testRandInt() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randInt(int, int)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randInt(2, 3);
  }

  @Test
  void testRandInt2() {
    assertEquals(1, MathUtility.randInt(1, 1));
  }

  @Test
  void testRandInt3() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randInt(int, int)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randInt(0, 3);
  }

  @Test
  void testRandInt4() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randInt(int, int)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randInt(1, 3);
  }

  @Test
  void testRandFloat() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randFloat()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randFloat();
  }

  @Test
  void testRandInRange() {
    assertEquals(10.0f, MathUtility.randInRange(10.0f, 10.0f));
  }

  @Test
  void testRandInRange2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randInRange(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randInRange(0.0f, 10.0f);
  }

  @Test
  void testRandInRange3() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randInRange(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randInRange(0.5f, 10.0f);
  }

  @Test
  void testRandInRange4() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randInRange(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randInRange(-0.5f, 10.0f);
  }

  @Test
  void testRandBool() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randBool()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randBool();
  }

  @Test
  void testRandomClamped() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randomClamped()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randomClamped();
  }

  @Test
  void testRandGaussian() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randGaussian()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randGaussian();
  }

  @Test
  void testRandGaussian2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randGaussian(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randGaussian(10.0f, 10.0f);
  }

  @Test
  void testRandGaussian3() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randGaussian(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randGaussian(2.0f, 10.0f);
  }

  @Test
  void testRandGaussian4() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randGaussian(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randGaussian(1.0f, 10.0f);
  }

  @Test
  void testRandGaussian5() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by randGaussian(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    MathUtility.randGaussian(0.0f, 10.0f);
  }

  @Test
  void testClamp() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by clamp(Number, Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Integer arg = Integer.valueOf(1);
    Integer minVal = Integer.valueOf(1);
    MathUtility.<Number>clamp(arg, minVal, Integer.valueOf(1));
  }

  @Test
  void testClamp2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by clamp(Number, Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Integer arg = Integer.valueOf(0);
    Integer minVal = Integer.valueOf(1);
    MathUtility.<Number>clamp(arg, minVal, Integer.valueOf(1));
  }

  @Test
  void testClamp3() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by clamp(Number, Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Integer arg = Integer.valueOf(3);
    Integer minVal = Integer.valueOf(1);
    MathUtility.<Number>clamp(arg, minVal, Integer.valueOf(1));
  }

  @Test
  void testClamp4() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by clamp(Number, Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Integer arg = Integer.valueOf(-1);
    Integer minVal = Integer.valueOf(1);
    MathUtility.<Number>clamp(arg, minVal, Integer.valueOf(1));
  }
}
