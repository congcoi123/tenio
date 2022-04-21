package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntitiesRelationshipTest {
  @Test
  void testEnforceNonPenetrationConstraint() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by enforceNonPenetrationConstraint(BaseGameEntity, List)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    BaseGameEntity entity = mock(BaseGameEntity.class);
    EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>enforceNonPenetrationConstraint(
        entity,
        new ArrayList<BaseGameEntity>());
  }

  @Test
  void testEnforceNonPenetrationConstraint2() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    doNothing().when(baseGameEntity).setPosition((Vector2) any());
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());
    BaseGameEntity baseGameEntity1 = mock(BaseGameEntity.class);
    when(baseGameEntity1.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity1.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity1);
    EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>enforceNonPenetrationConstraint(
        baseGameEntity,
        baseGameEntityList);
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity, atLeast(1)).getPosition();
    verify(baseGameEntity).setPosition((Vector2) any());
    verify(baseGameEntity1).getBoundingRadius();
    verify(baseGameEntity1).getPosition();
  }

  @Test
  void testEnforceNonPenetrationConstraint3() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    doNothing().when(baseGameEntity).setPosition((Vector2) any());
    when(baseGameEntity.getBoundingRadius()).thenReturn(Float.NaN);
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());
    BaseGameEntity baseGameEntity1 = mock(BaseGameEntity.class);
    when(baseGameEntity1.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity1.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity1);
    EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>enforceNonPenetrationConstraint(
        baseGameEntity,
        baseGameEntityList);
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getPosition();
    verify(baseGameEntity1).getBoundingRadius();
    verify(baseGameEntity1).getPosition();
  }

  @Test
  void testGetEntityLineSegmentIntersections() {
    ArrayList<BaseGameEntity> entities = new ArrayList<BaseGameEntity>();
    Vector2 vectorA = Vector2.newInstance();
    assertTrue(EntitiesRelationship
        .<BaseGameEntity, List<BaseGameEntity>>getEntityLineSegmentIntersections(entities,
            "The One To Ignore", vectorA,
            Vector2.newInstance())
        .isEmpty());
  }

  @Test
  void testGetEntityLineSegmentIntersections2() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertEquals(1,
        EntitiesRelationship
            .<BaseGameEntity, List<BaseGameEntity>>getEntityLineSegmentIntersections(
                baseGameEntityList,
                "The One To Ignore", vectorA, Vector2.newInstance())
            .size());
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetEntityLineSegmentIntersections3() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(0.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertTrue(
        EntitiesRelationship
            .<BaseGameEntity, List<BaseGameEntity>>getEntityLineSegmentIntersections(
                baseGameEntityList,
                "The One To Ignore", vectorA, Vector2.newInstance())
            .isEmpty());
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetEntityLineSegmentIntersections4() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("The One To Ignore");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertTrue(
        EntitiesRelationship
            .<BaseGameEntity, List<BaseGameEntity>>getEntityLineSegmentIntersections(
                baseGameEntityList,
                "The One To Ignore", vectorA, Vector2.newInstance())
            .isEmpty());
    verify(baseGameEntity).getId();
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testGetEntityLineSegmentIntersections5() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    assertEquals(1,
        EntitiesRelationship
            .<BaseGameEntity, List<BaseGameEntity>>getEntityLineSegmentIntersections(
                baseGameEntityList,
                "The One To Ignore", newInstanceResult, Vector2.newInstance())
            .size());
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetEntityLineSegmentIntersections6() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    assertTrue(
        EntitiesRelationship
            .<BaseGameEntity, List<BaseGameEntity>>getEntityLineSegmentIntersections(
                baseGameEntityList,
                "The One To Ignore", newInstanceResult, Vector2.newInstance())
            .isEmpty());
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection() {
    ArrayList<BaseGameEntity> entities = new ArrayList<BaseGameEntity>();
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            entities, "The One To Ignore", vectorA, Vector2.newInstance()));
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection2() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
        baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance());
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection3() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(0.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance()));
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection4() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("The One To Ignore");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance()));
    verify(baseGameEntity).getId();
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection5() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", newInstanceResult, Vector2.newInstance()));
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection6() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", newInstanceResult, Vector2.newInstance()));
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection7() {
    ArrayList<BaseGameEntity> entities = new ArrayList<BaseGameEntity>();
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            entities, "The One To Ignore", vectorA, Vector2.newInstance(), 10.0f));
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection8() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
        baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance(), 10.0f);
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection9() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(0.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance(), 10.0f));
    verify(baseGameEntity).getBoundingRadius();
    verify(baseGameEntity).getId();
    verify(baseGameEntity, atLeast(1)).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection10() {
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("The One To Ignore");
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance(), 10.0f));
    verify(baseGameEntity).getId();
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testGetClosestEntityLineSegmentIntersection11() {
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(Float.MAX_VALUE, Float.MAX_VALUE);
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getBoundingRadius()).thenReturn(10.0f);
    when(baseGameEntity.getId()).thenReturn("42");
    when(baseGameEntity.getPosition()).thenReturn(newInstanceResult);

    ArrayList<BaseGameEntity> baseGameEntityList = new ArrayList<BaseGameEntity>();
    baseGameEntityList.add(baseGameEntity);
    Vector2 vectorA = Vector2.newInstance();
    assertNull(
        EntitiesRelationship.<BaseGameEntity, java.util.List<BaseGameEntity>>getClosestEntityLineSegmentIntersection(
            baseGameEntityList, "The One To Ignore", vectorA, Vector2.newInstance(), 10.0f));
    verify(baseGameEntity).getId();
    verify(baseGameEntity).getPosition();
  }
}

