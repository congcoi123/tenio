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

package com.tenio.common.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.constant.CommonConstant;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.pool.StringBuilderPool;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For String Builder Pool")
class StringBuilderPoolTest {

  @BeforeEach
  void initialization() {
    StringBuilderPool.getInstance().cleanup();
  }

  @Test
  @DisplayName("To be able to get a builder instance from pool")
  void getBuilderFromPoolShouldWork() {
    var builder = StringBuilderPool.getInstance().get();
    assertNotNull(builder);
  }

  @Test
  @DisplayName("After retrieving a builder, allow to pay it back to the pool")
  void getAndRePayBuilderShouldWork() {
    var pool = StringBuilderPool.getInstance();
    var sizeBefore = pool.getAvailableSlot();
    var builder = pool.get();
    pool.repay(builder);
    var sizeAfter = pool.getAvailableSlot();

    assertEquals(sizeBefore, sizeAfter);
  }

  @Test
  @DisplayName("Taking builder from pool should decrease the pool size as expected")
  void poolSizeDecreaseShouldBeAsExpected() {
    var pool = StringBuilderPool.getInstance();
    var sizeBefore = pool.getAvailableSlot();
    var takenNumber = 10;
    for (int i = 0; i < takenNumber; i++) {
      pool.get();
    }
    var sizeAfter = pool.getAvailableSlot();
    var sizeExpected = sizeBefore - takenNumber;
    assertEquals(sizeExpected, sizeAfter);
  }

  @Test
  @DisplayName("When retrieving the instances reaches threshold, it automatically expands the pool size")
  void poolSizeIncreaseShouldBeAsExpected() {
    var pool = StringBuilderPool.getInstance();
    var takenNumber = CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL + 1;
    for (int i = 0; i < takenNumber; i++) {
      pool.get();
    }
    var sizeAfter = pool.getPoolSize();
    var sizeExpected =
        CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL +
            CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL;

    assertEquals(sizeExpected, sizeAfter);
  }

  @Test
  @DisplayName("Clear the pool should free all slots and reset the pool size")
  void clearPoolShouldReturnTheAvailableSlotsEqualsSize() {
    var pool = StringBuilderPool.getInstance();
    var takenNumber = 10;
    for (int i = 0; i < takenNumber; i++) {
      pool.get();
    }
    pool.cleanup();

    assertEquals(pool.getAvailableSlot(), pool.getPoolSize());
  }

  @Test
  @DisplayName("Attempt to repay an invalid builder instance should throw an exception")
  void repayAnInvalidElementShouldThrowAnException() {
    var pool = StringBuilderPool.getInstance();

    assertThrows(NullElementPoolException.class, () -> pool.repay(new StringBuilder()));
  }

  @Test
  @DisplayName("Pool expansion and error logging should work when logging is enabled")
  void poolLoggingBranchesShouldBeReachableWithLoggingEnabled() {
    Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.INFO);
    try {
      var pool = StringBuilderPool.getInstance();
      pool.cleanup();
      for (int i = 0; i <= CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL; i++) {
        pool.get();
      }
      assertTrue(pool.getPoolSize() > CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL);
      assertThrows(NullElementPoolException.class, () -> pool.repay(new StringBuilder()));
    } finally {
      Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.OFF);
      StringBuilderPool.getInstance().cleanup();
    }
  }

  @Test
  void builderBorrowedBeforeCleanupCanBeReturnedOnce() {
    var pool = StringBuilderPool.getInstance();
    var builder = pool.get();
    builder.append("old contents");

    pool.cleanup();
    pool.cleanup();
    pool.repay(builder);

    assertEquals(0, builder.length());
    assertEquals(CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL, pool.getPoolSize());
    assertEquals(pool.getPoolSize(), pool.getAvailableSlot());
    assertThrows(NullElementPoolException.class, () -> pool.repay(builder));
  }

  @Test
  void cleanupDoesNotReuseOutstandingBuilder() {
    var pool = StringBuilderPool.getInstance();
    var outstanding = pool.get();
    pool.cleanup();
    var borrowed = new ArrayList<StringBuilder>();
    try {
      for (int index = 0; index < CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL + 1; index++) {
        var builder = pool.get();
        assertFalse(builder == outstanding);
        borrowed.add(builder);
      }
    } finally {
      borrowed.forEach(pool::repay);
      pool.repay(outstanding);
    }
  }

  @Test
  void concurrentCleanupBorrowingAndReturnsKeepOwnershipConsistent() throws Exception {
    var pool = StringBuilderPool.getInstance();
    Set<StringBuilder> inUse = ConcurrentHashMap.newKeySet();
    var start = new CountDownLatch(1);
    var workers = Executors.newFixedThreadPool(9);
    var futures = new ArrayList<Future<?>>();
    try {
      for (int worker = 0; worker < 8; worker++) {
        futures.add(workers.submit(() -> {
          assertTrue(start.await(5, TimeUnit.SECONDS));
          for (int iteration = 0; iteration < 1000; iteration++) {
            var builder = pool.get();
            assertTrue(inUse.add(builder), "Builder borrowed concurrently");
            try {
              assertEquals(0, builder.length());
              builder.append("owned by ").append(Thread.currentThread().getName());
            } finally {
              assertTrue(inUse.remove(builder));
              pool.repay(builder);
            }
          }
          return null;
        }));
      }
      futures.add(workers.submit(() -> {
        assertTrue(start.await(5, TimeUnit.SECONDS));
        for (int iteration = 0; iteration < 1000; iteration++) {
          pool.cleanup();
        }
        return null;
      }));
      start.countDown();
      for (var future : futures) {
        future.get(15, TimeUnit.SECONDS);
      }
      assertTrue(inUse.isEmpty());
      assertEquals(pool.getPoolSize(), pool.getAvailableSlot());
    } finally {
      start.countDown();
      workers.shutdownNow();
      assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
    }
  }

  @Test
  void nullAndDuplicateReturnsAreRejected() {
    var pool = StringBuilderPool.getInstance();
    var builder = pool.get();
    pool.repay(builder);

    assertThrows(NullElementPoolException.class, () -> pool.repay(builder));
    assertThrows(NullElementPoolException.class, () -> pool.repay(null));
  }

  @Test
  void oversizedBuilderIsReplacedOnReturn() {
    var pool = StringBuilderPool.getInstance();
    var builder = pool.get();
    builder.ensureCapacity(16 * 1024);
    pool.repay(builder);
    var borrowed = new ArrayList<StringBuilder>();
    try {
      for (int index = 0; index < CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL; index++) {
        var next = pool.get();
        assertFalse(next == builder);
        assertTrue(next.capacity() <= 8 * 1024);
        borrowed.add(next);
      }
    } finally {
      borrowed.forEach(pool::repay);
    }
  }
}
