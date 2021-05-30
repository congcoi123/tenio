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
package com.tenio.examples.example4.entities;

import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * This class is used to create and render 2D walls. Defined as the two vectors
 * A - B with a perpendicular normal
 */
public final class Wall implements Renderable {

	private final Vector2 __temp = Vector2.newInstance();

	private float __vAX;
	private float __vAY;
	private Vector2 __vA;
	private float __vBX;
	private float __vBY;
	private Vector2 __vB;
	private float __vNX;
	private float __vNY;
	private Vector2 __vN;
	private float __vCX;
	private float __vCY;
	private Vector2 __vC;

	private boolean __renderNormal = false;

	public Wall() {

	}

	public Wall(float aX, float aY, float bX, float bY) {
		__vAX = aX;
		__vAY = aY;
		__vBX = bX;
		__vBY = bY;
		___calculateNormal();
	}

	public Wall(float aX, float aY, float bX, float bY, float nX, float nY) {
		__vAX = aX;
		__vAY = aY;
		__vBX = bX;
		__vBY = bY;
		__vNX = nX;
		__vNY = nY;
	}

	private void ___calculateNormal() {
		__temp.set(__vBX, __vBY).sub(__vAX, __vAY).normalize();
		__vNX = -__temp.y;
		__vNY = __temp.x;

		__temp.set(__vAX, __vAY).add(__vBX, __vBY).div(2);
		__vCX = __temp.x;
		__vCY = __temp.y;
	}

	public float getFromX() {
		return __vAX;
	}

	public float getFromY() {
		return __vAY;
	}

	public Vector2 getFrom() {
		return __vA.set(__vAX, __vAY);
	}

	public void setFrom(float x, float y) {
		__vAX = x;
		__vAY = y;
		___calculateNormal();
	}

	public float getToX() {
		return __vBX;
	}

	public float getToY() {
		return __vBY;
	}

	public Vector2 getTo() {
		return __vB.set(__vBX, __vBY);
	}

	public void setTo(float x, float y) {
		__vBX = x;
		__vBY = y;
		___calculateNormal();
	}

	public float getNormalX() {
		return __vNX;
	}

	public float getNormalY() {
		return __vNY;
	}

	public Vector2 getNormal() {
		return __vN.set(__vNX, __vNY);
	}

	public void setNormal(float x, float y) {
		__vNX = x;
		__vNY = y;
	}

	public Vector2 getCenter() {
		return __vC.set(__vCX, __vCY);
	}

	public void enableRenderNormal(boolean enabled) {
		__renderNormal = enabled;
	}

	@Override
	public void render(Paint paint) {
		paint.drawLine(__vAX, __vAY, __vBX, __vBY);

		// render the normals
		if (__renderNormal) {
			int midX = (int) ((__vAX + __vBX) / 2);
			int midY = (int) ((__vAY + __vBY) / 2);

			paint.drawLine(midX, midY, (int) (midX + (__vNX * 5)), (int) (midY + (__vNY * 5)));
		}
	}

}
