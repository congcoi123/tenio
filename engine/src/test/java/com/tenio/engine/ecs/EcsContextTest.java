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

package com.tenio.engine.ecs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenio.engine.ecs.basis.implement.ContextInfo;
import com.tenio.engine.ecs.model.GameComponent;
import com.tenio.engine.ecs.model.GameContext;
import com.tenio.engine.ecs.model.GameEntity;
import com.tenio.engine.ecs.model.component.Position;
import com.tenio.engine.ecs.model.component.View;
import com.tenio.engine.exception.ComponentIsNotExistedException;
import com.tenio.engine.exception.DuplicatedComponentException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EcsContextTest {

  private GameContext gameContext;
  private GameEntity gameEntity;

  @BeforeEach
  public void initialize() {
    var info = new ContextInfo("Game", GameComponent.getComponentNames(),
        GameComponent.getComponentTypes(), GameComponent.getNumberComponents());
    gameContext = new GameContext(info);

    gameEntity = gameContext.createEntity();
    gameEntity.setAnimation(true);
    gameEntity.setMotion(true);
    gameEntity.setView(false);
    gameEntity.setPosition(0, 0);
  }

  @AfterEach
  public void tearDown() {
    gameContext.reset();
  }

  @Test
  public void findEntityShouldReturnTrue() {
    assertTrue(gameContext.hasEntity(gameEntity));
  }

  @Test
  public void removeEntityShouldReturnSuccess() {
    gameContext.destroyEntity(gameEntity);

    assertFalse(gameContext.hasEntity(gameEntity));
  }

  @Test
  public void countEntitiesShouldMatchValue() {
    assertAll("countEntities", () -> assertEquals(1, gameContext.getEntitiesCount()),
        () -> assertEquals(1, gameContext.getEntities().size()));
  }

  @Test
  public void reAddEntityPositionShouldCauseException() {
    assertThrows(DuplicatedComponentException.class, () -> {
      Position position = new Position();
      position.x = 10;
      position.y = 10;

      gameEntity.setPosition(position.x, position.y);
    });
  }

  @Test
  public void changeEntityPositionShouldMatchValue() {
    Position position = new Position();
    position.x = 10;
    position.y = 10;

    ((Position) gameEntity.getComponent(GameComponent.POSITION)).x = position.x;
    ((Position) gameEntity.getComponent(GameComponent.POSITION)).y = position.y;

    assertAll("replacePosition",
        () -> assertEquals(position.x,
            ((Position) gameEntity.getComponent(GameComponent.POSITION)).x),
        () -> assertEquals(position.y,
            ((Position) gameEntity.getComponent(GameComponent.POSITION)).y));
  }

  @Test
  public void replaceEntityPositionShouldMatchValue() {
    Position position = new Position();
    position.x = 10;
    position.y = 10;

    gameEntity.replacePosition(position.x, position.y);

    assertAll("replacePosition",
        () -> assertEquals(position.x,
            ((Position) gameEntity.getComponent(GameComponent.POSITION)).x),
        () -> assertEquals(position.y,
            ((Position) gameEntity.getComponent(GameComponent.POSITION)).y));
  }

  @Test
  public void removeAllEntitiesShouldReturnSuccess() {
    gameContext.destroyAllEntities();

    assertEquals(0, gameContext.getEntitiesCount());
  }

  @Test
  public void hasComponentsShouldReturnTrueWhenAllPresent() {
    assertTrue(gameEntity.hasComponents(GameComponent.ANIMATION, GameComponent.MOTION));
  }

  @Test
  public void hasComponentsShouldReturnFalseWhenOneMissing() {
    assertFalse(gameEntity.hasComponents(GameComponent.ANIMATION, GameComponent.VIEW));
  }

  @Test
  public void hasAnyComponentShouldReturnTrueWhenAtLeastOnePresent() {
    assertTrue(gameEntity.hasAnyComponent(GameComponent.ANIMATION, GameComponent.VIEW));
  }

  @Test
  public void hasAnyComponentShouldReturnFalseWhenNonePresent() {
    assertFalse(gameEntity.hasAnyComponent(GameComponent.VIEW));
  }

  @Test
  public void hasComponentWithOutOfBoundsIndexShouldReturnFalse() {
    assertFalse(gameEntity.hasComponent(100));
  }

  @Test
  public void equalsWithSelfShouldReturnTrue() {
    assertTrue(gameEntity.equals(gameEntity));
  }

  @Test
  public void equalsWithNullShouldReturnFalse() {
    assertFalse(gameEntity.equals(null));
  }

  @Test
  public void equalsWithDifferentEntityShouldReturnFalse() {
    GameEntity other = gameContext.createEntity();
    assertFalse(gameEntity.equals(other));
  }

  @Test
  public void hashCodeShouldMatchIdHashCode() {
    assertEquals(gameEntity.getId().hashCode(), gameEntity.hashCode());
  }

  @Test
  public void getComponentsShouldReturnArrayOfExpectedLength() {
    assertNotNull(gameEntity.getComponents());
    assertEquals(GameComponent.getNumberComponents(), gameEntity.getComponents().length);
  }

  @Test
  public void removeAllComponentsShouldClearAllComponents() {
    gameEntity.removeAllComponents();
    assertFalse(gameEntity.isAnimation());
    assertFalse(gameEntity.isMotion());
    assertFalse(gameEntity.hasPosition());
  }

  @Test
  public void equalsWithDifferentClassShouldReturnFalse() {
    assertFalse(gameEntity.equals("not an entity"));
  }

  @Test
  public void getEntityByIdShouldReturnEntity() {
    var found = gameContext.getEntity(gameEntity.getId());
    assertSame(gameEntity, found);
  }

  @Test
  public void removePositionDirectlyShouldSucceed() {
    assertTrue(gameEntity.hasPosition());
    gameEntity.removePosition();
    assertFalse(gameEntity.hasPosition());
  }

  @Test
  public void removeAbsentComponentShouldThrowException() {
    assertThrows(ComponentIsNotExistedException.class,
        () -> gameEntity.removeComponent(GameComponent.VIEW));
  }

  @Test
  public void replaceComponentWithNonNullOnAbsentShouldSetComponent() {
    var view = new View();
    assertDoesNotThrow(() -> gameEntity.replaceComponent(GameComponent.VIEW, view));
    assertTrue(gameEntity.isView());
  }

  @Test
  public void setContextInfoAgainShouldNotChangeContextInfo() {
    var info = gameEntity.getContextInfo();
    gameEntity.setContextInfo(info);
    assertSame(info, gameEntity.getContextInfo());
  }

  @Test
  public void replaceComponentWithNullOnAbsentShouldBeNoOp() {
    assertDoesNotThrow(() -> gameEntity.replaceComponent(GameComponent.VIEW, null));
    assertFalse(gameEntity.isView());
  }

  @Test
  public void replaceComponentWithSameInstanceShouldBeNoOp() {
    var existing = gameEntity.getComponent(GameComponent.ANIMATION);
    assertDoesNotThrow(() -> gameEntity.replaceComponent(GameComponent.ANIMATION, existing));
    assertTrue(gameEntity.isAnimation());
  }
}
