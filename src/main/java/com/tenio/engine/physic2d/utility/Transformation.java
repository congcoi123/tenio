package com.tenio.engine.physic2d.utility;

import com.tenio.engine.physic2d.math.Matrix3;
import com.tenio.engine.physic2d.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Some functions for converting 2D vectors between World and Local space.
 */
public final class Transformation {

  /**
   * Given a list of 2D vectors (points), a position, orientation and scale, this function
   * transforms the 2D vectors into the object's world space.
   *
   * @param points   a list of 2D vectors
   * @param position the position
   * @param forward  the forward vector
   * @param side     the side vector
   * @param scale    the scale value
   * @return a list of vectors in world space
   */
  public static List<Vector2> pointsToWorldSpace(List<Vector2> points, Vector2 position,
                                                 Vector2 forward,
                                                 Vector2 side, Vector2 scale) {
    // copy the original vertices into the buffer about to be transformed
    final var tranVector2Ds = clone(points);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // scale
    if ((scale.x != 1) || (scale.y != 1)) {
      matrix.scale(scale.x, scale.y);
    }

    // rotate
    matrix.rotate(forward, side);

    // and translate
    matrix.translate(position.x, position.y);

    // now transform the object's vertices
    matrix.transformVector2Ds(tranVector2Ds);

    return tranVector2Ds;
  }

  /**
   * Given a list of 2D vectors (points), a position, orientation, this function transforms the
   * 2D vectors into the object's world space.
   *
   * @param points   a list of 2D vectors
   * @param position the position
   * @param forward  the forward vector
   * @param side     the side vector
   * @return a list of vectors in world space
   */
  public static List<Vector2> pointsToWorldSpace(List<Vector2> points, Vector2 position,
                                                 Vector2 forward,
                                                 Vector2 side) {
    // copy the original vertices into the buffer about to be transformed
    var tranVector2Ds = clone(points);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // rotate
    matrix.rotate(forward, side);

    // and translate
    matrix.translate(position.x, position.y);

    // now transform the object's vertices
    matrix.transformVector2Ds(tranVector2Ds);

    return tranVector2Ds;
  }

  /**
   * Transforms a point from the agent's local space into world space.
   *
   * @param point         the source point
   * @param agentHeading  the agent heading vector
   * @param agentSide     the agent side vector
   * @param agentPosition the agent position
   * @return the new vector in the world space
   */
  public static Vector2 pointToWorldSpace(Vector2 point, Vector2 agentHeading, Vector2 agentSide,
                                          Vector2 agentPosition) {
    // make a copy of the point
    var temp = Vector2.newInstance().set(point);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // rotate
    matrix.rotate(agentHeading, agentSide);

    // and translate
    matrix.translate(agentPosition.x, agentPosition.y);

    // now transform the vertices
    matrix.transformVector2D(temp);

    return temp;
  }

  /**
   * Transforms a vector from the agent's local space into world space.
   *
   * @param vector       the source point
   * @param agentHeading the agent heading vector
   * @param agentSide    the agent side vector
   * @return the new vector in the world space
   */
  public static Vector2 vectorToWorldSpace(Vector2 vector, Vector2 agentHeading,
                                           Vector2 agentSide) {
    // make a copy of the point
    var temp = Vector2.newInstance().set(vector);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // rotate
    matrix.rotate(agentHeading, agentSide);

    // now transform the vertices
    matrix.transformVector2D(temp);

    return temp.clone();
  }

  /**
   * Transforms a point from the world space into agent local space.
   *
   * @param point         the source point
   * @param agentHeading  the agent heading vector
   * @param agentSide     the agent side vector
   * @param agentPosition the agent position
   * @return the new vector in the local space
   */
  public static Vector2 pointToLocalSpace(Vector2 point, Vector2 agentHeading, Vector2 agentSide,
                                          Vector2 agentPosition) {
    // make a copy of the point
    var temp = Vector2.newInstance().set(point);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    float tx = -agentPosition.getDotProductValue(agentHeading);
    float ty = -agentPosition.getDotProductValue(agentSide);

    // create the transformation matrix
    matrix.p11(agentHeading.x);
    matrix.p12(agentSide.x);
    matrix.p21(agentHeading.y);
    matrix.p22(agentSide.y);
    matrix.p31(tx);
    matrix.p32(ty);

    // now transform the vertices
    matrix.transformVector2D(temp);

    return temp;
  }

