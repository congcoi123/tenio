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

package com.tenio.engine.ecs.pool;

import com.tenio.common.constant.CommonConstant;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.pool.ElementPool;
import com.tenio.engine.ecs.basis.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.annotation.concurrent.GuardedBy;

/**
 * The object pool mechanism for {@link Component}.
 */
public final class ComponentPool extends SystemLogger implements ElementPool<Component> {

  private final Class<?> clazz;
  @GuardedBy("this")
  private Component[] pool;
  @GuardedBy("this")
  private boolean[] used;

  /**
   * Initialization.
   *
   * @param clazz the class of element
   */
  public ComponentPool(Class<?> clazz) {
    this.clazz = clazz;
    pool = new Component[CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL];
    used = new boolean[CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL];

    for (int i = 0; i < pool.length; i++) {
      try {
        var component = (Component) this.clazz.getDeclaredConstructor().newInstance();
        pool[i] = component;
        used[i] = false;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException exception) {
        if (isErrorEnabled()) {
          error(exception);
        }
      }
    }
  }

  @Override
  public synchronized Component get() {
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
    pool = new Component[oldPool.length + CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL];
    System.arraycopy(oldPool, 0, pool, 0, oldPool.length);

    for (int i = oldPool.length; i < pool.length; i++) {
      try {
        pool[i] = (Component) clazz.getDeclaredConstructor().newInstance();
        used[i] = false;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException exception) {
        if (isErrorEnabled()) {
          error(exception);
        }
      }
    }

    if (isInfoEnabled()) {
      info("COMPONENT POOL", buildgen("Increase the number of elements by ",
          CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL, " to ", used.length));
    }

    // and allocate the last old ELement
    used[oldPool.length - 1] = true;
    return pool[oldPool.length - 1];
  }

  @Override
  public synchronized void repay(Component element) {
    boolean flagFound = false;
    for (int i = 0; i < pool.length; i++) {
      if (pool[i] == element) {
        used[i] = false;
        flagFound = true;
        break;
      }
    }
    if (!flagFound) {
      var exception = new NullElementPoolException(element.toString());
      if (isErrorEnabled()) {
        error(exception);
      }
      throw exception;
    }
  }

  @Override
  public synchronized void cleanup() {
    Arrays.fill(pool, null);
    used = null;
    pool = null;
  }

  @Override
  public synchronized int getPoolSize() {
    return (pool.length == used.length) ? pool.length : -1;
  }

  @Override
  public int getAvailableSlot() {
    int slot = 0;
    for (boolean b : used) {
      if (!b) {
        slot++;
      }
    }

    return slot;
  }
}
