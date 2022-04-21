package com.tenio.engine.physic2d.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Vector2Test {
  @Test
  void testNewInstance() {
    assertEquals("(0.0, 0.0)", Vector2.newInstance().toString());
  }

  @Test
  void testNewInstance2() {
    Vector2 actualNewInstanceResult = Vector2.newInstance();
    assertEquals(0.0f, actualNewInstanceResult.y);
    assertEquals(0.0f, actualNewInstanceResult.x);
  }

  @Test
  void testSet() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualSetResult = newInstanceResult.set(10.0f, 10.0f);
    assertSame(newInstanceResult, actualSetResult);
    assertEquals(10.0f, actualSetResult.y);
    assertEquals(10.0f, actualSetResult.x);
  }

  @Test
  void testSet2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualSetResult = newInstanceResult.set(Vector2.newInstance());
    assertSame(newInstanceResult, actualSetResult);
    assertEquals(0.0f, actualSetResult.y);
    assertEquals(0.0f, actualSetResult.x);
  }

  @Test
  void testClone() {
    Vector2 actualCloneResult = Vector2.newInstance().clone();
    assertEquals(0.0f, actualCloneResult.y);
    assertEquals(0.0f, actualCloneResult.x);
  }

  @Test
  void testIsZero() {
    assertTrue(Vector2.newInstance().isZero());
  }

  @Test
  void testIsZero2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 0.0f);
    assertFalse(newInstanceResult.isZero());
  }

  @Test
  void testIsZero3() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(0.0f, 10.0f);
    assertFalse(newInstanceResult.isZero());
  }

  @Test
  void testGetLength() {
    assertEquals(0.0f, Vector2.newInstance().getLength());
  }

  @Test
  void testGetLengthSqr() {
    assertEquals(0.0f, Vector2.newInstance().getLengthSqr());
  }

  @Test
  void testNormalize() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertSame(newInstanceResult, newInstanceResult.normalize());
  }

  @Test
  void testNormalize2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 0.0f);
    Vector2 actualNormalizeResult = newInstanceResult.normalize();
    assertSame(newInstanceResult, actualNormalizeResult);
    assertEquals(0.0f, actualNormalizeResult.y);
    assertEquals(1.0f, actualNormalizeResult.x);
  }

  @Test
  void testGetDotProductValue() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertEquals(0.0f, newInstanceResult.getDotProductValue(Vector2.newInstance()));
  }

  @Test
  void testGetSignValue() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertEquals(1, newInstanceResult.getSignValue(Vector2.newInstance()));
  }

  @Test
  void testGetSignValue2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.x = -1.0f;
    Vector2 newInstanceResult1 = Vector2.newInstance();
    newInstanceResult1.add(10.0f, 10.0f);
    assertEquals(Vector2.ANTI_CLOCK_WISE, newInstanceResult.getSignValue(newInstanceResult1));
  }

  @Test
  void testPerpendicular() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualPerpendicularResult = newInstanceResult.perpendicular();
    assertSame(newInstanceResult, actualPerpendicularResult);
    assertEquals(0.0f, actualPerpendicularResult.y);
    assertEquals(-0.0f, actualPerpendicularResult.x);
  }

  @Test
  void testTruncate() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertSame(newInstanceResult, newInstanceResult.truncate(10.0f));
  }

  @Test
  void testTruncate2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 10.0f);
    Vector2 actualTruncateResult = newInstanceResult.truncate(10.0f);
    assertSame(newInstanceResult, actualTruncateResult);
    assertEquals(7.071068f, actualTruncateResult.y);
    assertEquals(7.071068f, actualTruncateResult.x);
  }

  @Test
  void testTruncate3() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualTruncateResult = newInstanceResult.truncate(-1.0f);
    assertSame(newInstanceResult, actualTruncateResult);
    assertEquals(-0.0f, actualTruncateResult.y);
    assertEquals(-0.0f, actualTruncateResult.x);
  }

  @Test
  void testGetDistanceValue() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertEquals(0.0f, newInstanceResult.getDistanceValue(Vector2.newInstance()));
  }

  @Test
  void testGetDistanceSqrValue() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertEquals(0.0f, newInstanceResult.getDistanceSqrValue(Vector2.newInstance()));
  }

  @Test
  void testReverse() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualReverseResult = newInstanceResult.reverse();
    assertSame(newInstanceResult, actualReverseResult);
    assertEquals(-0.0f, actualReverseResult.y);
    assertEquals(-0.0f, actualReverseResult.x);
  }

  @Test
  void testAdd() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualAddResult = newInstanceResult.add(10.0f, 10.0f);
    assertSame(newInstanceResult, actualAddResult);
    assertEquals(10.0f, actualAddResult.y);
    assertEquals(10.0f, actualAddResult.x);
  }

  @Test
  void testAdd2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualAddResult = newInstanceResult.add(Vector2.newInstance());
    assertSame(newInstanceResult, actualAddResult);
    assertEquals(0.0f, actualAddResult.y);
    assertEquals(0.0f, actualAddResult.x);
  }

  @Test
  void testSub() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualSubResult = newInstanceResult.sub(10.0f, 10.0f);
    assertSame(newInstanceResult, actualSubResult);
    assertEquals(-10.0f, actualSubResult.y);
    assertEquals(-10.0f, actualSubResult.x);
  }

  @Test
  void testSub2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualSubResult = newInstanceResult.sub(Vector2.newInstance());
    assertSame(newInstanceResult, actualSubResult);
    assertEquals(0.0f, actualSubResult.y);
    assertEquals(0.0f, actualSubResult.x);
  }

  @Test
  void testMul() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualMulResult = newInstanceResult.mul(10.0f);
    assertSame(newInstanceResult, actualMulResult);
    assertEquals(0.0f, actualMulResult.y);
    assertEquals(0.0f, actualMulResult.x);
  }

  @Test
  void testDiv() {
    Vector2 newInstanceResult = Vector2.newInstance();
    Vector2 actualDivResult = newInstanceResult.div(10.0f);
    assertSame(newInstanceResult, actualDivResult);
    assertEquals(0.0f, actualDivResult.y);
    assertEquals(0.0f, actualDivResult.x);
  }

  @Test
  void testIsEqual() {
    Vector2 newInstanceResult = Vector2.newInstance();
    assertTrue(newInstanceResult.isEqual(Vector2.newInstance()));
  }

  @Test
  void testIsEqual2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 10.0f);
    assertFalse(newInstanceResult.isEqual(Vector2.newInstance()));
  }

  @Test
  void testIsEqual3() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(0.0f, 10.0f);
    assertFalse(newInstanceResult.isEqual(Vector2.newInstance()));
  }
}

