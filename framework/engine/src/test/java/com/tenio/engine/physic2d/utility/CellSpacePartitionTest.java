package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.math.Vector2;
import org.junit.jupiter.api.Test;

class CellSpacePartitionTest {
  @Test
  void testAddEntity() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1,
            3);
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());
    cellSpacePartition.addEntity(baseGameEntity);
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testUpdateEntity() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1,
            3);
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());
    cellSpacePartition.updateEntity(baseGameEntity, Vector2.newInstance());
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testUpdateEntity2() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 0, 1,
            3);
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getPosition()).thenReturn(Vector2.newInstance());
    cellSpacePartition.updateEntity(baseGameEntity, Vector2.newInstance());
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testUpdateEntity3() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 4, 1,
            3);
    Vector2 newInstanceResult = Vector2.newInstance();
    newInstanceResult.add(10.0f, 10.0f);
    BaseGameEntity baseGameEntity = mock(BaseGameEntity.class);
    when(baseGameEntity.getPosition()).thenReturn(newInstanceResult);
    cellSpacePartition.updateEntity(baseGameEntity, Vector2.newInstance());
    verify(baseGameEntity).getPosition();
  }

  @Test
  void testCalculateNeighbors() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1,
            3);
    cellSpacePartition.calculateNeighbors(Vector2.newInstance(), 10.0f);
    assertNull(cellSpacePartition.getFrontOfNeighbor());
  }

  @Test
  void testCalculateNeighbors2() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1,
            3);
    cellSpacePartition.calculateNeighbors(Vector2.newInstance(), -10.0f);
    assertNull(cellSpacePartition.getFrontOfNeighbor());
  }

  @Test
  void testCalculateNeighbors3() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 0.0f, 1, 1,
            3);
    cellSpacePartition.calculateNeighbors(Vector2.newInstance(), -10.0f);
    assertNull(cellSpacePartition.getFrontOfNeighbor());
  }

  @Test
  void testCalculateNeighbors4() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, Float.NaN, 1,
            1, 3);
    cellSpacePartition.calculateNeighbors(Vector2.newInstance(), -10.0f);
    assertNull(cellSpacePartition.getFrontOfNeighbor());
  }

  @Test
  void testCalculateNeighbors5() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(0.0f, Float.NaN, 1,
            1, 3);
    cellSpacePartition.calculateNeighbors(Vector2.newInstance(), -10.0f);
    assertNull(cellSpacePartition.getFrontOfNeighbor());
  }

  @Test
  void testGetFrontOfNeighbor() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1,
            3);
    assertNull(cellSpacePartition.getFrontOfNeighbor());
    assertNull(cellSpacePartition.getNextOfNeighbor());
  }

  @Test
  void testGetNextOfNeighbor() {
    assertNull((new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1, 3)).getNextOfNeighbor());
  }

  @Test
  void testIsEndOfNeighbors() {
    assertTrue((new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1, 3)).isEndOfNeighbors());
  }

  @Test
  void testClearCells() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by clearCells()
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1, 3)).clearCells();
  }

  @Test
  void testRender() {
    CellSpacePartition<BaseGameEntity> cellSpacePartition =
        new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 0, 1,
            3);
    cellSpacePartition.render(null);
    assertTrue(cellSpacePartition.isEndOfNeighbors());
  }

  @Test
  void testConstructor() {
    assertNull(
        (new CellSpacePartition<BaseGameEntity>(10.0f, 10.0f, 1, 1, 3)).getFrontOfNeighbor());
  }
}

