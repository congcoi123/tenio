package com.tenio.engine.physic2d.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InvertedAabbBox2DTest {
  @Test
  void testNewInstance() {
    InvertedAabbBox2D actualNewInstanceResult = InvertedAabbBox2D.newInstance();
    assertEquals(0.0f, actualNewInstanceResult.getBottom());
    assertEquals(0.0f, actualNewInstanceResult.getTop());
    assertEquals(0.0f, actualNewInstanceResult.getRight());
    assertEquals(0.0f, actualNewInstanceResult.getLeft());
  }

  @Test
  void testValueOf() {
    InvertedAabbBox2D actualValueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f);
    actualValueOfResult.setBottom(10.0f);
    actualValueOfResult.setLeft(10.0f);
    actualValueOfResult.setRight(10.0f);
    actualValueOfResult.setTop(10.0f);
    assertEquals(10.0f, actualValueOfResult.getBottom());
    assertEquals(10.0f, actualValueOfResult.getLeft());
    assertEquals(10.0f, actualValueOfResult.getRight());
    assertEquals(10.0f, actualValueOfResult.getTop());
  }

  @Test
  void testValueOf2() {
    InvertedAabbBox2D actualValueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f);
    assertEquals(10.0f, actualValueOfResult.getBottom());
    assertEquals(10.0f, actualValueOfResult.getTop());
    assertEquals(10.0f, actualValueOfResult.getRight());
    assertEquals(10.0f, actualValueOfResult.getLeft());
  }

  @Test
  void testIsOverlappedWith() {
    InvertedAabbBox2D valueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f);
    assertTrue(
        valueOfResult.isOverlappedWith(InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f)));
  }

  @Test
  void testIsOverlappedWith2() {
    InvertedAabbBox2D valueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 0.5f, 10.0f);
    assertFalse(
        valueOfResult.isOverlappedWith(InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f)));
  }

  @Test
  void testIsOverlappedWith3() {
    InvertedAabbBox2D valueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 0.5f);
    assertFalse(
        valueOfResult.isOverlappedWith(InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f)));
  }

  @Test
  void testIsOverlappedWith4() {
    InvertedAabbBox2D valueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f);
    assertFalse(
        valueOfResult.isOverlappedWith(InvertedAabbBox2D.valueOf(10.0f, 10.0f, 0.5f, 10.0f)));
  }

  @Test
  void testIsOverlappedWith5() {
    InvertedAabbBox2D valueOfResult = InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 10.0f);
    assertFalse(
        valueOfResult.isOverlappedWith(InvertedAabbBox2D.valueOf(10.0f, 10.0f, 10.0f, 0.5f)));
  }
}

