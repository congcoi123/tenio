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
import java.util.Map;

/**
 * This class holds data by pairs of key and value, works like a map.
 */
public interface ZeroMap extends ReadonlyZeroMap, Iterable<Map.Entry<String, ZeroElement>> {

  /**
   * Removes an element by using its key.
   *
   * @param key the {@link String} key of element should be removed
   * @return <code>true</code> if the action is successful, otherwise <code>false</code>
   */
  boolean removeElement(String key);

  /**
   * Puts a <code>null</code> value into the map.
   *
   * @param key the {@link String} key of element
   * @return the pointer of this instance
   */
  ZeroMap putNull(String key);

  /**
   * Puts a <code>boolean</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putBoolean(String key, boolean data);

  /**
   * Puts a <code>byte</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putByte(String key, byte data);

  /**
   * Puts a <code>short</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putShort(String key, short data);

  /**
   * Puts a <code>integer</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putInteger(String key, int data);

  /**
   * Puts a <code>long</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putLong(String key, long data);

  /**
   * Puts a <code>float</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putFloat(String key, float data);

  /**
   * Puts a <code>double</code> value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putDouble(String key, double data);

  /**
   * Puts a {@link String} value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putString(String key, String data);

  /**
   * Puts a {@link ZeroArray} value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putZeroArray(String key, ZeroArray data);

  /**
   * Puts a {@link ZeroMap} value into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putZeroMap(String key, ZeroMap data);

  /**
   * Puts a {@link ZeroElement} into the map.
   *
   * @param key     the {@link String} key of element
   * @param element the inserted data
   * @return the pointer of this instance
   */
  ZeroMap putZeroElement(String key, ZeroElement element);

  /**
   * Puts a collection of {@link Boolean} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putBooleanArray(String key, Collection<Boolean> data);

  /**
   * Puts an array of binaries into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted <code>byte[]</code> data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putByteArray(String key, byte[] data);

  /**
   * Puts a collection of {@link Short} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putShortArray(String key, Collection<Short> data);

  /**
   * Puts a collection of {@link Integer} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putIntegerArray(String key, Collection<Integer> data);

  /**
   * Puts a collection of {@link Long} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putLongArray(String key, Collection<Long> data);

  /**
   * Puts a collection of {@link Float} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putFloatArray(String key, Collection<Float> data);

  /**
   * Puts a collection of {@link Double} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putDoubleArray(String key, Collection<Double> data);

  /**
   * Puts a collection of {@link String} values into the map.
   *
   * @param key  the {@link String} key of element
   * @param data the inserted data
   * @return the pointer of this instance
   * @see Collection
   */
  ZeroMap putStringArray(String key, Collection<String> data);

  /**
   * Retrieves a new map in read-only mode.
   *
   * @return a read-only map {@link ReadonlyZeroMap}
   */
  ReadonlyZeroMap getReadonlyZeroMap();
}
