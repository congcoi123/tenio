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

import com.tenio.common.data.DataCollection;
import java.util.Collection;

/**
 * This class holds sequence elements in read-only mode.
 */
public interface ReadonlyZeroArray extends DataCollection {

  /**
   * Determines whether a value is available in array.
   *
   * @param element the {@link Object} value in determination
   * @return <code>true</code> if the value is existed, otherwise <code>false</code>
   */
  boolean contains(Object element);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in common {@link Object} type
   */
  Object getDataForElementAt(int index);

  /**
   * Determines whether a value at an index is {@link ZeroType#NULL}.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return <code>true</code> if the value equals to {@link ZeroType#NULL} type, otherwise
   * <code>false</code>
   * @see ZeroType
   */
  boolean isNull(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link Boolean} type
   */
  Boolean getBoolean(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code>  index needs to be checked
   * @return the value held at the index in {@link Byte} type
   */
  Byte getByte(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link Short} type
   */
  Short getShort(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link Integer} type
   */
  Integer getInteger(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link Long} type
   */
  Long getLong(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link Float} type
   */
  Float getFloat(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link Double} type
   */
  Double getDouble(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link String} type
   */
  String getString(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link ZeroArray} type
   */
  ZeroArray getZeroArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link ZeroMap} type
   */
  ZeroMap getZeroMap(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in {@link ZeroElement} type
   */
  ZeroElement getZeroElement(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link Boolean} type
   * @see Collection
   */
  Collection<Boolean> getBooleanArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in an array of binaries
   */
  byte[] getByteArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link Short} type
   * @see Collection
   */
  Collection<Short> getShortArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link Integer} type
   * @see Collection
   */
  Collection<Integer> getIntegerArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link Long} type
   * @see Collection
   */
  Collection<Long> getLongArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link Float} type
   * @see Collection
   */
  Collection<Float> getFloatArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link Double} type
   * @see Collection
   */
  Collection<Double> getDoubleArray(int index);

  /**
   * Retrieves the data of element at index in the array.
   *
   * @param index the <code>integer</code> index needs to be checked
   * @return the value held at the index in a collection of {@link String} type
   * @see Collection
   */
  Collection<String> getStringArray(int index);
}
