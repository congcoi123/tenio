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

package com.tenio.common.data.zero.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.zero.ReadonlyZeroMap;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroElement;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.ZeroType;
import com.tenio.common.data.zero.utility.ZeroUtility;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * An implementation for the zero object.
 */
public final class ZeroMapImpl implements ZeroMap {

  private final Map<String, ZeroElement> map;

  /**
   * Creates a new instance.
   */
  public ZeroMapImpl() {
    map = new HashMap<>();
  }

  private ZeroMapImpl(Map<String, ZeroElement> map) {
    this.map = map;
  }

  @Override
  public byte[] toBinary() {
    return ZeroUtility.mapToBinary(this);
  }

  @Override
  public boolean isNull(String key) {
    var element = getZeroElement(key);
    return Objects.nonNull(element) && element.getType() == ZeroType.NULL;
  }

  @Override
  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  @Override
  public boolean removeElement(String key) {
    return map.remove(key) != null;
  }

  @Override
  public Set<String> getKeys() {
    return map.keySet();
  }

  @Override
  public Set<String> getReadonlyKeys() {
    return Set.copyOf(map.keySet());
  }

  @Override
  public int size() {
    return map.size();
  }

  /**
   * This method potentially creates an issue called "escape references". Please be aware of
   * using it
   * properly.
   *
   * @return an iterator object for the interior array.
   * @see Iterator
   */
  @Override
  @Nonnull
  public Iterator<Entry<String, ZeroElement>> iterator() {
    return map.entrySet().iterator();
  }

  @Override
  public Boolean getBoolean(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Boolean) element.getData();
  }

  @Override
  public Byte getByte(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Byte) element.getData();
  }

  @Override
  public Short getShort(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Short) element.getData();
  }

  @Override
  public Integer getInteger(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Integer) element.getData();
  }

  @Override
  public Long getLong(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Long) element.getData();
  }

  @Override
  public Float getFloat(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Float) element.getData();
  }

  @Override
  public Double getDouble(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Double) element.getData();
  }

  @Override
  public String getString(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (String) element.getData();
  }

  @Override
  public DataCollection getDataCollection(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (DataCollection) element.getData();
  }

  @Override
  public ZeroArray getZeroArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (ZeroArray) element.getData();
  }

  @Override
  public ZeroMap getZeroMap(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (ZeroMap) element.getData();
  }

  @Override
  public ZeroElement getZeroElement(String key) {
    return map.get(key);
  }

  @Override
  public ZeroMap putNull(String key) {
    return putElement(key, ZeroType.NULL, null);
  }

  @Override
  public ZeroMap putBoolean(String key, boolean data) {
    return putElement(key, ZeroType.BOOLEAN, data);
  }

  @Override
  public ZeroMap putByte(String key, byte data) {
    return putElement(key, ZeroType.BYTE, data);
  }

  @Override
  public ZeroMap putShort(String key, short data) {
    return putElement(key, ZeroType.SHORT, data);
  }

  @Override
  public ZeroMap putInteger(String key, int data) {
    return putElement(key, ZeroType.INTEGER, data);
  }

  @Override
  public ZeroMap putLong(String key, long data) {
    return putElement(key, ZeroType.LONG, data);
  }

  @Override
  public ZeroMap putFloat(String key, float data) {
    return putElement(key, ZeroType.FLOAT, data);
  }

  @Override
  public ZeroMap putDouble(String key, double data) {
    return putElement(key, ZeroType.DOUBLE, data);
  }

  @Override
  public ZeroMap putString(String key, String data) {
    return putElement(key, ZeroType.STRING, data);
  }

  @Override
  public ZeroMap putZeroArray(String key, ZeroArray data) {
    return putElement(key, ZeroType.ZERO_ARRAY, data);
  }

  @Override
  public ZeroMap putZeroMap(String key, ZeroMap data) {
    return putElement(key, ZeroType.ZERO_MAP, data);
  }

  @Override
  public ZeroMap putZeroElement(String key, ZeroElement element) {
    map.put(key, element);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Boolean> getBooleanArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<Boolean>) element.getData();
  }

  @Override
  public byte[] getByteArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (byte[]) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Short> getShortArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<Short>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Integer> getIntegerArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<Integer>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Long> getLongArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<Long>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Float> getFloatArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<Float>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Double> getDoubleArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<Double>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<String> getStringArray(String key) {
    var element = getZeroElement(key);
    return Objects.isNull(element) ? null : (Collection<String>) element.getData();
  }

  @Override
  public ZeroMap putBooleanArray(String key, Collection<Boolean> data) {
    return putElement(key, ZeroType.BOOLEAN_ARRAY, data);
  }

  @Override
  public ZeroMap putByteArray(String key, byte[] data) {
    return putElement(key, ZeroType.BYTE_ARRAY, data);
  }

  @Override
  public ZeroMap putShortArray(String key, Collection<Short> data) {
    return putElement(key, ZeroType.SHORT_ARRAY, data);
  }

  @Override
  public ZeroMap putIntegerArray(String key, Collection<Integer> data) {
    return putElement(key, ZeroType.INTEGER_ARRAY, data);
  }

  @Override
  public ZeroMap putLongArray(String key, Collection<Long> data) {
    return putElement(key, ZeroType.LONG_ARRAY, data);
  }

  @Override
  public ZeroMap putFloatArray(String key, Collection<Float> data) {
    return putElement(key, ZeroType.FLOAT_ARRAY, data);
  }

  @Override
  public ZeroMap putDoubleArray(String key, Collection<Double> data) {
    return putElement(key, ZeroType.DOUBLE_ARRAY, data);
  }

  @Override
  public ZeroMap putStringArray(String key, Collection<String> data) {
    return putElement(key, ZeroType.STRING_ARRAY, data);
  }

  @Override
  public ReadonlyZeroMap getReadonlyZeroMap() {
    return new ZeroMapImpl(Map.copyOf(map));
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("ZeroMap{");

    for (var iteratorKey = getKeys().iterator(); iteratorKey.hasNext(); builder.append(';')) {
      var key = iteratorKey.next();
      var zeroElement = getZeroElement(key);
      builder.append(" (").append(zeroElement.getType().toString().toLowerCase()).append(") ")
          .append(key)
          .append(": ");
      if (zeroElement.getType() == ZeroType.ZERO_MAP) {
        builder.append(zeroElement.getData().toString());
      } else if (zeroElement.getType() == ZeroType.ZERO_ARRAY) {
        builder.append(zeroElement.getData().toString());
      } else if (zeroElement.getType() == ZeroType.BYTE_ARRAY) {
        builder.append(String.format("byte[%d]", ((byte[]) zeroElement.getData()).length));
      } else {
        builder.append(Objects.nonNull(zeroElement.getData()) ? zeroElement.getData().toString()
            : "null");
      }
    }

    if (size() > 0) {
      builder.setLength(builder.length() - 1);
    }

    builder.append(" }");
    return builder.toString();
  }

  private ZeroMap putElement(String key, ZeroType type, Object data) {
    map.put(key, ZeroUtility.newZeroElement(type, data));
    return this;
  }
}
