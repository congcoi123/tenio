package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utility.Geometry.SpanType;
import java.util.ArrayList;
import java.util.List;
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

  // ---- getWhereIsPoint ----

  @Test
  void testGetWhereIsPointPlaneFront() {
    var point = Vector2.newInstance();
    point.set(1.0f, 0.0f);
    var pointOnPlane = Vector2.newInstance();
    pointOnPlane.set(0.0f, 0.0f);
    var normal = Vector2.newInstance();
    normal.set(1.0f, 0.0f);
    assertEquals(SpanType.PLANE_FRONT, Geometry.getWhereIsPoint(point, pointOnPlane, normal));
  }

  @Test
  void testGetWhereIsPointPlaneBackside() {
    var point = Vector2.newInstance();
    point.set(-1.0f, 0.0f);
    var pointOnPlane = Vector2.newInstance();
    pointOnPlane.set(0.0f, 0.0f);
    var normal = Vector2.newInstance();
    normal.set(1.0f, 0.0f);
    assertEquals(SpanType.PLANE_BACKSIDE, Geometry.getWhereIsPoint(point, pointOnPlane, normal));
  }

  @Test
  void testGetWhereIsPointOnPlane() {
    var point = Vector2.newInstance();
    point.set(0.0f, 1.0f);
    var pointOnPlane = Vector2.newInstance();
    pointOnPlane.set(0.0f, 0.0f);
    var normal = Vector2.newInstance();
    normal.set(1.0f, 0.0f);
    assertEquals(SpanType.ON_PLANE, Geometry.getWhereIsPoint(point, pointOnPlane, normal));
  }

  // ---- isRayCircleIntersect ----

  @Test
  void testIsRayCircleIntersectTrue() {
    var origin = Vector2.newInstance();
    origin.set(0.0f, 0.0f);
    var heading = Vector2.newInstance();
    heading.set(1.0f, 0.0f);
    var circle = Vector2.newInstance();
    circle.set(5.0f, 0.0f);
    assertTrue(Geometry.isRayCircleIntersect(origin, heading, circle, 2.0f));
  }

  @Test
  void testIsRayCircleIntersectFalse() {
    var origin = Vector2.newInstance();
    origin.set(0.0f, 0.0f);
    var heading = Vector2.newInstance();
    heading.set(1.0f, 0.0f);
    var circle = Vector2.newInstance();
    circle.set(0.0f, 10.0f);
    assertFalse(Geometry.isRayCircleIntersect(origin, heading, circle, 1.0f));
  }

  // ---- getTangentPoints ----

  @Test
  void testGetTangentPointsInsideCircle() {
    var center = Vector2.newInstance();
    center.set(0.0f, 0.0f);
    var vector = Vector2.newInstance();
    vector.set(0.0f, 0.0f); // inside circle radius=10
    assertNull(Geometry.getTangentPoints(center, 10.0f, vector));
  }

  @Test
  void testGetTangentPointsOutsideCircle() {
    var center = Vector2.newInstance();
    center.set(0.0f, 0.0f);
    var vector = Vector2.newInstance();
    vector.set(10.0f, 0.0f); // outside circle radius=5
    assertNotNull(Geometry.getTangentPoints(center, 5.0f, vector));
  }

  // ---- isTwoSegmentIntersect ----

  @Test
  void testIsTwoSegmentIntersectParallel() {
    // Two horizontal parallel segments
    var a1 = Vector2.newInstance();
    a1.set(0.0f, 0.0f);
    var b1 = Vector2.newInstance();
    b1.set(10.0f, 0.0f);
    var c1 = Vector2.newInstance();
    c1.set(0.0f, 1.0f);
    var d1 = Vector2.newInstance();
    d1.set(10.0f, 1.0f);
    assertFalse(Geometry.isTwoSegmentIntersect(a1, b1, c1, d1));
  }

  @Test
  void testIsTwoSegmentIntersectTrue() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(2.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(1.0f, -1.0f);
    var d = Vector2.newInstance();
    d.set(1.0f, 1.0f);
    assertTrue(Geometry.isTwoSegmentIntersect(a, b, c, d));
  }

  @Test
  void testIsTwoSegmentIntersectFalse() {
    // Non-parallel but not crossing segments
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(1.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(5.0f, 0.0f);
    var d = Vector2.newInstance();
    d.set(6.0f, 1.0f);
    assertFalse(Geometry.isTwoSegmentIntersect(a, b, c, d));
  }

  // ---- getPointTwoSegmentIntersect ----

  @Test
  void testGetPointTwoSegmentIntersectFound() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(2.0f, 2.0f);
    var c = Vector2.newInstance();
    c.set(0.0f, 2.0f);
    var d = Vector2.newInstance();
    d.set(2.0f, 0.0f);
    var result = Geometry.getPointTwoSegmentIntersect(a, b, c, d);
    assertNotNull(result);
  }

  // ---- getDistanceTwoSegmentIntersect ----

  @Test
  void testGetDistanceTwoSegmentIntersectFound() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(2.0f, 2.0f);
    var c = Vector2.newInstance();
    c.set(0.0f, 2.0f);
    var d = Vector2.newInstance();
    d.set(2.0f, 0.0f);
    float dist = Geometry.getDistanceTwoSegmentIntersect(a, b, c, d);
    assertTrue(dist > 0);
  }

  // ---- isTwoObjectsIntersect ----

  @Test
  void testIsTwoObjectsIntersectTrue() {
    List<Vector2> obj1 = new ArrayList<>();
    var v1 = Vector2.newInstance();
    v1.set(0.0f, 0.0f);
    var v2 = Vector2.newInstance();
    v2.set(2.0f, 0.0f);
    var v3 = Vector2.newInstance();
    v3.set(2.0f, 2.0f);
    obj1.add(v1);
    obj1.add(v2);
    obj1.add(v3);

    List<Vector2> obj2 = new ArrayList<>();
    var v4 = Vector2.newInstance();
    v4.set(1.0f, -1.0f);
    var v5 = Vector2.newInstance();
    v5.set(1.0f, 3.0f);
    var v6 = Vector2.newInstance();
    v6.set(3.0f, 1.0f);
    obj2.add(v4);
    obj2.add(v5);
    obj2.add(v6);

    assertTrue(Geometry.isTwoObjectsIntersect(obj1, obj2));
  }

  @Test
  void testIsTwoObjectsIntersectFalse() {
    List<Vector2> obj1 = new ArrayList<>();
    List<Vector2> obj2 = new ArrayList<>();
    assertFalse(Geometry.isTwoObjectsIntersect(obj1, obj2));
  }

  // ---- isSegmentObjectIntersect ----

  @Test
  void testIsSegmentObjectIntersectTrue() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(2.0f, 0.0f);

    List<Vector2> obj = new ArrayList<>();
    var v1 = Vector2.newInstance();
    v1.set(1.0f, -1.0f);
    var v2 = Vector2.newInstance();
    v2.set(1.0f, 1.0f);
    obj.add(v1);
    obj.add(v2);

    assertTrue(Geometry.isSegmentObjectIntersect(a, b, obj));
  }

  @Test
  void testIsSegmentObjectIntersectFalse() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(1.0f, 0.0f);
    List<Vector2> obj = new ArrayList<>();
    assertFalse(Geometry.isSegmentObjectIntersect(a, b, obj));
  }

  // ---- isTwoCirclesOverlapped (float) ----

  @Test
  void testIsTwoCirclesOverlappedTrueFloat() {
    assertTrue(Geometry.isTwoCirclesOverlapped(0.0f, 0.0f, 5.0f, 3.0f, 0.0f, 5.0f));
  }

  @Test
  void testIsTwoCirclesOverlappedFalseFloat() {
    assertFalse(Geometry.isTwoCirclesOverlapped(0.0f, 0.0f, 1.0f, 10.0f, 0.0f, 1.0f));
  }

  // ---- isTwoCirclesOverlapped (Vector2) ----

  @Test
  void testIsTwoCirclesOverlappedTrueVector() {
    var c1 = Vector2.newInstance();
    c1.set(0.0f, 0.0f);
    var c2 = Vector2.newInstance();
    c2.set(3.0f, 0.0f);
    assertTrue(Geometry.isTwoCirclesOverlapped(c1, 5.0f, c2, 5.0f));
  }

  @Test
  void testIsTwoCirclesOverlappedFalseVector() {
    var c1 = Vector2.newInstance();
    c1.set(0.0f, 0.0f);
    var c2 = Vector2.newInstance();
    c2.set(10.0f, 0.0f);
    assertFalse(Geometry.isTwoCirclesOverlapped(c1, 1.0f, c2, 1.0f));
  }

  // ---- isTwoCirclesEnclosed ----

  @Test
  void testIsTwoCirclesEnclosedTrue() {
    // Small circle (1,0,1) inside large circle (0,0,10)
    assertTrue(Geometry.isTwoCirclesEnclosed(0.0f, 0.0f, 10.0f, 1.0f, 0.0f, 1.0f));
  }

  @Test
  void testIsTwoCirclesEnclosedFalse() {
    assertFalse(Geometry.isTwoCirclesEnclosed(0.0f, 0.0f, 5.0f, 10.0f, 0.0f, 5.0f));
  }

  // ---- getTwoCirclesIntersectionPoints ----

  @Test
  void testGetTwoCirclesIntersectionPointsNoOverlap() {
    assertNull(Geometry.getTwoCirclesIntersectionPoints(0.0f, 0.0f, 1.0f, 100.0f, 0.0f, 1.0f));
  }

  @Test
  void testGetTwoCirclesIntersectionPointsOverlap() {
    float[] points =
        Geometry.getTwoCirclesIntersectionPoints(0.0f, 0.0f, 5.0f, 3.0f, 0.0f, 5.0f);
    assertNotNull(points);
    assertEquals(4, points.length);
  }

  // ---- getTwoCirclesIntersectionArea ----

  @Test
  void testGetTwoCirclesIntersectionAreaNoOverlap() {
    float area =
        Geometry.getTwoCirclesIntersectionArea(0.0f, 0.0f, 1.0f, 100.0f, 0.0f, 1.0f);
    assertEquals(0.0f, area, 0.001f);
  }

  @Test
  void testGetTwoCirclesIntersectionAreaOverlap() {
    float area =
        Geometry.getTwoCirclesIntersectionArea(0.0f, 0.0f, 5.0f, 3.0f, 0.0f, 5.0f);
    assertTrue(area > 0);
  }

  // ---- isPointInCircle ----

  @Test
  void testIsPointInCircleTrue() {
    var center = Vector2.newInstance();
    center.set(0.0f, 0.0f);
    var point = Vector2.newInstance();
    point.set(1.0f, 0.0f);
    assertTrue(Geometry.isPointInCircle(center, 5.0f, point));
  }

  @Test
  void testIsPointInCircleFalse() {
    var center = Vector2.newInstance();
    center.set(0.0f, 0.0f);
    var point = Vector2.newInstance();
    point.set(10.0f, 0.0f);
    assertFalse(Geometry.isPointInCircle(center, 5.0f, point));
  }

  // ---- isSegmentCircleIntersectAtPoint ----

  @Test
  void testIsSegmentCircleIntersectAtPointTrue() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(10.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(5.0f, 0.0f);
    assertTrue(Geometry.isSegmentCircleIntersectAtPoint(a, b, c, 3.0f));
  }

  @Test
  void testIsSegmentCircleIntersectAtPointFalse() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(1.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(0.0f, 100.0f);
    assertFalse(Geometry.isSegmentCircleIntersectAtPoint(a, b, c, 1.0f));
  }

  // ---- isSegmentCircleClosestIntersectPoint ----

  @Test
  void testIsSegmentCircleClosestIntersectPointFound() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(10.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(5.0f, 0.0f);
    var intersection = Vector2.newInstance();
    assertTrue(Geometry.isSegmentCircleClosestIntersectPoint(a, b, c, 2.0f, intersection));
  }

  @Test
  void testIsSegmentCircleClosestIntersectPointNotFound() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(1.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(0.0f, 100.0f);
    var intersection = Vector2.newInstance();
    assertFalse(Geometry.isSegmentCircleClosestIntersectPoint(a, b, c, 1.0f, intersection));
  }

  // ---- insideRegion / notInsideRegion (Vector2) ----

  @Test
  void testInsideRegionTrue() {
    var point = Vector2.newInstance();
    point.set(5.0f, 5.0f);
    var topLeft = Vector2.newInstance();
    topLeft.set(0.0f, 0.0f);
    var botRight = Vector2.newInstance();
    botRight.set(10.0f, 10.0f);
    assertTrue(Geometry.insideRegion(point, topLeft, botRight));
  }

  @Test
  void testInsideRegionFalse() {
    var point = Vector2.newInstance();
    point.set(20.0f, 5.0f);
    var topLeft = Vector2.newInstance();
    topLeft.set(0.0f, 0.0f);
    var botRight = Vector2.newInstance();
    botRight.set(10.0f, 10.0f);
    assertFalse(Geometry.insideRegion(point, topLeft, botRight));
  }

  @Test
  void testNotInsideRegionTrue() {
    var point = Vector2.newInstance();
    point.set(20.0f, 5.0f);
    var topLeft = Vector2.newInstance();
    topLeft.set(0.0f, 0.0f);
    var botRight = Vector2.newInstance();
    botRight.set(10.0f, 10.0f);
    assertTrue(Geometry.notInsideRegion(point, topLeft, botRight));
  }

  @Test
  void testNotInsideRegionFalse() {
    var point = Vector2.newInstance();
    point.set(5.0f, 5.0f);
    var topLeft = Vector2.newInstance();
    topLeft.set(0.0f, 0.0f);
    var botRight = Vector2.newInstance();
    botRight.set(10.0f, 10.0f);
    assertFalse(Geometry.notInsideRegion(point, topLeft, botRight));
  }

  // ---- insideRegion (int overload) ----

  @Test
  void testInsideRegionIntTrue() {
    var point = Vector2.newInstance();
    point.set(5.0f, 5.0f);
    assertTrue(Geometry.insideRegion(point, 0, 0, 10, 10));
  }

  @Test
  void testInsideRegionIntFalse() {
    var point = Vector2.newInstance();
    point.set(20.0f, 5.0f);
    assertFalse(Geometry.insideRegion(point, 0, 0, 10, 10));
  }

  // ---- isSecondInFovoFirst ----

  @Test
  void testIsSecondInFovoFirstTrue() {
    // posFirst at origin, facing right (1,0), target directly to the right
    var posFirst = Vector2.newInstance();
    posFirst.set(0.0f, 0.0f);
    var facing = Vector2.newInstance();
    facing.set(1.0f, 0.0f);
    var posSecond = Vector2.newInstance();
    posSecond.set(10.0f, 0.0f);
    // fov = PI (180 degrees) — target ahead
    assertTrue(Geometry.isSecondInFovoFirst(posFirst, facing, posSecond, (float) Math.PI));
  }

  @Test
  void testIsSecondInFovoFirstFalse() {
    // posFirst at origin, facing right (1,0), target directly behind
    var posFirst = Vector2.newInstance();
    posFirst.set(0.0f, 0.0f);
    var facing = Vector2.newInstance();
    facing.set(1.0f, 0.0f);
    var posSecond = Vector2.newInstance();
    posSecond.set(-10.0f, 0.0f);
    // fov = PI/2 (90 degrees) — target behind, not in fov
    assertFalse(Geometry.isSecondInFovoFirst(posFirst, facing, posSecond, (float) (Math.PI / 2)));
  }

  @Test
  void testConstructorInstantiation() {
    assertDoesNotThrow(() -> new Geometry());
  }

  @Test
  void testIsTwoObjectsIntersectNonEmptyFalse() {
    List<Vector2> obj1 = new ArrayList<>();
    var v1 = Vector2.newInstance();
    v1.set(0.0f, 0.0f);
    var v2 = Vector2.newInstance();
    v2.set(1.0f, 0.0f);
    obj1.add(v1);
    obj1.add(v2);

    List<Vector2> obj2 = new ArrayList<>();
    var v3 = Vector2.newInstance();
    v3.set(5.0f, 5.0f);
    var v4 = Vector2.newInstance();
    v4.set(6.0f, 5.0f);
    obj2.add(v3);
    obj2.add(v4);

    assertFalse(Geometry.isTwoObjectsIntersect(obj1, obj2));
  }

  @Test
  void testIsSegmentObjectIntersectNonEmptyFalse() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(1.0f, 0.0f);

    List<Vector2> obj = new ArrayList<>();
    var v1 = Vector2.newInstance();
    v1.set(5.0f, 5.0f);
    var v2 = Vector2.newInstance();
    v2.set(6.0f, 5.0f);
    obj.add(v1);
    obj.add(v2);

    assertFalse(Geometry.isSegmentObjectIntersect(a, b, obj));
  }

  @Test
  void testIsSegmentCircleClosestIntersectPointCircleBehindA() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(10.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(-10.0f, 0.0f);
    var intersection = Vector2.newInstance();
    assertFalse(Geometry.isSegmentCircleClosestIntersectPoint(a, b, c, 1.0f, intersection));
  }

  @Test
  void testIsSegmentCircleClosestIntersectPointIpNegative() {
    var a = Vector2.newInstance();
    a.set(0.0f, 0.0f);
    var b = Vector2.newInstance();
    b.set(10.0f, 0.0f);
    var c = Vector2.newInstance();
    c.set(0.0f, 0.0f);
    var intersection = Vector2.newInstance();
    assertTrue(Geometry.isSegmentCircleClosestIntersectPoint(a, b, c, 1.0f, intersection));
  }
}

