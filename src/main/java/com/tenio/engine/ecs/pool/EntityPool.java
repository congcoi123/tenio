/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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
import com.tenio.engine.ecs.basis.Entity;
import com.tenio.engine.ecs.basis.implement.ContextInfo;
import com.tenio.engine.ecs.basis.implement.EntityImpl;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.concurrent.GuardedBy;

/**
 * The object pool mechanism for {@link Entity}.
 */
public final class EntityPool extends SystemLogger implements ElementPool<Entity> {

  private final Class<? extends EntityImpl> clazz;
  private final ContextInfo contextInfo;
  @GuardedBy("this")
  private Entity[] pool;
  @GuardedBy("this")
  private boolean[] used;

  /**
   * Initialization.
   *
   * @param clazz       the class of element
   * @param contextInfo the context information
   */
  public EntityPool(Class<? extends EntityImpl> clazz, ContextInfo contextInfo) {
    this.clazz = clazz;
    this.contextInfo = contextInfo;
    pool = new Entity[CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL];
    used = new boolean[CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL];

    for (int i = 0; i < pool.length; i++) {
      try {
        var entity = this.clazz.getDeclaredConstructor().newInstance();
        entity.setId(UUID.randomUUID().toString());
        entity.setContextInfo(this.contextInfo);
        pool[i] = entity;
        used[i] = false;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException exception) {
        error(exception);
      }
    }
  }

  @Override
  public synchronized Entity get() {
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
    pool = new Entity[oldPool.length + CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL];
    System.arraycopy(oldPool, 0, pool, 0, oldPool.length);

    for (int i = oldPool.length; i < pool.length; i++) {
      try {
        var entity = clazz.getDeclaredConstructor().newInstance();
        entity.setId(UUID.randomUUID().toString());
        entity.setContextInfo(contextInfo);
        pool[i] = entity;
        used[i] = false;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException exception) {
        error(exception);
      }
    }

    info("COMPONENT POOL", buildgen("Increase the number of elements by ",
        CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL, " to ", used.length));

    // and allocate the last old element
    used[oldPool.length - 1] = true;
    return pool[oldPool.length - 1];
  }

  @Override
  public synchronized void repay(Entity element) {
    boolean flagFound = false;
    for (int i = 0; i < pool.length; i++) {
      if (pool[i] == element) {
        used[i] = false;
        element.reset();
        flagFound = true;
        break;
      }
    }
    if (!flagFound) {
      var exception = new NullElementPoolException(element.toString());
      error(exception);
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
