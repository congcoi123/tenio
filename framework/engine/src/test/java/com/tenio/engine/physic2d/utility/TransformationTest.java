package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.engine.physic2d.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransformationTest {
  @Test
  void testPointsToWorldSpace() {
    ArrayList<Vector2> points = new ArrayList<Vector2>();
    Vector2 position = Vector2.newInstance();
    Vector2 forward = Vector2.newInstance();
    assertTrue(Transformation.pointsToWorldSpace(points, position, forward, Vector2.newInstance())
        .isEmpty());
  }

  @Test
  void testPointsToWorldSpace2() {
    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    Vector2 position = Vector2.newInstance();
    Vector2 forward = Vector2.newInstance();
    List<Vector2> actualPointsToWorldSpaceResult =
        Transformation.pointsToWorldSpace(vector2List, position, forward,
            Vector2.newInstance());
    assertEquals(1, actualPointsToWorldSpaceResult.size());
    Vector2 getResult = actualPointsToWorldSpaceResult.get(0);
    assertEquals(0.0f, getResult.y);
    assertEquals(0.0f, getResult.x);
  }

  @Test
  void testPointsToWorldSpace3() {
    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    vector2List.add(Vector2.newInstance());
    Vector2 position = Vector2.newInstance();
    Vector2 forward = Vector2.newInstance();
    List<Vector2> actualPointsToWorldSpaceResult =
        Transformation.pointsToWorldSpace(vector2List, position, forward,
            Vector2.newInstance());
    assertEquals(2, actualPointsToWorldSpaceResult.size());
    Vector2 getResult = actualPointsToWorldSpaceResult.get(0);
    assertEquals(0.0f, getResult.y);
    Vector2 getResult1 = actualPointsToWorldSpaceResult.get(1);
    assertEquals(0.0f, getResult1.y);
    assertEquals(0.0f, getResult1.x);
    assertEquals(0.0f, getResult.x);
  }

  @Test
  void testPointsToWorldSpace4() {
    ArrayList<Vector2> points = new ArrayList<Vector2>();
    Vector2 position = Vector2.newInstance();
    Vector2 forward = Vector2.newInstance();
    Vector2 side = Vector2.newInstance();
    assertTrue(
        Transformation.pointsToWorldSpace(points, position, forward, side, Vector2.newInstance())
            .isEmpty());
  }

  @Test
  void testPointsToWorldSpace5() {
    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    Vector2 position = Vector2.newInstance();
    Vector2 forward = Vector2.newInstance();
    Vector2 side = Vector2.newInstance();
    List<Vector2> actualPointsToWorldSpaceResult =
        Transformation.pointsToWorldSpace(vector2List, position, forward,
            side, Vector2.newInstance());
    assertEquals(1, actualPointsToWorldSpaceResult.size());
    Vector2 getResult = actualPointsToWorldSpaceResult.get(0);
    assertEquals(0.0f, getResult.y);
    assertEquals(0.0f, getResult.x);
  }

  @Test
  void testPointsToWorldSpace6() {
    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    vector2List.add(Vector2.newInstance());
    Vector2 position = Vector2.newInstance();
    Vector2 forward = Vector2.newInstance();
    Vector2 side = Vector2.newInstance();
    List<Vector2> actualPointsToWorldSpaceResult =
        Transformation.pointsToWorldSpace(vector2List, position, forward,
            side, Vector2.newInstance());
    assertEquals(2, actualPointsToWorldSpaceResult.size());
    Vector2 getResult = actualPointsToWorldSpaceResult.get(0);
    assertEquals(0.0f, getResult.y);
    Vector2 getResult1 = actualPointsToWorldSpaceResult.get(1);
    assertEquals(0.0f, getResult1.y);
    assertEquals(0.0f, getResult1.x);
    assertEquals(0.0f, getResult.x);
  }

  @Test
  void testPointToWorldSpace() {
    Vector2 point = Vector2.newInstance();
    Vector2 agentHeading = Vector2.newInstance();
    Vector2 agentSide = Vector2.newInstance();
    Vector2 actualPointToWorldSpaceResult =
        Transformation.pointToWorldSpace(point, agentHeading, agentSide,
            Vector2.newInstance());
    assertEquals(0.0f, actualPointToWorldSpaceResult.y);
    assertEquals(0.0f, actualPointToWorldSpaceResult.x);
  }

  @Test
  void testVectorToWorldSpace() {
    Vector2 vector = Vector2.newInstance();
    Vector2 agentHeading = Vector2.newInstance();
    Vector2 actualVectorToWorldSpaceResult = Transformation.vectorToWorldSpace(vector, agentHeading,
        Vector2.newInstance());
    assertEquals(0.0f, actualVectorToWorldSpaceResult.y);
    assertEquals(0.0f, actualVectorToWorldSpaceResult.x);
  }

  @Test
  void testPointToLocalSpace() {
    Vector2 point = Vector2.newInstance();
    Vector2 agentHeading = Vector2.newInstance();
    Vector2 agentSide = Vector2.newInstance();
    Vector2 actualPointToLocalSpaceResult =
        Transformation.pointToLocalSpace(point, agentHeading, agentSide,
            Vector2.newInstance());
    assertEquals(0.0f, actualPointToLocalSpaceResult.y);
    assertEquals(0.0f, actualPointToLocalSpaceResult.x);
  }

  @Test
  void testVectorToLocalSpace() {
    Vector2 vector = Vector2.newInstance();
    Vector2 agentHeading = Vector2.newInstance();
    Vector2 actualVectorToLocalSpaceResult = Transformation.vectorToLocalSpace(vector, agentHeading,
        Vector2.newInstance());
    assertEquals(0.0f, actualVectorToLocalSpaceResult.y);
    assertEquals(0.0f, actualVectorToLocalSpaceResult.x);
  }

  @Test
  void testVec2dRotateAroundOrigin() {
    Vector2 actualVec2dRotateAroundOriginResult =
        Transformation.vec2dRotateAroundOrigin(10.0f, 10.0f, 10.0f);
    assertEquals(-13.830926f, actualVec2dRotateAroundOriginResult.y);
    assertEquals(-2.9505033f, actualVec2dRotateAroundOriginResult.x);
  }

  @Test
  void testVec2dRotateAroundOrigin2() {
    Vector2 actualVec2dRotateAroundOriginResult =
        Transformation.vec2dRotateAroundOrigin(Vector2.newInstance(), 10.0f);
    assertEquals(0.0f, actualVec2dRotateAroundOriginResult.y);
    assertEquals(0.0f, actualVec2dRotateAroundOriginResult.x);
  }

  @Test
  void testCreateWhiskers() {
    Vector2 facing = Vector2.newInstance();
    List<Vector2> actualCreateWhiskersResult =
        Transformation.createWhiskers(10, 10.0f, 10.0f, facing,
            Vector2.newInstance());
    assertEquals(10, actualCreateWhiskersResult.size());
    Vector2 getResult = actualCreateWhiskersResult.get(5);
    assertEquals(0.0f, getResult.y);
    Vector2 getResult1 = actualCreateWhiskersResult.get(6);
    assertEquals(0.0f, getResult1.y);
    assertEquals(0.0f, getResult1.x);
    assertEquals(0.0f, getResult.x);
    Vector2 getResult2 = actualCreateWhiskersResult.get(2);
    assertEquals(0.0f, getResult2.x);
    Vector2 getResult3 = actualCreateWhiskersResult.get(4);
    assertEquals(0.0f, getResult3.x);
    Vector2 getResult4 = actualCreateWhiskersResult.get(8);
    assertEquals(0.0f, getResult4.x);
    Vector2 getResult5 = actualCreateWhiskersResult.get(7);
    assertEquals(0.0f, getResult5.x);
    Vector2 getResult6 = actualCreateWhiskersResult.get(3);
    assertEquals(0.0f, getResult6.x);
    Vector2 getResult7 = actualCreateWhiskersResult.get(1);
    assertEquals(0.0f, getResult7.x);
    Vector2 getResult8 = actualCreateWhiskersResult.get(9);
    assertEquals(0.0f, getResult8.x);
    Vector2 getResult9 = actualCreateWhiskersResult.get(0);
    assertEquals(0.0f, getResult9.x);
    assertEquals(0.0f, getResult8.y);
    assertEquals(0.0f, getResult9.y);
    assertEquals(0.0f, getResult3.y);
    assertEquals(0.0f, getResult4.y);
    assertEquals(0.0f, getResult6.y);
    assertEquals(0.0f, getResult7.y);
    assertEquals(0.0f, getResult2.y);
    assertEquals(0.0f, getResult5.y);
  }

  @Test
  void testWrapAround() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by wrapAround(Vector2, int, int)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Transformation.wrapAround(Vector2.newInstance(), 3, 3);
  }

  @Test
  void testWrapAround2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 3.0f);
    Vector2 actualWrapAroundResult = Transformation.wrapAround(newInstanceResult, 3, 3);
    assertEquals(3.0f, actualWrapAroundResult.y);
    assertEquals(0.0f, actualWrapAroundResult.x);
    assertEquals(0.0f, newInstanceResult.x);
  }

  @Test
  void testWrapAround3() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(-1.0f, 3.0f);
    Vector2 actualWrapAroundResult = Transformation.wrapAround(newInstanceResult, 3, 3);
    assertEquals(3.0f, actualWrapAroundResult.y);
    assertEquals(3.0f, actualWrapAroundResult.x);
    assertEquals(3.0f, newInstanceResult.x);
  }

  @Test
  void testWrapAround4() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(3.0f, 10.0f);
    Vector2 actualWrapAroundResult = Transformation.wrapAround(newInstanceResult, 3, 3);
    assertEquals(0.0f, actualWrapAroundResult.y);
    assertEquals(3.0f, actualWrapAroundResult.x);
    assertEquals(0.0f, newInstanceResult.y);
  }

  @Test
  void testWrapAround5() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(3.0f, -1.0f);
    Vector2 actualWrapAroundResult = Transformation.wrapAround(newInstanceResult, 3, 3);
    assertEquals(3.0f, actualWrapAroundResult.y);
    assertEquals(3.0f, actualWrapAroundResult.x);
    assertEquals(3.0f, newInstanceResult.y);
  }
}

