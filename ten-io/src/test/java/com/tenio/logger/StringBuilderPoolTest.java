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
package com.tenio.logger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.configuration.constant.Constants;
import com.tenio.exception.NullElementPoolException;
import com.tenio.logger.pool.StringBuilderPool;
import com.tenio.pool.IElementPool;

/**
 * @author kong
 */
public final class StringBuilderPoolTest {

	private IElementPool<StringBuilder> __stringPool;

	@BeforeEach
	public void initialize() {
		__stringPool = new StringBuilderPool();
	}

	@AfterEach
	public void tearDown() {
		__stringPool.cleanup();
	}

	@Test
	public void createNewStringBuilderShouldReturnSuccess() {
		StringBuilder string = __stringPool.get();
		
		assertNotEquals(null, string);
	}

	@Test
	public void repayAStringBuilderWithoutGetShouldCauseException() {
		assertThrows(NullElementPoolException.class, () -> {
			StringBuilder string = new StringBuilder();
			__stringPool.repay(string);
		});
	}

	@Test
	public void afterReplayStringShouldBeClearedAllData() {
		StringBuilder string = __stringPool.get();
		string.append("a").append("b").append("c");
		__stringPool.repay(string);
		
		assertEquals(0, string.length());
	}

	@Test
	public void createNumberOfElementsShouldLessThanPoolSize() {
		int numberElement = 100;
		for (int i = 0; i < numberElement; i++) {
			__stringPool.get();
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
		
		assertAll("createNumberOfElements", () -> assertEquals(expected, __stringPool.getPoolSize()),
				() -> assertTrue(__stringPool.getPoolSize() > numberElement));
	}

}
