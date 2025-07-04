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

package com.tenio.common.pool;

import com.tenio.common.exception.NullElementPoolException;

/**
 * In an application, you can have resources that are limited or time-consuming
 * to create a new one. A solution is to create a limited resource once and
 * reuse it. The object pool design will have the mechanism to create a bulk of
 * objects to pooling use. If the requirements of resources increases, the
 * current bulk's size will be also automatically increased.
 */
public interface ElementPool<T> {

  /**
   * Retrieves an element in the current pool.
   *
   * @return an element in the pool
   */
  T get();

  /**
   * When you finished using an element, repay (free) it for the reusing.
   *
   * @param element the finished using element
   * @throws NullElementPoolException when you try to repay an invalid element
   */
  void repay(T element) throws NullElementPoolException;

  /**
   * Clean up, after that all array elements will be set to <b>null</b>.
   */
  void cleanup();

  /**
   * Retrieves the pool size.
   *
   * @return the total number of element or <b>-1</b> if any exceptions caused
   */
  int getPoolSize();

  /**
   * Retrieves the current available slots.
   *
   * @return the number of available slots in the pool
   */
  int getAvailableSlot();
}
