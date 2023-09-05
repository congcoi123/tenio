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

package com.tenio.common.constant;

import com.tenio.common.pool.ElementPool;

/**
 * All base constants' values for the server are defined here.
 */
public final class CommonConstant {

  /**
   * The number of elements in a bulk those created for the first time.
   *
   * @see ElementPool
   */
  public static final int DEFAULT_NUMBER_ELEMENTS_POOL = 64;
  /**
   * When the desired number of elements exceeded the first configuration, the new number of
   * elements will be added.
   *
   * @see ElementPool
   */
  public static final int ADDITIONAL_NUMBER_ELEMENTS_POOL = 32;

  private CommonConstant() {
    throw new UnsupportedOperationException("This class doesn't support to create a new instance");
  }
}
