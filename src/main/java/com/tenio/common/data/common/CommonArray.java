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

package com.tenio.common.data.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an element array class holds data in a map. All message comes from other services
 * will be converted to this object. That helps normalize the way to communicate and be easy to use.
 */
public final class CommonArray extends ArrayList<Object> implements Serializable {

  private static final long serialVersionUID = -5100842875580575666L;

  /**
   * Create a fresh instance.
   */
  public CommonArray() {
  }

  /**
   * Retrieves new instance of the class.
   *
   * @return an instance
   */
  public static CommonArray newInstance() {
    return new CommonArray();
  }

  /**
   * Appends a new value into the array.
   *
   * @param value the data value in common {@link Object} type
   * @return the pointer of this instance
   */
  public CommonArray put(Object value) {
    add(value);
    return this;
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>double</code> type at index in the array
   */
  public double getDouble(int index) {
    return (double) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>float</code> type at index in the array
   */
  public float getFloat(int index) {
    return (float) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>long</code> type at index in the array
   */
  public long getLong(int index) {
    return (long) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>integer</code> type at index in the array
   */
  public int getInt(int index) {
    return (int) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>boolean</code> type at index in the array
   */
  public boolean getBoolean(int index) {
    return (boolean) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in {@link String} type at index in the array
   */
  public String getString(int index) {
    return (String) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in {@link Object} type at index in the array
   */
  public Object getObject(int index) {
    return get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in {@link CommonArray} type at index in the array
   */
  public CommonArray getCommonObjectArray(int index) {
    return (CommonArray) get(index);
  }

  /**
   * Retrieves a new copy of the interior array.
   *
   * @return a new copy of the array in unmodified mode.
   */
  public List<Object> getReadonlyList() {
    return List.copyOf(this);
  }
}
