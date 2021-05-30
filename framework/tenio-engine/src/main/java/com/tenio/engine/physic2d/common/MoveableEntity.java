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

import com.tenio.engine.physic2d.math.Matrix3;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * A base class defining an entity that moves. The entity has a local coordinate
 * system <b>root(0, 0)</b> and members for defining its mass and velocity.
 */
public abstract class MoveableEntity extends BaseGameEntity {

	// for temporary calculations
	private final Vector2 __temp1 = Vector2.newInstance();
	private final Matrix3 __matrix = Matrix3.newInstance();

	// vector for velocity
	private float __velocityX;
	private float __velocityY;
	private Vector2 __velocity = Vector2.newInstance();
	// a normalized vector pointing in the direction the entity is heading
	private float __headingX;
	private float __headingY;
	private Vector2 __heading = Vector2.newInstance();
	// a vector perpendicular to the heading vector
	private float __sideX;
	private float __sideY;
	private Vector2 __side = Vector2.newInstance();
	// for entity's mass
	private float __mass;
	// the maximum speed this entity may travel at
	private float __maxSpeed;
	// the maximum force this entity can produce to power itself
	// (think rockets and thrust)
	private float __maxForce;
	// the maximum rate (radians per second) this vehicle can rotate
	private float __maxTurnRate;
	// current rotation
	private float __rotation;

	public MoveableEntity(Vector2 position, float radius, Vector2 velocity, float maxSpeed, Vector2 heading, float mass,
			Vector2 scale, float maxTurnRate, float maxForce) {
		super(0, position.x, position.y, radius);

		setVelocity(velocity);
		setScale(scale);
		setHeading(heading);
		__mass = mass;
		__maxSpeed = maxSpeed;
		__maxTurnRate = maxTurnRate;
		__maxForce = maxForce;
	}

	public float getVelocityX() {
		return __velocityX;
	}

	public float getVelocityY() {
		return __velocityY;
	}

	public Vector2 getVelocity() {
		return __velocity.set(__velocityX, __velocityY);
	}

	public void setVelocity(float x, float y) {
		__velocityX = x;
		__velocityY = y;
	}

	public void setVelocity(Vector2 velocity) {
		setVelocity(velocity.x, velocity.y);
	}

	public float getHeadingX() {
		return __headingX;
	}

	public float getHeadingY() {
		return __headingY;
	}

	public Vector2 getHeading() {
		return __heading.set(__headingX, __headingY);
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
		__headingX = x;
		__headingY = y;
		// the side vector must always be perpendicular to the heading
		__temp1.set(x, y).perpendicular();
		__sideX = __temp1.x;
		__sideY = __temp1.y;
		// update the rotation
		float angle = (float) Math.atan2(__headingY, __headingX);
		float degrees = (float) (180 * angle / Math.PI);
		degrees = (360 + Math.round(degrees)) % 360;
		__rotation = degrees;
	}

	public void setHeading(Vector2 heading) {
		setHeading(heading.x, heading.y);
	}

	public float getSideX() {
		return __sideX;
	}

	public float getSideY() {
		return __sideY;
	}

	public Vector2 getSide() {
		return __side.set(__sideX, __sideY);
	}

	private void __setSide(Vector2 side) {
		__sideX = side.x;
		__sideY = side.y;
	}

	public float getRotation() {
		return __rotation;
	}

	public float getMass() {
		return __mass;
	}

	public float getMaxSpeed() {
		return __maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		__maxSpeed = maxSpeed;
	}

	public float getMaxForce() {
		return __maxForce;
	}

	public void setMaxForce(float maxForce) {
		__maxForce = maxForce;
	}

	public boolean isSpeedMaxedOut() {
		return __maxSpeed * __maxSpeed >= getVelocity().getLengthSqr();
	}

	public float getSpeed() {
		return getVelocity().getLength();
	}

	public float getSpeedSqr() {
		return getVelocity().getLengthSqr();
	}

	public float getMaxTurnRate() {
		return __maxTurnRate;
	}

	public void setMaxTurnRate(float maxTurnRate) {
		__maxTurnRate = maxTurnRate;
	}

	/**
	 * Given a target position, this method rotates the entity's heading and side
	 * vectors by an amount not greater than m_dMaxTurnRate until it directly faces
	 * the target.
	 *
	 * @param target the new target vector
	 * 
	 * @return <b>true</b> when the heading is facing in the desired direction
	 */
	public boolean isRotatedHeadingToFacePosition(Vector2 target) {
		// get direction between 2 vectors
		__temp1.set(target).sub(getPosition()).normalize();

		// first determine the angle between the heading vector and the target
		float angle = (float) Math.acos(getHeading().getDotProductValue(__temp1));
		if (Float.isNaN(angle)) {
			angle = 0;
		}

		// return true if the player is facing the target
		if (angle < 0.00001) {
			return true;
		}

		// clamp the amount to turn to the max turn rate
		if (angle > __maxTurnRate) {
			angle = __maxTurnRate;
		}

		// The next few lines use a rotation matrix to rotate the player's heading
		// vector accordingly
		__matrix.initialize();

		// notice how the direction of rotation has to be determined when creating
		// the rotation matrix
		__matrix.rotate(angle * getHeading().getSignValue(__temp1));
		__matrix.transformVector2D(getHeading());
		__matrix.transformVector2D(getVelocity());

		// finally recreate m_vSide
		__setSide(getHeading().perpendicular());

		return false;
	}

}
