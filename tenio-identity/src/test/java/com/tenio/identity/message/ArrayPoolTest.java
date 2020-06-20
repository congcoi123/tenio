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
package com.tenio.identity.message;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.common.configuration.constant.CommonConstants;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.pool.IElementPool;
import com.tenio.identity.api.pool.ArrayPool;
import com.tenio.identity.entity.element.TArray;

/**
 * @author kong
 */
public final class ArrayPoolTest {

	private IElementPool<TArray> __arrayPool;

	@BeforeEach
	public void initialize() {
		__arrayPool = new ArrayPool();
	}

	@AfterEach
	public void tearDown() {
		__arrayPool.cleanup();
	}

	@Test
	public void createNewTArrayShouldReturnSuccess() {
		TArray array = __arrayPool.get();
		
		assertNotEquals(null, array);
	}

	@Test
	public void repayAnArrayWithoutGetShouldCauseException() {
		assertThrows(NullElementPoolException.class, () -> {
			TArray array = TArray.newInstance();
			__arrayPool.repay(array);
		});
	}

	@Test
	public void afterReplayArrayShouldBeClearedAllData() {
		TArray array = __arrayPool.get();
		array.put(10).put(20).put(30);
		__arrayPool.repay(array);
		
		assertTrue(array.isEmpty());
	}

	@Test
	public void createNumberOfElementsShouldLessThanPoolSize() {
		int numberElement = 100;
		for (int i = 0; i < numberElement; i++) {
			__arrayPool.get();
		}
		int expectedPoolSize = 0;
		if (numberElement <= CommonConstants.BASE_ELEMENT_POOL) {
			expectedPoolSize = CommonConstants.BASE_ELEMENT_POOL;
		} else {
			double p = Math
					.ceil((double) (numberElement - CommonConstants.BASE_ELEMENT_POOL) / (double) CommonConstants.ADD_ELEMENT_POOL);
			expectedPoolSize = (int) (CommonConstants.BASE_ELEMENT_POOL + CommonConstants.ADD_ELEMENT_POOL * p);
		}
		final int expected = expectedPoolSize;
		
		assertAll("createNumberOfElements", () -> assertEquals(expected, __arrayPool.getPoolSize()),
				() -> assertTrue(__arrayPool.getPoolSize() > numberElement));
	}

}
