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
import com.tenio.engine.fsm.entity.AbstractEntity;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * The base game entity class, it aims for creating movable objects.
 */
public abstract class BaseGameEntity extends AbstractEntity {

  public static final int DEFAULT_ENTITY_TYPE = -1;
  /**
   * Using this vector for temporary calculation, so the change of this vector
   * does not affect to current instance.
   */
  private final Vector2 position = Vector2.newInstance();
  /**
   * Using this vector for temporary calculation, so the change of this vector
   * does not affect to current instance.
   */
  private final Vector2 scale = Vector2.newInstance();
  /**
   * Every entity has a type associated with it (health, troll, ammo, etc.).
   */
  private int type;
  /**
   * This is a generic flag.
   */
  private boolean tag;
  // It's position in the environment
  private float positionX;
  private float positionY;
  // It's current scale rate
  private float scaleX;
  private float scaleY;
  /**
   * This object's bounding radius.
   */
  private float boundingRadius;

  protected BaseGameEntity() {
    boundingRadius = 0;
    positionX = 0;
    positionY = 0;
    scaleX = 0;
    scaleY = 0;
    type = DEFAULT_ENTITY_TYPE;
    tag = false;
  }

  protected BaseGameEntity(int type) {
    this();
    this.type = type;
    scaleX = 1;
    scaleY = 1;
  }

  protected BaseGameEntity(int type, float positionX, float positionY, float radius) {
    this();
    this.type = type;
    this.positionX = positionX;
    this.positionY = positionY;
    boundingRadius = radius;
  }

  public float getPositionX() {
    return positionX;
  }

  public float getPositionY() {
    return positionY;
  }

  public Vector2 getPosition() {
    return position.set(positionX, positionY);
  }

  public void setPosition(Vector2 position) {
    setPosition(position.x, position.y);
  }

  public void setPosition(float x, float y) {
    positionX = x;
    positionY = y;
  }

  public float getScaleX() {
    return scaleX;
  }

  public float getScaleY() {
    return scaleY;
  }

  public Vector2 getScale() {
    return scale.set(scaleX, scaleY);
  }

  public void setScale(Vector2 scale) {
    setScale(scale.x, scale.y);
  }

  /**
   * Set the new scale value.
   *
   * @param value the new value
   */
  public void setScale(float value) {
    boundingRadius *= (value / MathUtility.maxOf(scaleX, scaleY));
    scaleX = value;
    scaleY = value;
  }

  /**
   * Set the new scale value.
   *
   * @param x in width
   * @param y in height
   */
  public void setScale(float x, float y) {
    boundingRadius *= MathUtility.maxOf(x, y) / MathUtility.maxOf(scaleX, scaleY);
    scaleX = x;
    scaleY = y;
  }

  public float getBoundingRadius() {
    return boundingRadius;
  }

  public void setBoundingRadius(float radius) {
    boundingRadius = radius;
  }

  public boolean isTagged() {
    return tag;
  }

  public void enableTag(boolean enabled) {
    tag = enabled;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}
