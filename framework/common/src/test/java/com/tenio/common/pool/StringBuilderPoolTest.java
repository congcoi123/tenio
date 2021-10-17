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

package com.tenio.common.pool;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.pool.StringBuilderPool;
import org.junit.jupiter.api.Test;

public final class StringBuilderPoolTest {

  @Test
  public void getBuilderFromPoolShouldWork() {
    var builder = StringBuilderPool.getInstance().get();
    assertTrue(builder instanceof StringBuilder);
  }

  @Test
  public void getAndPayBuilderShouldWork() {
    var pool = StringBuilderPool.getInstance();
    var sizeBefore = pool.getAvailableSlot();
    var builder = pool.get();
    pool.repay(builder);
    var sizeAfter = pool.getAvailableSlot();

    assertTrue(sizeBefore == sizeAfter);
  }

  @Test
  public void poolSizeDecreaseShouldBeAsExpected() {
    var pool = StringBuilderPool.getInstance();
    var sizeBefore = pool.getAvailableSlot();
    var takenNumber = 10;
    for (int i = 0; i < takenNumber; i++) {
      pool.get();
    }
    var sizeAfter = pool.getAvailableSlot();

    assertTrue(sizeAfter == (sizeBefore - takenNumber));
  }

  @Test
  public void clearPoolShouldReturnTheAvailableSlotsEqualsSize() {
    var pool = StringBuilderPool.getInstance();
    var takenNumber = 10;
    for (int i = 0; i < takenNumber; i++) {
      pool.get();
    }
    pool.cleanup();

    assertTrue(pool.getAvailableSlot() == pool.getPoolSize());
  }

  @Test
  public void repayAnInvalidElementShouldThrowAnException() {
    var pool = StringBuilderPool.getInstance();

    assertThrows(NullElementPoolException.class, () -> {
      pool.repay(new StringBuilder());
    });
  }
}
