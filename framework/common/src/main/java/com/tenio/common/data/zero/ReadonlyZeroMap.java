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
import java.util.Set;

/**
 * This class holds data by pairs of key and value, works like a map in read-only mode.
 */
public interface ReadonlyZeroMap extends DataCollection {

  /**
   * Determines whether a value in the map is {@link ZeroType#NULL} type.
   *
   * @param key the {@link String} key used to fetch value
   * @return <code>true</code> if an element is available, and it equals to
   * {@link ZeroType#NULL} type, otherwise <code>false</code>
   * @see ZeroType
   */
  boolean isNull(String key);

  /**
   * Determines whether a value is available in map.
   *
   * @param key the {@link String} key for checking
   * @return <code>true</code> if the key is found, otherwise <code>false</code>
   */
  boolean containsKey(String key);

  /**
   * Retrieves a set of keys in map.
   *
   * @return a {@link Set} of keys in map.
   */
  Set<String> getKeys();

  /**
   * Retrieves an unmodifiable set of keys in map.
   *
   * @return an unmodifiable {@link Set} of keys in map.
   */
  Set<String> getReadonlyKeys();

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link Boolean} type
   */
  Boolean getBoolean(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link Byte} type
   */
  Byte getByte(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link Short} type
   */
  Short getShort(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String}  key needs to be checked
   * @return the value held fetched by its key in {@link Integer} type
   */
  Integer getInteger(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link Long} type
   */
  Long getLong(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link Float} type
   */
  Float getFloat(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link Double} type
   */
  Double getDouble(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link String} type
   */
  String getString(String key);

  /**
   * Retrieves the data of element by its key in the generic data collection type.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link DataCollection} type
   * @since 0.5.0
   */
  DataCollection getDataCollection(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link ZeroArray} type
   */
  ZeroArray getZeroArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link ZeroMap} type
   */
  ZeroMap getZeroMap(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held fetched by its key in {@link ZeroElement} type
   */
  ZeroElement getZeroElement(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in the map of {@link Boolean} type
   * @see Collection
   */
  Collection<Boolean> getBooleanArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in a map of an array of binaries
   */
  byte[] getByteArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the index in a collection of {@link Short} type
   * @see Collection
   */
  Collection<Short> getShortArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in  the map of {@link Integer} type
   * @see Collection
   */
  Collection<Integer> getIntegerArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in the map of {@link Long} type
   * @see Collection
   */
  Collection<Long> getLongArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in the map of {@link Float} type
   * @see Collection
   */
  Collection<Float> getFloatArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in the map of {@link Double} type
   * @see Collection
   */
  Collection<Double> getDoubleArray(String key);

  /**
   * Retrieves the data of element by its key in the map.
   *
   * @param key the {@link String} key needs to be checked
   * @return the value held at the key in the map of {@link String} type
   * @see Collection
   */
  Collection<String> getStringArray(String key);
}
