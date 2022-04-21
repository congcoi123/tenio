package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.tenio.engine.physic2d.math.Vector2;
import org.junit.jupiter.api.Test;

class SmootherVectorTest {
  @Test
  void testConstructor() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     SmootherVector.histories
    //     SmootherVector.zeroValue
    //     SmootherVector.nextUpdateSlot

    new SmootherVector<Vector2>(3, Vector2.newInstance());

  }

  @Test
  void testUpdate() {
    Vector2 newInstanceResult = Vector2.newInstance();
    SmootherVector<Vector2> smootherVector = new SmootherVector<Vector2>(3, newInstanceResult);
    Vector2 actualUpdateResult = smootherVector.update(Vector2.newInstance());
    assertSame(newInstanceResult, actualUpdateResult);
    assertEquals(0.0f, actualUpdateResult.y);
    assertEquals(0.0f, actualUpdateResult.x);
  }

  @Test
  void testUpdate2() {
    Vector2 newInstanceResult = Vector2.newInstance();
    SmootherVector<Vector2> smootherVector = new SmootherVector<Vector2>(1, newInstanceResult);
    Vector2 actualUpdateResult = smootherVector.update(Vector2.newInstance());
    assertSame(newInstanceResult, actualUpdateResult);
    assertEquals(0.0f, actualUpdateResult.y);
    assertEquals(0.0f, actualUpdateResult.x);
  }
}

