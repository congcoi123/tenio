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

package com.tenio.engine.physic2d.common;

import com.tenio.engine.physic2d.math.Matrix3;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * A base class defining an entity that moves. The entity has a local coordinate
 * system <b>root(0, 0)</b> and members for defining its mass and velocity.
 */
public abstract class MoveableEntity extends BaseGameEntity {

  private final Vector2 velocity = Vector2.newInstance();
  private final Vector2 heading = Vector2.newInstance();
  private final Vector2 side = Vector2.newInstance();
  // for entity's mass
  private final float mass;
  // vector for velocity
  private float velocityX;
  private float velocityY;
  // a normalized vector pointing in the direction the entity is heading
  private float headingX;
  private float headingY;
  // a vector perpendicular to the heading vector
  private float sideX;
  private float sideY;
  // the maximum speed this entity may travel at
  private float maxSpeed;
  // the maximum force this entity can produce to power itself
  // (think rockets and thrust)
  private float maxForce;
  // the maximum rate (radians per second) this vehicle can rotate
  private float maxTurnRate;
  // current rotation
  private float rotation;

  /**
   * Constructor.
   *
   * @param position    the current position
   * @param radius      the radius
   * @param velocity    the velocity
   * @param maxSpeed    the max speed
   * @param heading     the heading vector
   * @param mass        the mass
   * @param scale       the scale value
   * @param maxTurnRate the max turn rate
   * @param maxForce    the max force
   */
  public MoveableEntity(Vector2 position, float radius, Vector2 velocity, float maxSpeed,
                        Vector2 heading, float mass,
                        Vector2 scale, float maxTurnRate, float maxForce) {
    super(0, position.x, position.y, radius);

    setVelocity(velocity);
    setScale(scale);
    setHeading(heading);
    this.mass = mass;
    this.maxSpeed = maxSpeed;
    this.maxTurnRate = maxTurnRate;
    this.maxForce = maxForce;
  }

  public float getVelocityX() {
    return velocityX;
  }

  public float getVelocityY() {
    return velocityY;
  }

  public Vector2 getVelocity() {
    return velocity.set(velocityX, velocityY);
  }

  public void setVelocity(Vector2 velocity) {
    setVelocity(velocity.x, velocity.y);
  }

  public void setVelocity(float x, float y) {
    velocityX = x;
    velocityY = y;
  }

  public float getHeadingX() {
    return headingX;
  }

  public float getHeadingY() {
    return headingY;
  }

  public Vector2 getHeading() {
    return heading.set(headingX, headingY);
  }

  public void setHeading(Vector2 heading) {
    setHeading(heading.x, heading.y);
  }

  /**
   * First checks that the given heading is not a vector of zero length. If the
   * new heading is valid this function sets the entity's heading and side vectors
   * accordingly
   *
   * @param x the new heading in X
   * @param y the new heading in Y
   */
  public void setHeading(float x, float y) {
    headingX = x;
    headingY = y;
    // the side vector must always be perpendicular to the heading
    var temp = Vector2.newInstance().set(x, y).perpendicular();
    sideX = temp.x;
    sideY = temp.y;
    // update the rotation
    float angle = (float) Math.atan2(headingY, headingX);
    float degrees = (float) (180 * angle / Math.PI);
    degrees = (360 + Math.round(degrees)) % 360;
    rotation = degrees;
  }

  public float getSideX() {
    return sideX;
  }

  public float getSideY() {
    return sideY;
  }

  public Vector2 getSide() {
    return side.set(sideX, sideY);
  }

  private void setSide(Vector2 side) {
    sideX = side.x;
    sideY = side.y;
  }

  public float getRotation() {
    return rotation;
  }

  public float getMass() {
    return mass;
  }

  public float getMaxSpeed() {
    return maxSpeed;
  }

  public void setMaxSpeed(float maxSpeed) {
    this.maxSpeed = maxSpeed;
  }

  public float getMaxForce() {
    return maxForce;
  }

  public void setMaxForce(float maxForce) {
    this.maxForce = maxForce;
  }

  public boolean isSpeedMaxedOut() {
    return maxSpeed * maxSpeed >= getVelocity().getLengthSqr();
  }

  public float getSpeed() {
    return getVelocity().getLength();
  }

  public float getSpeedSqr() {
    return getVelocity().getLengthSqr();
  }

  public float getMaxTurnRate() {
    return maxTurnRate;
  }

  public void setMaxTurnRate(float maxTurnRate) {
    this.maxTurnRate = maxTurnRate;
  }

  /**
   * Given a target position, this method rotates the entity's heading and side
   * vectors by an amount not greater than m_dMaxTurnRate until it directly faces
   * the target.
   *
   * @param target the new target vector
   * @return <b>true</b> when the heading is facing in the desired direction
   */
  public boolean isRotatedHeadingToFacePosition(Vector2 target) {
    // get direction between 2 vectors
    var temp = Vector2.newInstance().set(target).sub(getPosition()).normalize();

    // first determine the angle between the heading vector and the target
    float angle = (float) Math.acos(getHeading().getDotProductValue(temp));
    if (Float.isNaN(angle)) {
      angle = 0;
    }

    // return true if the player is facing the target
    if (angle < 0.00001) {
      return true;
    }

    // clamp the amount to turn to the max turn rate
    if (angle > maxTurnRate) {
      angle = maxTurnRate;
    }

    // The next few lines use a rotation matrix to rotate the player's heading
    // vector accordingly
    var matrix3 = Matrix3.newInstance();

    // notice how the direction of rotation has to be determined when creating
    // the rotation matrix
    matrix3.rotate(angle * getHeading().getSignValue(temp));
    matrix3.transformVector2D(getHeading());
    matrix3.transformVector2D(getVelocity());

    // finally, recreate m_vSide
    setSide(getHeading().perpendicular());

    return false;
  }
}
