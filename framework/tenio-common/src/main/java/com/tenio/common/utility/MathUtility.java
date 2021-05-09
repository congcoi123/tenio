/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
 * 
 * @author kong
 */
public final class MathUtility {

	public static final int MAX_INT = Integer.MAX_VALUE;
	public static final int MIN_INT = Integer.MIN_VALUE;
	public static final float MAX_FLOAT = Float.MAX_VALUE;
	public static final float MIN_FLOAT = Float.MIN_VALUE;
	public static final float PI = (float) Math.PI;
	public static final float TWO_PI = PI * 2;
	public static final float HALF_PI = PI / 2;
	public static final float QUARTER_PI = PI / 4;
	public static final float EPSILON_FLOAT = Float.MIN_NORMAL;
	
	private MathUtility() {
		
	}

	/**
	 * @param <T> the number template
	 * @param val the number value
	 * @return <b>true</b> if the value is a <b>NaN</b>
	 */
	public static <T> boolean isNaN(T val) {
		return !(val != null);
	}

	public static float degsToRads(float degs) {
		return TWO_PI * (degs / 360);
	}

	/**
	 * Compares two real numbers
	 * 
	 * @param a a value
	 * @param b b value
	 * @return Returns <b>true</b> if they are equal
	 */
	public static boolean isEqual(float a, float b) {
		if (Math.abs(a - b) < 1E-12) {
			return true;
		}

		return false;
	}

	/**
	 * @param <T> comparable template
	 * @param a   a value
	 * @param b   b value
	 * @return the maximum of two values
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Comparable> T maxOf(T a, T b) {
		if (a.compareTo(b) > 0) {
			return a;
		}
		return b;
	}

	/**
	 * @param <T> comparable template
	 * @param a   a value
	 * @param b   b value
	 * @return the minimum of two values
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Comparable> T minOf(T a, T b) {
		if (a.compareTo(b) < 0) {
			return a;
		}
		return b;
	}

	// ----------------------------------------------
	// some random number functions
	// ----------------------------------------------
	private static Random __rand = new Random();

	/**
	 * Initialize seed
	 * 
	 * @param seed seed value
	 */
	public static void setSeed(long seed) {
		__rand.setSeed(seed);
	}

	/**
	 * @param x x value
	 * @param y y value
	 * @return a random integer between x and y (included x and y)
	 */
	public static int randInt(int x, int y) {
		return x + (int)(Math.random() * ((y - x) + 1));
	}

	/**
	 * @return a random float between <b>zero</b> and <b>1</b>
	 */
	public static float randFloat() {
		return __rand.nextFloat();
	}

	/**
	 * @param x x value
	 * @param y y value
	 * @return a random float between x and y
	 */
	public static float randInRange(float x, float y) {
		return x + randFloat() * (y - x);
	}

	/**
	 * @return a random boolean
	 */
	public static boolean randBool() {
		if (randFloat() > 0.5f) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return a random float in the range <b>[-1, 1]</b>
	 */
	public static float randomClamped() {
		return randFloat() - randFloat();
	}

	/**
	 * @return a random number with a normal distribution. More details
	 *         <a href="http://www.taygeta.com/random/gaussian.html">here</a>
	 */
	public static float randGaussian() {
		return randGaussian(0, 1);
	}

	private static float Y2 = 0;
	private static boolean USE_LAST = false;

	public static float randGaussian(float mean, float standardDeviation) {

		float x1, x2, w, y1;

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

	// Clamps the first argument between the second two
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
