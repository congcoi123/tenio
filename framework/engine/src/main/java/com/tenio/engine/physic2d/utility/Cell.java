package com.tenio.engine.physic2d.utility;

import com.tenio.engine.physic2d.common.InvertedAabbBox2D;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to define a cell containing a list of pointers to
 * entities.
 */
class Cell<T extends Object> {
  /**
   * All the entities inhabiting this cell.
   */
  public List<T> members = new LinkedList<T>();
  /**
   * The cell's bounding box (it's inverted because the Window's default
   * co-ordinate system has a y axis that increases as it descends).
   */
  public InvertedAabbBox2D bbox;

  public Cell(float left, float top, float right, float bottom) {
    bbox = InvertedAabbBox2D.valueOf(left, top, right, bottom);
  }
}
