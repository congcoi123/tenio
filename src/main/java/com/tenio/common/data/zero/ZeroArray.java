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

package com.tenio.common.data.zero;

import java.util.Collection;

/**
 * This class holds sequence elements.
 */
public interface ZeroArray extends ReadonlyZeroArray, Iterable<ZeroElement> {

  /**
   * Removes an element at an <code>integer</code> index.
   *
   * @param index the index of element should be removed
   */
  void removeElementAt(int index);

  /**
   * Appends a <code>null</code> value into the array.
   *
   * @return the pointer of this instance
   */
  ZeroArray addNull();

  /**
   * Appends a <code>boolean</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addBoolean(boolean data);

  /**
   * Appends a <code>byte</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addByte(byte data);

  /**
   * Appends a <code>short</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addShort(short data);

  /**
   * Appends a <code>integer</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addInteger(int data);

  /**
   * Appends a <code>long</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addLong(long data);

  /**
   * Appends a <code>float</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addFloat(float data);

  /**
   * Appends a <code>double</code> value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addDouble(double data);

  /**
   * Appends a {@link String} value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addString(String data);

  /**
   * Appends a {@link ZeroArray} value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addZeroArray(ZeroArray data);

  /**
   * Appends a {@link ZeroMap} value into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   */
  ZeroArray addZeroMap(ZeroMap data);

  /**
   * Appends a {@link ZeroElement} into the array.
   *
   * @param element the appended data
   * @return the pointer of this instance
   */
  ZeroArray addZeroElement(ZeroElement element);

  /**
   * Appends a collection of {@link Boolean} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addBooleanArray(Collection<Boolean> data);

  /**
   * Appends an array of binaries into the array.
   *
   * @param data the appended <code>byte[]</code> data
   * @return the pointer of this instance
   */
  ZeroArray addByteArray(byte[] data);

  /**
   * Appends a collection of {@link Short} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addShortArray(Collection<Short> data);

  /**
   * Appends a collection of {@link Integer} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addIntegerArray(Collection<Integer> data);

  /**
   * Appends a collection of {@link Long} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addLongArray(Collection<Long> data);

  /**
   * Appends a collection of {@link Float} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addFloatArray(Collection<Float> data);

  /**
   * Appends a collection of {@link Double} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addDoubleArray(Collection<Double> data);

  /**
   * Appends a collection of {@link String} values into the array.
   *
   * @param data the appended data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroArray addStringArray(Collection<String> data);

  /**
   * Retrieves a new array in read-only mode.
   *
   * @return a read-only array {@link ReadonlyZeroArray}
   */
  ReadonlyZeroArray getReadonlyZeroArray();
}
