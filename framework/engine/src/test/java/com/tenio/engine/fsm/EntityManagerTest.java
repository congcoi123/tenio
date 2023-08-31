package com.tenio.engine.fsm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.engine.fsm.entity.AbstractEntity;
import org.junit.jupiter.api.Test;

class EntityManagerTest {
  @Test
  void testRegister() {
    EntityManager entityManager = new EntityManager();
    AbstractEntity abstractEntity = mock(AbstractEntity.class);
    when(abstractEntity.getId()).thenReturn("42");
    entityManager.register(abstractEntity);
    verify(abstractEntity, atLeast(1)).getId();
  }

  @Test
  void testContain() {
    assertFalse((new EntityManager()).contain("42"));
  }

  @Test
  void testCount() {
    assertEquals(0L, (new EntityManager()).count());
  }

  @Test
  void testGet() {
    assertNull((new EntityManager()).get("42"));
  }

  @Test
  void testUpdate() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by update(float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new EntityManager()).update(0.5f);
  }

  @Test
  void testGets() {
    assertTrue((new EntityManager()).gets().isEmpty());
  }

  @Test
  void testRemove() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by remove(String)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new EntityManager()).remove("42");
  }

  @Test
  void testClear() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     AbstractLogger.logger
    //     EntityManager.entities

    (new EntityManager()).clear();
  }
}

