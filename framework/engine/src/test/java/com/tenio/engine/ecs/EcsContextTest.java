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

package com.tenio.engine.ecs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenio.engine.ecs.basis.implement.ContextInfo;
import com.tenio.engine.ecs.model.GameComponent;
import com.tenio.engine.ecs.model.GameContext;
import com.tenio.engine.ecs.model.GameEntity;
import com.tenio.engine.ecs.model.component.Position;
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
  public void removeAllEntitiesShouldReturnSucess() {
    gameContext.destroyAllEntities();

    assertEquals(0, gameContext.getEntitiesCount());
  }
}
