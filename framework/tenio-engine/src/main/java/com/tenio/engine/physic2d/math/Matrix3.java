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
package com.tenio.engine.physic2d.math;

import java.util.List;

/**
 * A 3x3 <a href="http://en.wikipedia.org/wiki/Row-major_order">row major</a>
 * matrix. It's useful for 2D transforms.<br>
 * More detail in <a href=
 * "https://medium.com/swlh/understanding-3d-matrix-transforms-with-pixijs-c76da3f8bd8">this
 * document</a>.
 */
public final class Matrix3 {

	private class Matrix {

		public float _11, _12, _13;
		public float _21, _22, _23;
		public float _31, _32, _33;

		public Matrix() {
			initialize();
		}

		public void initialize() {
			_11 = 0; _12 = 0; _13 = 0;
			_21 = 0; _22 = 0; _23 = 0;
			_31 = 0; _32 = 0; _33 = 0;
		}

		public void set(float _11, float _12, float _13, float _21, float _22, float _23, float _31, float _32,
				float _33) {
			this._11 = _11; this._12 = _12; this._13 = _13;
			this._21 = _21; this._22 = _22; this._23 = _23;
			this._31 = _31; this._32 = _32; this._33 = _33;
		}

	}

	// The temporary object for calculations
	private final Matrix __matrix = new Matrix();
	private final Matrix __temp = new Matrix();
	
	public static Matrix3 newInstance() {
		return new Matrix3();
	}

	private Matrix3() {
		// initialize the matrix to an identity matrix
		initialize();
	}

	/**
	 * Create an identity matrix
	 */
	public void initialize() {
		__matrix._11 = 1; __matrix._12 = 0; __matrix._13 = 0;
		__matrix._21 = 0; __matrix._22 = 1; __matrix._23 = 0;
		__matrix._31 = 0; __matrix._32 = 0; __matrix._33 = 1;
	}

	public void _11(float val) {
		__matrix._11 = val;
	}

	public void _12(float val) {
		__matrix._12 = val;
	}

	public void _13(float val) {
		__matrix._13 = val;
	}

	public void _21(float val) {
		__matrix._21 = val;
	}

	public void _22(float val) {
		__matrix._22 = val;
	}

	public void _23(float val) {
		__matrix._23 = val;
	}

	public void _31(float val) {
		__matrix._31 = val;
	}

	public void _32(float val) {
		__matrix._32 = val;
	}

	public void _33(float val) {
		__matrix._33 = val;
	}

	/**
	 * Multiply two matrices together by rows
	 * 
	 * @param matrix see {@link Matrix}
	 */
	private void __mul(final Matrix matrix) {
		// first
		float _11 = (__matrix._11 * matrix._11) + (__matrix._12 * matrix._21) + (__matrix._13 * matrix._31);
		float _12 = (__matrix._11 * matrix._12) + (__matrix._12 * matrix._22) + (__matrix._13 * matrix._32);
		float _13 = (__matrix._11 * matrix._13) + (__matrix._12 * matrix._23) + (__matrix._13 * matrix._33);

		// second
		float _21 = (__matrix._21 * matrix._11) + (__matrix._22 * matrix._21) + (__matrix._23 * matrix._31);
		float _22 = (__matrix._21 * matrix._12) + (__matrix._22 * matrix._22) + (__matrix._23 * matrix._32);
		float _23 = (__matrix._21 * matrix._13) + (__matrix._22 * matrix._23) + (__matrix._23 * matrix._33);

		// third
		float _31 = (__matrix._31 * matrix._11) + (__matrix._32 * matrix._21) + (__matrix._33 * matrix._31);
		float _32 = (__matrix._31 * matrix._12) + (__matrix._32 * matrix._22) + (__matrix._33 * matrix._32);
		float _33 = (__matrix._31 * matrix._13) + (__matrix._32 * matrix._23) + (__matrix._33 * matrix._33);

		__matrix.set(_11, _12, _13, _21, _22, _23, _31, _32, _33);
	}

	/**
	 * Applies a 2D transformation matrix to a list of Vector2Ds
	 * 
	 * @param points see {@link Vector2}
	 */
	public void transformVector2Ds(List<Vector2> points) {
		points.forEach(vector -> {
			float tempX = (__matrix._11 * vector.x) + (__matrix._21 * vector.y) + (__matrix._31);
			float tempY = (__matrix._12 * vector.x) + (__matrix._22 * vector.y) + (__matrix._32);
			vector.x = tempX;
			vector.y = tempY;
		});
	}

	/**
	 * Applies a 2D transformation matrix to a single Vector2D
	 * 
	 * @param point see {@link Vector2}
	 */
	public void transformVector2D(Vector2 point) {
		float tempX = (__matrix._11 * point.x) + (__matrix._21 * point.y) + (__matrix._31);
		float tempY = (__matrix._12 * point.x) + (__matrix._22 * point.y) + (__matrix._32);

		point.x = tempX;
		point.y = tempY;
	}

	/**
	 * Create a transformation matrix
	 * 
	 * @param x new x value
	 * @param y new y value
	 */
	public void translate(float x, float y) {
		/*
		 * matrix._11 = 1; matrix._12 = 0; matrix._13 = 0;
		 * matrix._21 = 0; matrix._22 = 1; matrix._23 = 0;
		 * matrix._31 = x; matrix._32 = y; matrix._33 = 1;
		 */

		__temp.initialize();
		__temp.set(1, 0, 0, 0, 1, 0, x, y, 1);

		// and multiply
		__mul(__temp);
	}

	/**
	 * Create a scale matrix
	 * 
	 * @param xScale scale horizon
	 * @param yScale scale vertical
	 */
	public void scale(float xScale, float yScale) {
		/*
		 * matrix._11 = xScale;	matrix._12 = 0;			matrix._13 = 0;
		 * matrix._21 = 0;		matrix._22 = yScale;	matrix._23 = 0;
		 * matrix._31 = 0;		matrix._32 = 0;			matrix._33 = 1;
		 */

		__temp.initialize();
		__temp.set(xScale, 0, 0, 0, yScale, 0, 0, 0, 1);

		// and multiply
		__mul(__temp);
	}

	/**
	 * Create a rotation matrix
	 * 
	 * @param rotation rotation value
	 */
	public void rotate(float rotation) {
		float sin = (float) Math.sin(rotation);
		float cos = (float) Math.cos(rotation);

		/*
		 * matrix._11 = cos;	matrix._12 = sin;	matrix._13 = 0;
		 * matrix._21 = -sin;	matrix._22 = cos;	matrix._23 = 0;
		 * matrix._31 = 0;		matrix._32 = 0;		matrix._33 = 1;
		 */

		__temp.initialize();
		__temp.set(cos, sin, 0, -sin, cos, 0, 0, 0, 1);

		// and multiply
		__mul(__temp);
	}

	/**
	 * Create a rotation matrix from a 2D vector
	 * 
	 * @param forward forward vector
	 * @param side side vector
	 */
	public void rotate(Vector2 forward, Vector2 side) {
		/*
		 * matrix._11 = forward.x;	matrix._12 = forward.y; matrix._13 = 0;
		 * matrix._21 = side.x;		matrix._22 = side.y;	matrix._23 = 0;
		 * matrix._31 = 0;			matrix._32 = 0;			matrix._33 = 1;
		 */

		__temp.initialize();
		__temp.set(forward.x, forward.y, 0, side.x, side.y, 0, 0, 0, 1);

		// and multiply
		__mul(__temp);
	}

}
