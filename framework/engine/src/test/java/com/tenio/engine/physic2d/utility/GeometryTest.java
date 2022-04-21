package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenio.engine.physic2d.math.Vector2;
import org.junit.jupiter.api.Test;

class GeometryTest {
  @Test
  void testGetDistanceRayPlaneIntersection() {
    Vector2 rayOrigin = Vector2.newInstance();
    Vector2 rayHeading = Vector2.newInstance();
    Vector2 planePoint = Vector2.newInstance();
    assertEquals(-1.0f,
        Geometry.getDistanceRayPlaneIntersection(rayOrigin, rayHeading, planePoint,
            Vector2.newInstance()));
  }

  @Test
  void testGetDistanceRayPlaneIntersection2() {
    Vector2 rayOrigin = Vector2.newInstance();
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.NaN, -1.0f);
    Vector2 planePoint = Vector2.newInstance();
    assertEquals(Float.NaN,
        Geometry.getDistanceRayPlaneIntersection(rayOrigin, newInstanceResult, planePoint,
            Vector2.newInstance()));
  }

  @Test
  void testGetDistanceRayPlaneIntersection3() {
    Vector2 rayOrigin = Vector2.newInstance();
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(-1.0f, -1.0f);
    Vector2 planePoint = Vector2.newInstance();
    Vector2 newInstanceResult1 = Vector2.newInstance();
    newInstanceResult1.add(10.0f, 10.0f);
    assertEquals(0.0f,
        Geometry.getDistanceRayPlaneIntersection(rayOrigin, newInstanceResult, planePoint,
            newInstanceResult1));
  }

  @Test
  void testGetDistanceRayCircleIntersect() {
    Vector2 rayOrigin = Vector2.newInstance();
    Vector2 rayHeading = Vector2.newInstance();
    assertEquals(-10.0f,
        Geometry.getDistanceRayCircleIntersect(rayOrigin, rayHeading, Vector2.newInstance(),
            10.0f));
  }

  @Test
  void testGetDistanceRayCircleIntersect2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 10.0f);
    newInstanceResult.add(0.0f, 0.0f);
    Vector2 rayHeading = Vector2.newInstance();
    assertEquals(-1.0f,
        Geometry.getDistanceRayCircleIntersect(newInstanceResult, rayHeading, Vector2.newInstance(),
            10.0f));
  }

  @Test
  void testGetDistancePointSegment() {
    Vector2 vectorA = Vector2.newInstance();
    Vector2 vectorB = Vector2.newInstance();
    assertEquals(0.0f, Geometry.getDistancePointSegment(vectorA, vectorB, Vector2.newInstance()));
  }

  @Test
  void testGetDistancePointSegment2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 0.0f);
    Vector2 vectorB = Vector2.newInstance();
    assertEquals(0.0f,
        Geometry.getDistancePointSegment(newInstanceResult, vectorB, Vector2.newInstance()));
  }

  @Test
  void testGetDistancePointSegment3() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.NaN, 0.0f);
    Vector2 vectorB = Vector2.newInstance();
    assertEquals(Float.NaN,
        Geometry.getDistancePointSegment(newInstanceResult, vectorB, Vector2.newInstance()));
  }

  @Test
  void testGetDistancePointSegmentSqr() {
    Vector2 vectorA = Vector2.newInstance();
    Vector2 vectorB = Vector2.newInstance();
    assertEquals(0.0f,
        Geometry.getDistancePointSegmentSqr(vectorA, vectorB, Vector2.newInstance()));
  }

  @Test
  void testGetDistancePointSegmentSqr2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 0.0f);
    Vector2 vectorB = Vector2.newInstance();
    assertEquals(0.0f,
        Geometry.getDistancePointSegmentSqr(newInstanceResult, vectorB, Vector2.newInstance()));
  }

  @Test
  void testGetDistancePointSegmentSqr3() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.NaN, 0.0f);
    Vector2 vectorB = Vector2.newInstance();
    assertEquals(Float.NaN,
        Geometry.getDistancePointSegmentSqr(newInstanceResult, vectorB, Vector2.newInstance()));
  }

  @Test
  void testGetPointTwoSegmentIntersect() {
    Vector2 vectorA = Vector2.newInstance();
    Vector2 vectorB = Vector2.newInstance();
    Vector2 vectorC = Vector2.newInstance();
    assertNull(
        Geometry.getPointTwoSegmentIntersect(vectorA, vectorB, vectorC, Vector2.newInstance()));
  }

  @Test
  void testGetPointTwoSegmentIntersect2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.NaN, 0.0f);
    Vector2 vectorB = Vector2.newInstance();
    Vector2 vectorC = Vector2.newInstance();
    assertNull(Geometry.getPointTwoSegmentIntersect(newInstanceResult, vectorB, vectorC,
        Vector2.newInstance()));
  }

  @Test
  void testGetDistanceTwoSegmentIntersect() {
    Vector2 vectorA = Vector2.newInstance();
    Vector2 vectorB = Vector2.newInstance();
    Vector2 vectorC = Vector2.newInstance();
    assertEquals(-1.0f,
        Geometry.getDistanceTwoSegmentIntersect(vectorA, vectorB, vectorC, Vector2.newInstance()));
  }

  @Test
  void testGetDistanceTwoSegmentIntersect2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.NaN, 0.0f);
    Vector2 vectorB = Vector2.newInstance();
    Vector2 vectorC = Vector2.newInstance();
    assertEquals(-1.0f,
        Geometry.getDistanceTwoSegmentIntersect(newInstanceResult, vectorB, vectorC,
            Vector2.newInstance()));
  }

  @Test
  void testGetCircleArea() {
    assertEquals(314.15927f, Geometry.getCircleArea(10.0f));
  }
}

