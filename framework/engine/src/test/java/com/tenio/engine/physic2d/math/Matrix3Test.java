package com.tenio.engine.physic2d.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class Matrix3Test {
  @Test
  void testNewInstance() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     Matrix3.matrix
    //     Matrix3.tempMatrix

    Matrix3.newInstance();
  }

  @Test
  void testNewInstance2() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     Matrix3.matrix
    //     Matrix3.tempMatrix

    Matrix3.newInstance();
  }

  @Test
  void testInitialize() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     Matrix3.matrix
    //     Matrix3.tempMatrix

    Matrix3.newInstance().initialize();
  }

  @Test
  void testTransformVector2Ds() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by transformVector2Ds(List)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Matrix3 newInstanceResult = Matrix3.newInstance();
    newInstanceResult.transformVector2Ds(new ArrayList<Vector2>());
  }

  @Test
  void testTransformVector2Ds2() {
    Matrix3 newInstanceResult = Matrix3.newInstance();

    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    newInstanceResult.transformVector2Ds(vector2List);
    Vector2 getResult = vector2List.get(0);
    assertEquals(0.0f, getResult.y);
    assertEquals(0.0f, getResult.x);
  }

  @Test
  void testTransformVector2Ds3() {
    Matrix3 newInstanceResult = Matrix3.newInstance();

    ArrayList<Vector2> vector2List = new ArrayList<Vector2>();
    vector2List.add(Vector2.newInstance());
    vector2List.add(Vector2.newInstance());
    newInstanceResult.transformVector2Ds(vector2List);
    Vector2 getResult = vector2List.get(0);
    assertEquals(0.0f, getResult.y);
    Vector2 getResult1 = vector2List.get(1);
    assertEquals(0.0f, getResult1.y);
    assertEquals(0.0f, getResult1.x);
    assertEquals(0.0f, getResult.x);
  }

  @Test
  void testTransformVector2D() {
    Matrix3 newInstanceResult = Matrix3.newInstance();
    Vector2 newInstanceResult1 = Vector2.newInstance();
    newInstanceResult.transformVector2D(newInstanceResult1);
    assertEquals(0.0f, newInstanceResult1.y);
    assertEquals(0.0f, newInstanceResult1.x);
  }

  @Test
  void testTranslate() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by translate(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Matrix3.newInstance().translate(10.0f, 10.0f);
  }

  @Test
  void testScale() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by scale(float, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Matrix3.newInstance().scale(10.0f, 10.0f);
  }

  @Test
  void testRotate() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by rotate(float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Matrix3.newInstance().rotate(10.0f);
  }

  @Test
  void testRotate2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by rotate(Vector2, Vector2)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Matrix3 newInstanceResult = Matrix3.newInstance();
    Vector2 forward = Vector2.newInstance();
    newInstanceResult.rotate(forward, Vector2.newInstance());
  }
}

