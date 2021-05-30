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
package com.tenio.engine.physic2d.common;

import com.tenio.common.utilities.MathUtility;
import com.tenio.engine.fsm.entity.AbstractEntity;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * The base game entity class, it aims for creating movable objects.
 */
public abstract class BaseGameEntity extends AbstractEntity {

	public static final int DEFAULT_ENTITY_TYPE = -1;

	/**
	 * Every entity has a type associated with it (health, troll, ammo etc)
	 */
	private int __type;
	/**
	 * This is a generic flag
	 */
	private boolean __tag;
	// It's position in the environment
	private float __positionX;
	private float __positionY;
	/**
	 * Using this vector for temporary calculation, so the change of this vector
	 * does not effect to current instance
	 */
	private Vector2 __position = Vector2.newInstance();
	// It's current scale rate
	private float __scaleX;
	private float __scaleY;
	/**
	 * Using this vector for temporary calculation, so the change of this vector
	 * does not effect to current instance
	 */
	private Vector2 __scale = Vector2.newInstance();
	/**
	 * This object's bounding radius
	 */
	private float __boundingRadius;

	protected BaseGameEntity() {
		__boundingRadius = 0;
		__positionX = 0;
		__positionY = 0;
		__scaleX = 0;
		__scaleY = 0;
		__type = DEFAULT_ENTITY_TYPE;
		__tag = false;
	}

	protected BaseGameEntity(int type) {
		this();
		__type = type;
		__scaleX = 1;
		__scaleY = 1;
	}

	protected BaseGameEntity(int type, float positionX, float positionY, float radius) {
		this();
		__type = type;
		__positionX = positionX;
		__positionY = positionY;
		__boundingRadius = radius;
	}

	public float getPositionX() {
		return __positionX;
	}

	public float getPositionY() {
		return __positionY;
	}

	public Vector2 getPosition() {
		return __position.set(__positionX, __positionY);
	}

	public void setPosition(float x, float y) {
		__positionX = x;
		__positionY = y;
	}

	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
	}

	public float getScaleX() {
		return __scaleX;
	}

	public float getScaleY() {
		return __scaleY;
	}

	public Vector2 getScale() {
		return __scale.set(__scaleX, __scaleY);
	}

	public void setScale(float x, float y) {
		__boundingRadius *= MathUtility.maxOf(x, y) / MathUtility.maxOf(__scaleX, __scaleY);
		__scaleX = x;
		__scaleY = y;
	}

	public void setScale(Vector2 scale) {
		setScale(scale.x, scale.y);
	}

	public void setScale(float val) {
		__boundingRadius *= (val / MathUtility.maxOf(__scaleX, __scaleY));
		__scaleX = val;
		__scaleY = val;
	}

	public float getBoundingRadius() {
		return __boundingRadius;
	}

	public void setBoundingRadius(float radius) {
		__boundingRadius = radius;
	}

	public boolean isTagged() {
		return __tag;
	}

	public void enableTag(boolean enabled) {
		__tag = enabled;
	}

	public int getType() {
		return __type;
	}

	public void setType(int type) {
		__type = type;
	}

}
