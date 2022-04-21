package com.tenio.engine.physic2d.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.engine.physic2d.common.InvertedAabbBox2D;
import org.junit.jupiter.api.Test;

class CellTest {
  @Test
  void testConstructor() {
    Cell<Object> actualCell = new Cell<Object>(10.0f, 10.0f, 10.0f, 10.0f);

    assertTrue(actualCell.members.isEmpty());
    InvertedAabbBox2D invertedAabbBox2D = actualCell.bbox;
    assertEquals(10.0f, invertedAabbBox2D.getTop());
    assertEquals(10.0f, invertedAabbBox2D.getRight());
    assertEquals(10.0f, invertedAabbBox2D.getLeft());
    assertEquals(10.0f, invertedAabbBox2D.getBottom());
  }
}