  /**
   * Transforms a point from the world space into agent local space.
   *
   * @param vector       the source point
   * @param agentHeading the agent heading vector
   * @param agentSide    the agent side vector
   * @return the new vector in the local space
   */
  public static Vector2 vectorToLocalSpace(Vector2 vector, Vector2 agentHeading,
                                           Vector2 agentSide) {
    // make a copy of the point
    var temp = Vector2.newInstance().set(vector);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // create the transformation matrix
    matrix.p11(agentHeading.x);
    matrix.p12(agentSide.x);
    matrix.p21(agentHeading.y);
    matrix.p22(agentSide.y);

    // now transform the vertices
    matrix.transformVector2D(temp);

    return temp;
  }

  /**
   * Rotates a vector angle radians around the origin.
   *
   * @param vector the vector
   * @param angle  the angle
   * @return a vector in new angle
   */
  public static Vector2 vec2dRotateAroundOrigin(Vector2 vector, float angle) {
    // make a copy of the point
    var temp = Vector2.newInstance().set(vector);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // rotate
    matrix.rotate(angle);

    // now transform the object's vertices
    matrix.transformVector2D(temp);

    return temp;
  }

  /**
   * Rotates a vector angle radians around the origin.
   *
   * @param x     the vector x
   * @param y     the vector y
   * @param angle the angle
   * @return a vector in new angle
   */
  public static Vector2 vec2dRotateAroundOrigin(float x, float y, float angle) {
    // make a copy of the point
    var temp = Vector2.newInstance().set(x, y);

    // create a transformation matrix
    var matrix = Matrix3.newInstance();

    // rotate
    matrix.rotate(angle);

    // now transform the object's vertices
    matrix.transformVector2D(temp);

    return temp;
  }

  /**
   * Given an origin, a facing direction, a 'field of view' describing the limit of the outer
   * whiskers, a whisker length and the number of whiskers this method returns a vector
   * containing the end positions of a series of whiskers radiating away from the origin and with
   * equal distance between them. (like the spokes of a wheel clipped to a specific segment size).
   *
   * @param numWhiskers   the number of whiskers
   * @param whiskerLength the whisker length
   * @param fov           the fov
   * @param facing        the facing
   * @param origin        the origin
   * @return a list of vector
   */
  public static List<Vector2> createWhiskers(int numWhiskers, float whiskerLength, float fov,
                                             Vector2 facing,
                                             Vector2 origin) {
    // this is the magnitude of the angle separating each whisker
    float sectorSize = fov / (float) (numWhiskers - 1);

    var whiskers = new ArrayList<Vector2>(numWhiskers);
    float angle = -fov * 0.5f;

    for (int w = 0; w < numWhiskers; ++w) {
      // create the whisker extending outwards at this angle
      var temp2 = Vector2.newInstance().set(facing);
      var temp = vec2dRotateAroundOrigin(temp2, angle);
      var temp1 = Vector2.newInstance().set(temp).mul(whiskerLength).add(origin);
      whiskers.add(temp1);

      angle += sectorSize;
    }

    return whiskers;
  }

  /**
   * Wrap around a vector.
   *
   * @param position the position
   * @param maxX     max x
   * @param maxY     max y
   * @return a new vector
   */
  public static Vector2 wrapAround(Vector2 position, int maxX, int maxY) {

    boolean clone = false;

    if (position.x > maxX) {
      position.x = 0;
      clone = true;
    }

    if (position.x < 0) {
      position.x = maxX;
      clone = true;
    }

    if (position.y < 0) {
      position.y = maxY;
      clone = true;
    }

    if (position.y > maxY) {
      position.y = 0;
      clone = true;
    }

    if (clone) {
      return Vector2.newInstance().set(position);
    }

    return position;
  }

  private static List<Vector2> clone(List<Vector2> list) {
    return list.stream().map(e -> e.clone()).collect(Collectors.toList());
  }
}
