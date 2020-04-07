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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.configuration.constant.Constants;
import com.tenio.ecs.model.GameComponents;
import com.tenio.ecs.model.GameEntity;
import com.tenio.engine.ecs.ContextInfo;
import com.tenio.engine.ecs.api.IEntity;
import com.tenio.engine.ecs.pool.EntityPool;
import com.tenio.exception.NullElementPoolException;
import com.tenio.pool.IElementPool;

/**
 * @author kong
 */
public final class EntityPoolTest {

	private IElementPool<IEntity> __entityPool;

	@BeforeEach
	public void initialize() {
		ContextInfo info = new ContextInfo("Game", GameComponents.getComponentNames(),
				GameComponents.getComponentTypes(), GameComponents.getNumberComponents());
		__entityPool = new EntityPool(GameEntity.class, info);
	}

	@AfterEach
	public void tearDown() {
		__entityPool.cleanup();
	}

	@Test
	public void createNewEntityShouldReturnSuccess() {
		IEntity entity = __entityPool.get();
		
		assertNotEquals(null, entity);
	}

	@Test
	public void repayAnEntityWithoutGetShouldCauseException() {
		assertThrows(NullElementPoolException.class, () -> {
			IEntity entity = new GameEntity();
			__entityPool.repay(entity);
		});
	}

	@Test
	public void createNumberOfElementsShouldLessThanPoolSize() {
		int numberElement = 100;
		for (int i = 0; i < numberElement; i++) {
			__entityPool.get();
		}
		int expectedPoolSize = 0;
		if (numberElement <= Constants.BASE_ELEMENT_POOL) {
			expectedPoolSize = Constants.BASE_ELEMENT_POOL;
		} else {
			double p = Math
					.ceil((double) (numberElement - Constants.BASE_ELEMENT_POOL) / (double) Constants.ADD_ELEMENT_POOL);
			expectedPoolSize = (int) (Constants.BASE_ELEMENT_POOL + Constants.ADD_ELEMENT_POOL * p);
		}
		final int expected = expectedPoolSize;
		
		assertAll("createNumberOfElements", () -> assertEquals(expected, __entityPool.getPoolSize()),
				() -> assertTrue(__entityPool.getPoolSize() > numberElement));
	}

}
