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

package com.tenio.common.logger.pool;

import com.tenio.common.constant.CommonConstant;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.pool.ElementPool;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The object pool mechanism for {@link StringBuilder}.
 */
public final class StringBuilderPool implements ElementPool<StringBuilder> {

  private static volatile StringBuilderPool instance;
  private static final int MAX_RETAINED_CAPACITY = 8 * 1024;

  private final Queue<StringBuilder> available;
  private final Set<StringBuilder> borrowed;
  /** Weak keys allow abandoned builders from earlier pool generations to be collected. */
  private final Map<StringBuilder, Boolean> retired;

  private StringBuilderPool() {
    available = new ArrayDeque<>();
    borrowed = new HashSet<>();
    retired = new WeakHashMap<>();
    refill(CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL);
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

  private void refill(int count) {
    for (int i = 0; i < count; i++) {
      available.offer(new StringBuilder());
    }
  }

  @Override
  public synchronized StringBuilder get() {
    if (available.isEmpty()) {
      refill(CommonConstant.ADDITIONAL_NUMBER_ELEMENTS_POOL);
    }
    var element = available.remove();
    borrowed.add(element);
    return element;
  }

  @Override
  public synchronized void repay(StringBuilder element) {
    if (element == null) {
      throw new NullElementPoolException("null");
    }
    if (!borrowed.remove(element)) {
      if (retired.remove(element) != null) {
        element.setLength(0);
        return;
      }
      throw new NullElementPoolException(String.valueOf(element));
    }
    if (element.capacity() > MAX_RETAINED_CAPACITY) {
      available.offer(new StringBuilder());
    } else {
      element.setLength(0);
      available.offer(element);
    }
  }

  /**
   * Resets idle capacity while allowing outstanding builders to be returned once.
   * Builders borrowed before cleanup are discarded when returned.
   */
  @Override
  public synchronized void cleanup() {
    available.clear();
    for (var builder : borrowed) {
      retired.put(builder, Boolean.TRUE);
    }
    borrowed.clear();
    refill(CommonConstant.DEFAULT_NUMBER_ELEMENTS_POOL);
  }

  @Override
  public synchronized int getPoolSize() {
    return available.size() + borrowed.size();
  }

  @Override
  public synchronized int getAvailableSlot() {
    return available.size();
  }
}
