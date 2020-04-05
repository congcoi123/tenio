/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.ecs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.ecs.model.GameComponents;
import com.tenio.ecs.model.GameContext;
import com.tenio.ecs.model.GameEntity;
import com.tenio.ecs.model.components.Position;
import com.tenio.engine.ecs.ContextInfo;

/**
 * 
 * @author kong
 *
 */
public final class ECSContextTest {

	private GameContext __context;
	private GameEntity __entity;

	@BeforeEach
	public void initialize() {
		ContextInfo info = new ContextInfo("Game", GameComponents.getComponentNames(),
				GameComponents.getComponentTypes(), GameComponents.getNumberComponents());
		__context = new GameContext(info);

		__entity = __context.createEntity();
		__entity.setEnabled(true);
		__entity.setAnimation(true);
		__entity.setMotion(true);
		__entity.setView(true);
		__entity.addPosition(0, 0);
	}

	@AfterEach
	public void tearDown() {
		__context.reset();
	}

	@Test
	public void findEntityShouldReturnTrue() {
		assertEquals(true, __context.hasEntity(__entity));
	}

	@Test
	public void removeEntityShouldReturnSuccess() {
		__context.destroyEntity(__entity);
		assertEquals(false, __context.hasEntity(__entity));
	}

	@Test
	public void countEntitesShouldMatchValue() {
		assertAll("countEntities", () -> assertEquals(1, __context.getEntitesCount()),
				() -> assertEquals(1, __context.getEntities().size()));
	}

	@Test
	public void reAddEntityPositionShouldTakeNoEffect() {
		Position position = new Position();
		position.x = 10;
		position.y = 10;

		__entity.addPosition(position.x, position.y);

		assertAll("addPosition", () -> assertEquals(0, ((Position) __entity.getComponent(GameComponents.POSITION)).x),
				() -> assertEquals(0, ((Position) __entity.getComponent(GameComponents.POSITION)).y));
	}

	@Test
	public void changeEntityPositionShouldMatchValue() {
		Position position = new Position();
		position.x = 10;
		position.y = 10;

		__entity.replacePosition(position.x, position.y);

		assertAll("replacePosition",
				() -> assertEquals(position.x, ((Position) __entity.getComponent(GameComponents.POSITION)).x),
				() -> assertEquals(position.y, ((Position) __entity.getComponent(GameComponents.POSITION)).y));
	}

	@Test
	public void removeAllEntitiesShouldReturnSucess() {
		__context.destroyAllEntities();
		assertEquals(0, __context.getEntitesCount());
	}

}
