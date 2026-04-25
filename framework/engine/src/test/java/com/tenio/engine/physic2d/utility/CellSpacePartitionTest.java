package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.math.Vector2;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

  @Test
  void testCalculateNeighborsWithEntityInRange() {
    CellSpacePartition<BaseGameEntity> csp = new CellSpacePartition<>(100.0f, 100.0f, 1, 1, 3);
    BaseGameEntity entity = mock(BaseGameEntity.class);
    var pos = Vector2.newInstance();
    pos.set(5.0f, 5.0f);
    when(entity.getPosition()).thenReturn(pos);
    csp.addEntity(entity);
    csp.calculateNeighbors(pos, 50.0f);
    assertNotNull(csp.getFrontOfNeighbor());
    assertNull(csp.getNextOfNeighbor());
    assertTrue(csp.isEndOfNeighbors());
  }

  @Test
  void testGetNextOfNeighborWithTwoInRangeAndOneOut() {
    CellSpacePartition<BaseGameEntity> csp = new CellSpacePartition<>(100.0f, 100.0f, 1, 1, 3);

    BaseGameEntity e1 = mock(BaseGameEntity.class);
    var p1 = Vector2.newInstance();
    p1.set(5.0f, 5.0f);
    when(e1.getPosition()).thenReturn(p1);

    BaseGameEntity e2 = mock(BaseGameEntity.class);
    var p2 = Vector2.newInstance();
    p2.set(3.0f, 3.0f);
    when(e2.getPosition()).thenReturn(p2);

    BaseGameEntity e3 = mock(BaseGameEntity.class);
    var p3 = Vector2.newInstance();
    p3.set(80.0f, 80.0f);
    when(e3.getPosition()).thenReturn(p3);

    csp.addEntity(e1);
    csp.addEntity(e2);
    csp.addEntity(e3);

    var target = Vector2.newInstance();
    target.set(5.0f, 5.0f);
    csp.calculateNeighbors(target, 10.0f);

    assertNotNull(csp.getFrontOfNeighbor());
    assertFalse(csp.isEndOfNeighbors());
    assertNotNull(csp.getNextOfNeighbor());
    assertTrue(csp.isEndOfNeighbors());
  }

  @Test
  void testRenderWithRealPaint() {
    CellSpacePartition<BaseGameEntity> csp = new CellSpacePartition<>(100.0f, 100.0f, 1, 1, 3);
    Paint paint = Paint.getInstance();
    Graphics2D g = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB).createGraphics();
    paint.startDrawing(g);
    assertDoesNotThrow(() -> csp.render(paint));
  }
}

