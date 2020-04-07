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
package com.tenio.message;

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
import com.tenio.message.codec.ByteArrayInputStream;
import com.tenio.message.pool.ByteArrayInputStreamPool;
import com.tenio.pool.IElementPool;

/**
 * @author kong
 */
public final class ByteArrayInputStreamPoolTest {

	private IElementPool<ByteArrayInputStream> __byteArrayPool;

	@BeforeEach
	public void initialize() {
		__byteArrayPool = new ByteArrayInputStreamPool();
	}

	@AfterEach
	public void tearDown() {
		__byteArrayPool.cleanup();
	}

	@Test
	public void createNewByteArrayShouldReturnSuccess() {
		ByteArrayInputStream array = __byteArrayPool.get();
		assertNotEquals(null, array);
	}

	@Test
	public void repayAnByteArrayWithoutGetShouldCauseException() {
		assertThrows(NullElementPoolException.class, () -> {
			ByteArrayInputStream array = ByteArrayInputStream.newInstance();
			__byteArrayPool.repay(array);
		});
	}

	@Test
	public void createNumberOfElementsShouldLessThanPoolSize() {
		int numberElement = 100;
		for (int i = 0; i < numberElement; i++) {
			__byteArrayPool.get();
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
		assertAll("createNumberOfElements", () -> assertEquals(expected, __byteArrayPool.getPoolSize()),
				() -> assertTrue(__byteArrayPool.getPoolSize() > numberElement));
	}

}
