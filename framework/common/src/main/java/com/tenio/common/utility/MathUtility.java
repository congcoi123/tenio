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

package com.tenio.common.utility;

import java.util.Random;

/**
 * A collection of utility methods for calculations.
 */
public final class MathUtility {

  /**
   * The maximum value of an integer number.
   */
  public static final int MAX_INT = Integer.MAX_VALUE;
  /**
   * The minimum value of an integer number.
   */
  public static final int MIN_INT = Integer.MIN_VALUE;
  /**
   * The maximum value of a float number.
   */
  public static final float MAX_FLOAT = Float.MAX_VALUE;
  /**
   * The minimum value of a float number.
   */
  public static final float MIN_FLOAT = Float.MIN_VALUE;
  /**
   * The <a href="https://en.wikipedia.org/wiki/Pi">Pi</a> number.
   */
  public static final float PI = (float) Math.PI;
  /**
   * The double value of <a href="https://en.wikipedia.org/wiki/Pi">Pi</a> number.
   */
  public static final float TWO_PI = PI * 2;
  /**
   * The half value of <a href="https://en.wikipedia.org/wiki/Pi">Pi</a> number.
   */
  public static final float HALF_PI = PI / 2;
  /**
   * The quarter value <a href="https://en.wikipedia.org/wiki/Pi">Pi</a> number.
   */
  public static final float QUARTER_PI = PI / 4;
  /**
   * The epsilon value in float.
   */
  public static final float EPSILON_FLOAT = Float.MIN_NORMAL;
  // ----------------------------------------------
  // some random number functions
  // ----------------------------------------------
  private static final Random random = new Random();
  private static float Y2 = 0;
  private static boolean USE_LAST = false;

  private MathUtility() {
    throw new UnsupportedOperationException("This class does not support to create a new instance");
  }

  /**
   * Determines if an object is not <code>null</code>.
   *
   * @param <T> a generic type
   * @param val the instance
   * @return <code>true</code> if the instance is not <code>null</code>,
   * otherwise <code>false</code>
   */
  public static <T> boolean isNaN(T val) {
    return val == null;
  }

  /**
   * Converts a value from degree to radian unit.
   *
   * @param degree the value in degree unit
   * @return a converted value in radian unit
   */
  public static float degreeToRadian(float degree) {
    return TWO_PI * (degree / 360);
  }

  /**
   * Compares two real numbers.
   *
   * @param a a value
   * @param b b value
   * @return Returns <b>true</b> if they are equal
   */
  public static boolean isEqual(float a, float b) {
    return Math.abs(a - b) < 1E-12;
  }

  /**
   * Retrieves the greater value of two numbers.
   *
   * @param <T> comparable template
   * @param a   a value
   * @param b   b value
   * @return the maximum of two values
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T extends Comparable> T maxOf(T a, T b) {
    if (a.compareTo(b) > 0) {
      return a;
    }
    return b;
  }

  /**
   * Retrieves the lesser value of two number.
   *
   * @param <T> comparable template
   * @param a   a value
   * @param b   b value
   * @return the minimum of two values
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T extends Comparable> T minOf(T a, T b) {
    if (a.compareTo(b) < 0) {
      return a;
    }
    return b;
  }

  /**
   * Initializes the seed.
   *
   * @param seed seed value
   */
  public static void setSeed(long seed) {
    random.setSeed(seed);
  }

  /**
   * Retrieves a random integer number.
   *
   * @param x from value
   * @param y to value
   * @return a random integer between x and y (included x and y)
   */
  public static int randInt(int x, int y) {
    return x + (int) (Math.random() * ((y - x) + 1));
  }

  /**
   * Retrieves a random float number.
   *
   * @return a random float between <b>zero</b> and <b>1</b>
   */
  public static float randFloat() {
    return random.nextFloat();
  }

  /**
   * Retrieves a random float number in range.
   *
   * @param x x value
   * @param y y value
   * @return a random float between x and y
   */
  public static float randInRange(float x, float y) {
    return x + randFloat() * (y - x);
  }

  /**
   * Retrieves a random boolean value.
   *
   * @return a random boolean
   */
  public static boolean randBool() {
    return randFloat() > 0.5f;
  }

  /**
   * Retrieves a random clamped value.
   *
   * @return a random float in the range <b>[-1, 1]</b>
   */
  public static float randomClamped() {
    return randFloat() - randFloat();
  }

  /**
   * Retrieves a random gaussian value.
   * More details: <a href="http://www.taygeta.com/random/gaussian.html">here</a>
   *
   * @return a random number with a normal distribution.
   */
  public static float randGaussian() {
    return randGaussian(0, 1);
  }

  /**
   * Retrieves a random gaussian value.
   * More details: <a href="http://www.taygeta.com/random/gaussian.html">here</a>
   *
   * @param mean              mean value
   * @param standardDeviation deviation value
   * @return a random number with set distribution.
   */
  public static float randGaussian(float mean, float standardDeviation) {
    float x1;
    float x2;
    float w;
    float y1;

    // use value from previous call
    if (USE_LAST) {
      y1 = Y2;
      USE_LAST = false;
    } else {
      do {
        x1 = 2 * randFloat() - 1;
        x2 = 2 * randFloat() - 1;
        w = x1 * x1 + x2 * x2;
      } while (w >= 1.0);

      w = (float) Math.sqrt((-2 * Math.log(w)) / w);
      y1 = x1 * w;
      Y2 = x2 * w;
      USE_LAST = true;
    }

    return (mean + y1 * standardDeviation);
  }

  /**
   * Clamp method, have no idea.
   *
   * @param arg    arg value
   * @param minVal minimum value
   * @param maxVal maximum value
   * @param <T>    class type
   * @return a number
   */
  public static <T extends Number> T clamp(T arg, T minVal, T maxVal) {
    if (arg.doubleValue() < minVal.doubleValue()) {
      return minVal;
    }

    if (arg.doubleValue() > maxVal.doubleValue()) {
      return maxVal;
    }
    return arg;
  }
}
