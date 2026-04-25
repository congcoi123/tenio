package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SmootherTest {
  @Test
  void testAdd() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by add(Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    Integer a = Integer.valueOf(1);
    smoother.<Number>add(a, Integer.valueOf(1));
  }

  @Test
  void testAdd2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by add(Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    Integer a = Integer.valueOf(0);
    smoother.<Number>add(a, Integer.valueOf(1));
  }

  @Test
  void testAdd3() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by add(Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    Integer a = Integer.valueOf(3);
    smoother.<Number>add(a, Integer.valueOf(1));
  }

  @Test
  void testAdd4() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by add(Number, Number)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    Integer a = Integer.valueOf(-1);
    smoother.<Number>add(a, Integer.valueOf(1));
  }

  @Test
  void testDiv() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by div(Number, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    smoother.<Number>div(Integer.valueOf(1), 10.0f);
  }

  @Test
  void testDiv2() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by div(Number, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    smoother.<Number>div(Integer.valueOf(0), 10.0f);
  }

  @Test
  void testDiv3() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by div(Number, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    smoother.<Number>div(Integer.valueOf(3), 10.0f);
  }

  @Test
  void testDiv4() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by div(Number, float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    Smoother<Number> smoother = new Smoother<Number>(3, Integer.valueOf(1));
    smoother.<Number>div(Integer.valueOf(-1), 10.0f);
  }

  @Test
  void testConstructor() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     Smoother.histories
    //     Smoother.zeroValue
    //     Smoother.nextUpdateSlot

    new Smoother<Number>(3, Integer.valueOf(1));

  }

  @Test
  void testUpdateReturnsSingleSampleAverage() {
    Smoother<Float> smoother = new Smoother<>(1, 0.0f);
    Float result = smoother.update(5.0f);
    assertEquals(5.0f, result, 0.001f);
  }

  @Test
  void testUpdateAveragesHistoryAfterWrapAround() {
    Smoother<Float> smoother = new Smoother<>(2, 0.0f);
    smoother.update(4.0f);
    smoother.update(8.0f);
    Float result = smoother.update(12.0f);
    assertEquals(10.0f, result, 0.001f);
  }
}

