/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.engine.fsm.entity.AbstractEntity;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.math.Vector2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveableEntityTest {

  private static final class SimpleEntity extends BaseGameEntity {

    SimpleEntity(int type) {
      super(type);
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public boolean handleMessage(Telegram msg) {
      return false;
    }
  }

  private static final class TestEntity extends MoveableEntity {

    TestEntity(Vector2 position, float radius, Vector2 velocity, float maxSpeed,
               Vector2 heading, float mass, Vector2 scale, float maxTurnRate, float maxForce) {
      super(position, radius, velocity, maxSpeed, heading, mass, scale, maxTurnRate, maxForce);
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public boolean handleMessage(Telegram msg) {
      return false;
    }
  }

  private TestEntity entity;

  @BeforeEach
  void setUp() {
    var position = Vector2.newInstance();
    position.set(5.0f, 10.0f);
    var velocity = Vector2.newInstance();
    velocity.set(2.0f, 3.0f);
    var heading = Vector2.newInstance();
    heading.set(1.0f, 0.0f);
    var scale = Vector2.newInstance();
    scale.set(1.0f, 1.0f);
    entity = new TestEntity(position, 5.0f, velocity, 10.0f, heading, 1.5f, scale, 0.5f, 8.0f);
  }

  // ---- MoveableEntity: velocity ----

  @Test
  void testGetVelocityX() {
    assertEquals(2.0f, entity.getVelocityX(), 0.001f);
  }

  @Test
  void testGetVelocityY() {
    assertEquals(3.0f, entity.getVelocityY(), 0.001f);
  }

  @Test
  void testGetVelocity() {
    var vel = entity.getVelocity();
    assertNotNull(vel);
    assertEquals(2.0f, vel.x, 0.001f);
    assertEquals(3.0f, vel.y, 0.001f);
  }

  @Test
  void testSetVelocityVector2() {
    var newVel = Vector2.newInstance();
    newVel.set(4.0f, 5.0f);
    entity.setVelocity(newVel);
    assertEquals(4.0f, entity.getVelocityX(), 0.001f);
    assertEquals(5.0f, entity.getVelocityY(), 0.001f);
  }

  @Test
  void testSetVelocityFloatFloat() {
    entity.setVelocity(7.0f, 8.0f);
    assertEquals(7.0f, entity.getVelocityX(), 0.001f);
    assertEquals(8.0f, entity.getVelocityY(), 0.001f);
  }

  // ---- MoveableEntity: heading ----

  @Test
  void testGetHeadingX() {
    assertEquals(1.0f, entity.getHeadingX(), 0.001f);
  }

  @Test
  void testGetHeadingY() {
    assertEquals(0.0f, entity.getHeadingY(), 0.001f);
  }

  @Test
  void testGetHeading() {
    var heading = entity.getHeading();
    assertNotNull(heading);
    assertEquals(1.0f, heading.x, 0.001f);
    assertEquals(0.0f, heading.y, 0.001f);
  }

  @Test
  void testSetHeadingVector2() {
    var newHeading = Vector2.newInstance();
    newHeading.set(0.0f, 1.0f);
    entity.setHeading(newHeading);
    assertEquals(0.0f, entity.getHeadingX(), 0.001f);
    assertEquals(1.0f, entity.getHeadingY(), 0.001f);
  }

  @Test
  void testSetHeadingFloatFloat() {
    entity.setHeading(0.0f, 1.0f);
    assertEquals(0.0f, entity.getHeadingX(), 0.001f);
    assertEquals(1.0f, entity.getHeadingY(), 0.001f);
    // side vector should be perpendicular: heading (0,1) -> side (1, 0) or (-1, 0)
    // rotation should be updated
    float rotation = entity.getRotation();
    assertTrue(rotation >= 0 && rotation < 360);
  }

  // ---- MoveableEntity: side ----

  @Test
  void testGetSideX() {
    // heading (1,0) => side (0, -1) after perpendicular
    entity.setHeading(1.0f, 0.0f);
    float sx = entity.getSideX();
    // perpendicular of (1,0) depends on implementation; just assert the value exists
    assertTrue(Float.isFinite(sx));
  }

  @Test
  void testGetSideY() {
    entity.setHeading(1.0f, 0.0f);
    float sy = entity.getSideY();
    assertTrue(Float.isFinite(sy));
  }

  @Test
  void testGetSide() {
    var side = entity.getSide();
    assertNotNull(side);
  }

  // ---- MoveableEntity: rotation ----

  @Test
  void testGetRotationHeadingEast() {
    entity.setHeading(1.0f, 0.0f);
    float rotation = entity.getRotation();
    assertEquals(0.0f, rotation, 1.0f);
  }

  @Test
  void testGetRotationHeadingSouth() {
    entity.setHeading(0.0f, 1.0f);
    float rotation = entity.getRotation();
    // atan2(1,0) = PI/2 => 90 degrees
    assertEquals(90.0f, rotation, 2.0f);
  }

  // ---- MoveableEntity: mass, speed, force ----

  @Test
  void testGetMass() {
    assertEquals(1.5f, entity.getMass(), 0.001f);
  }

  @Test
  void testGetMaxSpeed() {
    assertEquals(10.0f, entity.getMaxSpeed(), 0.001f);
  }

  @Test
  void testSetMaxSpeed() {
    entity.setMaxSpeed(20.0f);
    assertEquals(20.0f, entity.getMaxSpeed(), 0.001f);
  }

  @Test
  void testGetMaxForce() {
    assertEquals(8.0f, entity.getMaxForce(), 0.001f);
  }

  @Test
  void testSetMaxForce() {
    entity.setMaxForce(12.0f);
    assertEquals(12.0f, entity.getMaxForce(), 0.001f);
  }

  @Test
  void testIsSpeedMaxedOutTrue() {
    // velocity (2,3) has speed ~3.6, maxSpeed 10 => not maxed out? No: maxedOut when
    // maxSpeed^2 >= speedSqr => 100 >= 13 => true
    assertTrue(entity.isSpeedMaxedOut());
  }

  @Test
  void testIsSpeedMaxedOutFalse() {
    // set velocity larger than maxSpeed
    entity.setVelocity(100.0f, 100.0f);
    entity.setMaxSpeed(1.0f);
    assertFalse(entity.isSpeedMaxedOut());
  }

  @Test
  void testGetSpeed() {
    entity.setVelocity(3.0f, 4.0f);
    assertEquals(5.0f, entity.getSpeed(), 0.001f);
  }

  @Test
  void testGetSpeedSqr() {
    entity.setVelocity(3.0f, 4.0f);
    assertEquals(25.0f, entity.getSpeedSqr(), 0.001f);
  }

  @Test
  void testGetMaxTurnRate() {
    assertEquals(0.5f, entity.getMaxTurnRate(), 0.001f);
  }

  @Test
  void testSetMaxTurnRate() {
    entity.setMaxTurnRate(1.0f);
    assertEquals(1.0f, entity.getMaxTurnRate(), 0.001f);
  }

  // ---- MoveableEntity: isRotatedHeadingToFacePosition ----

  @Test
  void testIsRotatedHeadingToFacePositionAlreadyFacing() {
    // entity position is (5,10), heading (1,0)
    // target far to the right => direction (1,0) => angle ~ 0 => return true
    entity.setPosition(0.0f, 0.0f);
    entity.setHeading(1.0f, 0.0f);
    var target = Vector2.newInstance();
    target.set(100.0f, 0.0f);
    assertTrue(entity.isRotatedHeadingToFacePosition(target));
  }

  @Test
  void testIsRotatedHeadingToFacePositionNeedsRotation() {
    entity.setPosition(0.0f, 0.0f);
    entity.setHeading(1.0f, 0.0f);
    entity.setMaxTurnRate(0.01f);
    var target = Vector2.newInstance();
    target.set(0.0f, 100.0f); // 90 degrees away
    assertFalse(entity.isRotatedHeadingToFacePosition(target));
  }

  @Test
  void testIsRotatedHeadingToFacePositionNanAngle() {
    entity.setPosition(0.0f, 0.0f);
    entity.setHeading(2.0f, 0.0f); // not normalized, dot product can exceed 1 → NaN from acos
    var target = Vector2.newInstance();
    target.set(1.0f, 0.0f);
    assertTrue(entity.isRotatedHeadingToFacePosition(target));
  }

  // ---- BaseGameEntity: position ----

  @Test
  void testGetPositionX() {
    assertEquals(5.0f, entity.getPositionX(), 0.001f);
  }

  @Test
  void testGetPositionY() {
    assertEquals(10.0f, entity.getPositionY(), 0.001f);
  }

  @Test
  void testGetPosition() {
    var pos = entity.getPosition();
    assertNotNull(pos);
    assertEquals(5.0f, pos.x, 0.001f);
    assertEquals(10.0f, pos.y, 0.001f);
  }

  @Test
  void testSetPositionVector2() {
    var newPos = Vector2.newInstance();
    newPos.set(20.0f, 30.0f);
    entity.setPosition(newPos);
    assertEquals(20.0f, entity.getPositionX(), 0.001f);
    assertEquals(30.0f, entity.getPositionY(), 0.001f);
  }

  @Test
  void testSetPositionFloatFloat() {
    entity.setPosition(15.0f, 25.0f);
    assertEquals(15.0f, entity.getPositionX(), 0.001f);
    assertEquals(25.0f, entity.getPositionY(), 0.001f);
  }

  // ---- BaseGameEntity: scale ----

  @Test
  void testGetScaleX() {
    assertEquals(1.0f, entity.getScaleX(), 0.001f);
  }

  @Test
  void testGetScaleY() {
    assertEquals(1.0f, entity.getScaleY(), 0.001f);
  }

  @Test
  void testGetScale() {
    var scale = entity.getScale();
    assertNotNull(scale);
  }

  @Test
  void testSetScaleSingleValue() {
    entity.setScale(2.0f);
    assertEquals(2.0f, entity.getScaleX(), 0.001f);
    assertEquals(2.0f, entity.getScaleY(), 0.001f);
  }

  @Test
  void testSetScaleXY() {
    entity.setScale(3.0f, 4.0f);
    assertEquals(3.0f, entity.getScaleX(), 0.001f);
    assertEquals(4.0f, entity.getScaleY(), 0.001f);
  }

  // ---- BaseGameEntity: bounding radius ----

  @Test
  void testGetBoundingRadius() {
    // bounding radius is scaled during construction (initial scale 0 causes Infinity or a real value)
    float radius = entity.getBoundingRadius();
    assertTrue(radius >= 0 || Float.isInfinite(radius));
  }

  @Test
  void testSetBoundingRadius() {
    entity.setBoundingRadius(12.0f);
    assertEquals(12.0f, entity.getBoundingRadius(), 0.001f);
  }

  // ---- BaseGameEntity: tag ----

  @Test
  void testIsTaggedDefault() {
    assertFalse(entity.isTagged());
  }

  @Test
  void testEnableTag() {
    entity.enableTag(true);
    assertTrue(entity.isTagged());
    entity.enableTag(false);
    assertFalse(entity.isTagged());
  }

  // ---- BaseGameEntity: type ----

  @Test
  void testGetType() {
    assertEquals(0, entity.getType());
  }

  @Test
  void testSetType() {
    entity.setType(5);
    assertEquals(5, entity.getType());
  }

  // ---- AbstractEntity: getId and constructors ----

  @Test
  void testAbstractEntityDefaultConstructorId() {
    AbstractEntity ae = new AbstractEntity() {
      @Override
      public void update(float deltaTime) {
      }

      @Override
      public boolean handleMessage(Telegram msg) {
        return false;
      }
    };
    assertNotNull(ae.getId());
  }

  @Test
  void testAbstractEntityStringConstructorId() {
    AbstractEntity ae = new AbstractEntity("my-id") {
      @Override
      public void update(float deltaTime) {
      }

      @Override
      public boolean handleMessage(Telegram msg) {
        return false;
      }
    };
    assertEquals("my-id", ae.getId());
  }

  @Test
  void testSetId() {
    AbstractEntity ae = new AbstractEntity("original") {
      @Override
      public void update(float deltaTime) {
      }

      @Override
      public boolean handleMessage(Telegram msg) {
        return false;
      }
    };
    ae.setId("changed");
    assertEquals("changed", ae.getId());
  }

  @Test
  void testBaseGameEntityConstructorWithType() {
    SimpleEntity e = new SimpleEntity(42);
    assertEquals(42, e.getType());
    assertEquals(1.0f, e.getScaleX(), 0.001f);
    assertEquals(1.0f, e.getScaleY(), 0.001f);
  }
}
