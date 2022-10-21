package com.tenio.engine.physic2d.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.engine.physic2d.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PathTest {
  @Test
  void testConstructor() {
    Path actualPath = new Path();
    actualPath.enableLoop(true);
    assertNull(actualPath.getCurrentWayPoint());
  }

  @Test
  void testConstructor2() {
    Path actualPath = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);

    assertFalse(actualPath.isEndOfWayPoints());
    List<Vector2> wayPoints = actualPath.getWayPoints();
    assertEquals(10, wayPoints.size());
    Vector2 currentWayPoint = actualPath.getCurrentWayPoint();
    assertEquals(10.0f, currentWayPoint.y);
    assertFalse(currentWayPoint.isZero());
    assertFalse(wayPoints.get(7).isZero());
    assertFalse(wayPoints.get(2).isZero());
    assertFalse(wayPoints.get(1).isZero());
    assertFalse(wayPoints.get(8).isZero());
    assertFalse(wayPoints.get(9).isZero());
  }

  @Test
  void testConstructor3() {
    Path actualPath = new Path(0, 10.0f, 10.0f, 10.0f, 10.0f, true);

    assertTrue(actualPath.isEndOfWayPoints());
    assertTrue(actualPath.getWayPoints().isEmpty());
  }

  @Test
  void testConstructor4() {
    Path actualPath = new Path(10, 2.0f, 10.0f, 10.0f, 10.0f, true);

    assertFalse(actualPath.isEndOfWayPoints());
    List<Vector2> wayPoints = actualPath.getWayPoints();
    assertEquals(10, wayPoints.size());
    Vector2 currentWayPoint = actualPath.getCurrentWayPoint();
    assertEquals(10.0f, currentWayPoint.y);
    assertFalse(currentWayPoint.isZero());
    assertFalse(wayPoints.get(7).isZero());
    assertFalse(wayPoints.get(2).isZero());
    assertFalse(wayPoints.get(1).isZero());
    assertFalse(wayPoints.get(8).isZero());
    assertFalse(wayPoints.get(9).isZero());
  }

  @Test
  void testIsEndOfWayPoints() {
    assertFalse((new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true)).isEndOfWayPoints());
    assertTrue((new Path(0, 10.0f, 10.0f, 10.0f, 10.0f, true)).isEndOfWayPoints());
  }

  @Test
  void testSetToNextWayPoint() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by setToNextWayPoint()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true)).setToNextWayPoint();
  }

  @Test
  void testSetToNextWayPoint2() {
    Path path = new Path(0, 10.0f, 10.0f, 10.0f, 10.0f, true);
    path.setToNextWayPoint();
    assertTrue(path.isEndOfWayPoints());
    assertTrue(path.getWayPoints().isEmpty());
  }

  @Test
  void testSetToNextWayPoint3() {
    Path path = new Path(1, 10.0f, 10.0f, 10.0f, 10.0f, true);
    path.setToNextWayPoint();
    assertTrue(path.isEndOfWayPoints());
    assertEquals(10.0f, path.getCurrentWayPoint().y);
  }

  @Test
  void testSetToNextWayPoint4() {
    Path path = new Path(1, 10.0f, 10.0f, 10.0f, 10.0f, false);
    path.setToNextWayPoint();
    assertTrue(path.isEndOfWayPoints());
    assertEquals(1, path.getWayPoints().size());
    assertEquals(10.0f, path.getCurrentWayPoint().y);
  }

  @Test
  @Disabled
  void testCreateRandomPath() {
    Path path = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);
    List<Vector2> actualCreateRandomPathResult =
        path.createRandomPath(10, 10.0f, 10.0f, 10.0f, 10.0f);
    assertEquals(10, actualCreateRandomPathResult.size());
    Vector2 getResult = actualCreateRandomPathResult.get(0);
    Vector2 getResult1 = actualCreateRandomPathResult.get(1);
    Vector2 getResult2 = actualCreateRandomPathResult.get(2);
    Vector2 getResult3 = actualCreateRandomPathResult.get(3);
    Vector2 getResult4 = actualCreateRandomPathResult.get(4);
    Vector2 getResult5 = actualCreateRandomPathResult.get(5);
    Vector2 getResult6 = actualCreateRandomPathResult.get(7);
    Vector2 getResult7 = actualCreateRandomPathResult.get(8);
    Vector2 getResult8 = actualCreateRandomPathResult.get(9);
    float actualResultFloat = getResult5.y;
    assertEquals(9.999999f, actualResultFloat);
    assertFalse(actualCreateRandomPathResult.get(6).isZero());
    assertFalse(getResult5.isZero());
    assertFalse(getResult2.isZero());
    assertFalse(getResult4.isZero());
    assertFalse(getResult7.isZero());
    assertFalse(getResult6.isZero());
    assertFalse(getResult3.isZero());
    assertFalse(getResult1.isZero());
    assertFalse(getResult8.isZero());
    assertFalse(getResult.isZero());
    assertEquals(10.0f, getResult.y);
    assertSame(getResult, path.getCurrentWayPoint());
    assertFalse(path.isEndOfWayPoints());
  }

  @Test
  void testCreateRandomPath2() {
    Path path = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);
    assertTrue(path.createRandomPath(0, 10.0f, 10.0f, 10.0f, 10.0f).isEmpty());
    assertTrue(path.isEndOfWayPoints());
  }

  @Test
  void testCreateRandomPath3() {
    Path path = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);
    List<Vector2> actualCreateRandomPathResult =
        path.createRandomPath(10, 2.0f, 10.0f, 10.0f, 10.0f);
    assertEquals(10, actualCreateRandomPathResult.size());
    assertFalse(actualCreateRandomPathResult.get(6).isZero());
    assertFalse(actualCreateRandomPathResult.get(5).isZero());
    assertFalse(actualCreateRandomPathResult.get(2).isZero());
    assertFalse(actualCreateRandomPathResult.get(4).isZero());
    assertFalse(actualCreateRandomPathResult.get(8).isZero());
    assertFalse(actualCreateRandomPathResult.get(7).isZero());
    assertFalse(actualCreateRandomPathResult.get(3).isZero());
    assertFalse(actualCreateRandomPathResult.get(1).isZero());
    assertFalse(actualCreateRandomPathResult.get(9).isZero());
    Vector2 getResult = actualCreateRandomPathResult.get(0);
    assertFalse(getResult.isZero());
    assertEquals(10.0f, getResult.y);
    assertSame(getResult, path.getCurrentWayPoint());
    assertFalse(path.isEndOfWayPoints());
  }

  @Test
  void testSetPath() {
    Path path = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);
    Path path1 = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);

    path.setPath(path1);
    Vector2 expectedCurrentWayPoint = path1.getCurrentWayPoint();
    assertSame(expectedCurrentWayPoint, path.getCurrentWayPoint());
    assertFalse(path.isEndOfWayPoints());
    List<Vector2> expectedWayPoints = path1.getWayPoints();
    assertSame(expectedWayPoints, path.getWayPoints());
  }

  @Test
  void testClear() {
    Path path = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);
    path.clear();
    assertTrue(path.getWayPoints().isEmpty());
  }

  @Test
  void testSetWayPoints() {
    Path path = new Path(10, 10.0f, 10.0f, 10.0f, 10.0f, true);

    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    path.setWayPoints(vector2List);
    Vector2 expectedCurrentWayPoint = vector2List.get(0);
    assertSame(expectedCurrentWayPoint, path.getCurrentWayPoint());
    assertTrue(path.isEndOfWayPoints());
    assertSame(vector2List, path.getWayPoints());
  }
}

