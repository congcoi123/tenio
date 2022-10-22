/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.common.logger.pool;

import com.google.common.base.Throwables;
import com.tenio.common.constant.CommonConstant;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.pool.ElementPool;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The object pool mechanism for {@link StringBuilder}.
 */
@ThreadSafe
public final class StringBuilderPool implements ElementPool<StringBuilder> {

  private static volatile StringBuilderPool instance;
  private final Logger logger = LogManager.getLogger(getClass());

  @GuardedBy("this")
  private StringBuilder[] pool;
  @GuardedBy("this")
  private boolean[] used;

  private StringBuilderPool() {
    initialization();
  }

  /**
   * Preventing Singleton object instantiation from outside creates multiple instance if two
   * thread access this method simultaneously.
   *
   * @return the instance of this class
   */
  public static StringBuilderPool getInstance() {
    var reference = instance;
    if (reference == null) {
      synchronized (StringBuilderPool.class) {
        reference = instance;
        if (reference == null) {
          instance = reference = new StringBuilderPool();
        }
      }
    }
    return reference;
  }

  private void initialization() {
    pool = new StringBuilder[CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL];
    used = new boolean[CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL];

    for (int i = 0; i < pool.length; i++) {
      pool[i] = new StringBuilder();
      used[i] = false;
    }
  }

  @Override
  public synchronized StringBuilder get() {
    for (int i = 0; i < used.length; i++) {
      if (!used[i]) {
        used[i] = true;
        return pool[i];
      }
    }
    // If we got here, then all the Elements are in use. We will
    // increase the number in our pool by @ADD_ELEMENT_POOL (arbitrary value for
    // illustration purposes).
    var oldUsed = used;
    used = new boolean[oldUsed.length + CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL];
    System.arraycopy(oldUsed, 0, used, 0, oldUsed.length);

    var oldPool = pool;
    pool = new StringBuilder[oldPool.length + CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL];
    System.arraycopy(oldPool, 0, pool, 0, oldPool.length);

    for (int i = oldPool.length; i < pool.length; i++) {
      pool[i] = new StringBuilder();
      used[i] = false;
    }

    infoWithoutPool(strgen("Increased the number of elements by ",
        CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL, " to ", used.length));

    // and allocate the last old element
    used[oldPool.length - 1] = true;
    return pool[oldPool.length - 1];
  }

  @Override
  public synchronized void repay(StringBuilder element) {
    var flagFound = false;
    for (int i = 0; i < pool.length; i++) {
      if (pool[i] == element) {
        used[i] = false;
        // clear the array
        element.setLength(0);
        flagFound = true;
        break;
      }
    }
    if (!flagFound) {
      var e = new NullElementPoolException(element.toString());
      errorWithoutPool(e);
      throw e;
    }
  }

  @Override
  public synchronized void cleanup() {
    for (int i = 0; i < pool.length; i++) {
      pool[i] = null;
      used[i] = false;
    }

    initialization();
  }

  @Override
  public synchronized int getPoolSize() {
    return (pool.length == used.length) ? pool.length : -1;
  }

  @Override
  public synchronized int getAvailableSlot() {
    int slot = 0;
    for (boolean b : used) {
      if (!b) {
        slot++;
      }
    }

    return slot;
  }

  /**
   * Only use for {@link StringBuilderPool}. It might cause out of memory, so be
   * careful if you use it. You are warned!
   *
   * @param msg the message content
   */
  private void infoWithoutPool(String msg) {
    if (!logger.isInfoEnabled()) {
      return;
    }
    logger.info("[STRINGBUILDER POOL] " + msg);
  }

  /**
   * Only use for {@link StringBuilderPool}. It might cause out of memory, so be
   * careful if you use it. You are warned!
   *
   * @param cause the throwable
   */
  private void errorWithoutPool(Throwable cause) {
    if (!logger.isErrorEnabled()) {
      return;
    }
    logger.error(Throwables.getStackTraceAsString(cause));
  }

  /**
   * To generate {@code String} for logging information by the corresponding
   * objects.
   *
   * @param objects the corresponding objects, see {@link Object}
   * @return a string value
   */
  private String strgen(Object... objects) {
    var builder = new StringBuilder();
    for (var object : objects) {
      builder.append(object);
    }
    return builder.toString();
  }
}
