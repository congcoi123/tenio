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
package com.tenio.core.message;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.common.configuration.constant.CommonConstant;
import com.tenio.common.data.elements.CommonObject;
import com.tenio.common.exceptions.NullElementPoolException;
import com.tenio.common.pool.ElementsPool;
import com.tenio.core.pool.CommonObjectPool;

/**
 * @author kong
 */
public final class MessageObjectPoolTest {

	private ElementsPool<CommonObject> __objectPool;

	@BeforeEach
	public void initialize() {
		__objectPool = new CommonObjectPool();
	}

	@AfterEach
	public void tearDown() {
		__objectPool.cleanup();
	}

	@Test
	public void createNewMessageObjectShouldReturnSuccess() {
		CommonObject object = __objectPool.get();
		
		assertNotEquals(null, object);
	}

	@Test
	public void repayAnObjectWithoutGetShouldCauseException() {
		assertThrows(NullElementPoolException.class, () -> {
			CommonObject object = CommonObject.newInstance();
			__objectPool.repay(object);
		});
	}

	@Test
	public void afterReplayObjectShouldBeClearedAllData() {
		CommonObject object = __objectPool.get();
		object.put("key1", "value1");
		object.put("key2", "value2");
		object.put("key3", "value3");
		__objectPool.repay(object);
		
		assertTrue(object.isEmpty());
	}

	@Test
	public void createNumberOfElementsShouldLessThanPoolSize() {
		int numberElement = 100;
		for (int i = 0; i < numberElement; i++) {
			__objectPool.get();
		}
		int expectedPoolSize = 0;
		if (numberElement <= CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL) {
			expectedPoolSize = CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL;
		} else {
			double p = Math
					.ceil((double) (numberElement - CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL) / (double) CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL);
			expectedPoolSize = (int) (CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL + CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL * p);
		}
		final int expected = expectedPoolSize;
		
		assertAll("createNumberOfElements", () -> assertEquals(expected, __objectPool.getPoolSize()),
				() -> assertTrue(__objectPool.getPoolSize() > numberElement));
	}

}
