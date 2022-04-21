/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.engine.physic2d.math;

import com.tenio.common.utility.MathUtility;

/**
 * A 2D Vector. Allows chaining methods by returning a reference to itself.
 */
public class Vector2 {

  public static final int CLOCK_WISE = 1;
  public static final int ANTI_CLOCK_WISE = -1;

  public float x;
  public float y;

  private Vector2() {
    zero();
  }

  public static Vector2 newInstance() {
    return new Vector2();
  }

  public static Vector2 valueOf(float a, float b) {
    return newInstance().set(a, b);
  }

  public static Vector2 valueOf(Vector2 vector) {
    return newInstance().set(vector);
  }

  public Vector2 set(Vector2 vector) {
    return set(vector.x, vector.y);
  }

  public Vector2 set(float a, float b) {
    x = a;
    y = b;

    return this;
  }

  @Override
  public Vector2 clone() {
    return valueOf(this);
  }

  public Vector2 zero() {
    return set(0, 0);
  }

  // returns true if both x and y are zero
  public boolean isZero() {
    return x == 0 && y == 0;
  }

  /**
   * @return the length of a 2D vector
   */
  public float getLength() {
    return (float) Math.sqrt(getLengthSqr());
  }

  /**
   * @return the squared length of the vector (thereby avoiding the sqrt)
   */
  public float getLengthSqr() {
    return (x * x + y * y);
  }

  /**
   * <b>Normalize a 2D Vector</b> <br>
   * Detail <a href=
   * "https://www.khanacademy.org/computing/computer-programming/programming-natural-simulations/programming-vectors/a/vector-magnitude-normalization#targetText=To%20normalize%20a%20vector%2C%20therefore,is%20called%20a%20unit%20vector">normalize</a>.
   * Vector and how to use <a href=
   * "https://www.stdio.vn/articles/vector-va-ung-dung-cua-chung-45">vector</a>
   * <br>
   * <p>
   * Normalizing refers to the process of making something “standard” or, well,
   * “normal.” In the case of vectors, let’s assume for the moment that a standard
   * vector has a length of 1. To normalize a vector, therefore, is to take a
   * vector of any length and, keeping it pointing in the same direction, change
   * its length to 1, turning it into what is called a unit vector.
   * </p>
   *
   * @return a new normalized vector, see {@link Vector2}
   */
  public Vector2 normalize() {
    float length = getLength();

    if (length != 0) {
      x /= length;
      y /= length;
    }

    return this;
  }

  /**
   * <b>Calculates the dot product</b> <br>
   * Since the only way a negative number can be introduced to this equation is
   * the cosine function, the result of the dot product is negative if and only if
   * the vectors point in a direction greater than pi/2 radians (90 degrees) apart
   * from one another. <br>
   * <i>+ The simple take-away: negative dot product means the vectors point in
   * different directions</i> <br>
   * <i>+ If the dot product is zero the two vectors are orthogonal
   * (perpendicular)</i> <br>
   * <i>+ If the vectors are unit length and the result of the dot product is 1,
   * the vectors are equal</i> <br>
   * <p>
   * How to use <a href=
   * "https://hackernoon.com/applications-of-the-vector-dot-product-for-game-programming-12443ac91f16#targetText=Typically%20you'll%20see%20the,angle%20between%20the%20two%20vectors">dot
   * product</a> <br>
   * What is <a href="https://minhng.info/toan-hoc/y-nghia-tich-vo-huong.html">dot
   * product</a> <br>
   * <br>
   *
   * @param vector see {@link Vector2}
   * @return dot product value
   */
  public float getDotProductValue(Vector2 vector) {
    return x * vector.x + y * vector.y;
  }

  /**
   * <b>Get sign value between 2 vectors</b> <br>
   * How to compute <a href=
   * "https://stackoverflow.com/questions/14066933/direct-way-of-computing-clockwise-angle-between-2-vectors">sign
   * value</a>
   * <p>
   * dot = x1*x2 + y1*y2 # dot product between [x1, y1] and [x2, y2] <br>
   * det = x1*y2 - y1*x2 # determinant <br>
   * angle = atan2(det, dot) # atan2(y, x) or atan2(sin, cos)
   * </p>
   * <p>
   * The orientation of this angle matches that of the coordinate system. In a
   * left-handed coordinate system, i.e. x pointing right and y down as is common
   * for computer graphics, this will mean you get a positive sign for clockwise
   * angles. If the orientation of the coordinate system is mathematical with y
   * up, you get counter-clockwise angles as is the convention in mathematics.
   * Changing the order of the inputs will change the sign, so if you are unhappy
   * with the signs just swap the inputs.
   * </p>
   * <br>
   *
   * @param vector see {@link Vector2}
   * @return positive if v2 is clockwise of this vector, negative if
   * anti-clockwise (assuming the Y axis is POINTING DOWN, X axis to RIGHT
   * in Graphic System)
   */
  public int getSignValue(Vector2 vector) {
    if (y * vector.x > x * vector.y) {
      return ANTI_CLOCK_WISE;
    } else {
      return CLOCK_WISE;
    }
  }

  /**
   * @return the vector ({@link Vector2}) that is perpendicular to this one. At an
   * angle of 90° to a given line, plane, or surface or to the ground.
   */
  public Vector2 perpendicular() {
    // swap
    float temp = x;
    x = -y;
    y = temp;

    return this;
  }

  /**
   * Adjusts x and y so that the length of the vector does not exceed max
   * truncates a vector so that its length does not exceed max
   *
   * @param max the max value
   * @return a new truncated vector, see {@link Vector2}
   */
  public Vector2 truncate(float max) {
    if (getLength() > max) {
      normalize().mul(max);
    }

    return this;
  }

  /**
   * Calculates the Euclidean distance between two vectors
   *
   * @param vector see {@link Vector2}
   * @return the distance between this vector and the one passed as a parameter
   */
  public float getDistanceValue(Vector2 vector) {
    return (float) Math.sqrt(getDistanceSqrValue(vector));
  }

  /**
   * Squared version of distance: Calculates the Euclidean distance squared
   * between two vectors
   *
   * @param vector see {@link Vector2}
   * @return the distance sqr between this vector and the one passed as a
   * parameter
   */
  public float getDistanceSqrValue(Vector2 vector) {
    float ySeparation = vector.y - y;
    float xSeparation = vector.x - x;

    return ySeparation * ySeparation + xSeparation * xSeparation;
  }

  /**
   * @return the new vector that is the reverse of this vector, see
   * {@link Vector2}
   */
  public Vector2 reverse() {
    x *= -1;
    y *= -1;

    return this;
  }

  // ----------------------- Overloaded Operators -----------------------
  // --------------------------------------------------------------------
  public Vector2 add(float a, float b) {
    x += a;
    y += b;

    return this;
  }

  public Vector2 add(Vector2 rhs) {
    x += rhs.x;
    y += rhs.y;

    return this;
  }

  public Vector2 sub(float a, float b) {
    x -= a;
    y -= b;

    return this;
  }

  public Vector2 sub(Vector2 rhs) {
    x -= rhs.x;
    y -= rhs.y;

    return this;
  }

  public Vector2 mul(float rhs) {
    x *= rhs;
    y *= rhs;

    return this;
  }

  public Vector2 div(float rhs) {
    x /= rhs;
    y /= rhs;

    return this;
  }

  public boolean isEqual(Vector2 vector) {
    return (MathUtility.isEqual(x, vector.x) && MathUtility.isEqual(y, vector.y));
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
