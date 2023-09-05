/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.tenio.engine.physic2d.common;

import com.tenio.common.utility.MathUtility;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utility.Transformation;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is used to define, manage, and traverse a path (defined by a
 * series of 2D vectors).
 */
public final class Path implements Renderable {

  /**
   * The list of way points.
   */
  private List<Vector2> wayPoints;
  // points to the current way-points
  private ListIterator<Vector2> currWayPoints;
  private Vector2 currWayPoint;
  /**
   * This flag is used to indicate if the path should be looped (the last
   * way-points connected to the first).
   */
  private boolean looped;

  public Path() {
    wayPoints = new LinkedList<>();
    looped = false;
  }

  /**
   * This constructor for creating a path with initial random way-points.
   *
   * @param numWaypoints the number of waypoints
   * @param minX         min x
   * @param minY         min y
   * @param maxX         max x
   * @param maxY         max y
   * @param looped       is accepted loop or not?
   */
  public Path(int numWaypoints, float minX, float minY, float maxX, float maxY, boolean looped) {
    this();
    this.looped = looped;
    createRandomPath(numWaypoints, minX, minY, maxX, maxY);
  }

  public Vector2 getCurrentWayPoint() {
    return currWayPoint;
  }

  public boolean isEndOfWayPoints() {
    return !(currWayPoints.hasNext());
  }

  /**
   * Moves the iterator on to the next way-point in the list.
   */
  public void setToNextWayPoint() {
    if (wayPoints.isEmpty()) {
      return;
    }

    if (!currWayPoints.hasNext()) {
      if (looped) {
        currWayPoints = wayPoints.listIterator();
      }
    }
    if (currWayPoints.hasNext()) {
      currWayPoint = currWayPoints.next();
    }
  }

  /**
   * Create a new random path.
   *
   * @param numWaypoints the number of waypoints
   * @param minX         min x
   * @param minY         min y
   * @param maxX         max x
   * @param maxY         max y
   * @return a list of vector2
   */
  public List<Vector2> createRandomPath(int numWaypoints, float minX, float minY, float maxX,
                                        float maxY) {
    wayPoints.clear();

    float midX = (maxX + minX) / 2;
    float midY = (maxY + minY) / 2;

    float smaller = MathUtility.minOf(midX, midY);

    float spacing = MathUtility.TWO_PI / numWaypoints;

    for (int i = 0; i < numWaypoints; ++i) {
      float radialDist = MathUtility.randInRange(smaller * 0.2f, smaller);

      var temp = Transformation.vec2dRotateAroundOrigin(radialDist, 0, i * spacing);

      temp.x += midX;
      temp.y += midY;

      wayPoints.add(temp);
    }

    currWayPoints = wayPoints.listIterator();
    if (currWayPoints.hasNext()) {
      currWayPoint = currWayPoints.next();
    }

    return wayPoints;
  }

  public void enableLoop(boolean enabled) {
    looped = enabled;
  }

  public void setPath(Path path) {
    setWayPoints(path.getWayPoints());
  }

  public void clear() {
    wayPoints.clear();
  }

  public List<Vector2> getWayPoints() {
    return wayPoints;
  }

  /**
   * Adds a way-point to the end of the path methods for setting the path with
   * either another path or a list of vectors.
   *
   * @param wayPoints list of way-points
   */
  public void setWayPoints(List<Vector2> wayPoints) {
    this.wayPoints = wayPoints;
    currWayPoints = this.wayPoints.listIterator();
    currWayPoint = currWayPoints.next();
  }

  @Override
  public void render(Paint paint) {
    paint.setPenColor(Color.ORANGE);

    var it = wayPoints.listIterator();

    var wp = it.next();

    while (it.hasNext()) {
      var n = it.next();
      paint.drawLine(wp, n);

      wp = n;
    }

    if (looped) {
      paint.drawLine(wp, wayPoints.get(0));
    }
  }
}
