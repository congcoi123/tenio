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

package com.tenio.common.data.zero.implement;

import com.tenio.common.data.zero.ReadonlyZeroArray;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroElement;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.ZeroType;
import com.tenio.common.data.zero.utility.ZeroUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * An implementation for the zero array.
 */
public final class ZeroArrayImpl implements ZeroArray {

  private final List<ZeroElement> array;

  /**
   * Creates a new instance.
   */
  public ZeroArrayImpl() {
    array = new ArrayList<>();
  }

  private ZeroArrayImpl(List<ZeroElement> array) {
    this.array = array;
  }

  @Override
  public byte[] toBinary() {
    return ZeroUtility.arrayToBinary(this);
  }

  @Override
  public boolean contains(Object data) {
    var match =
        array.stream().filter(element -> element.getData().equals(data)).findFirst();
    return match.orElse(null) != null;
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
  public Iterator<ZeroElement> iterator() {
    return array.iterator();
  }

  @Override
  public Object getDataForElementAt(int index) {
    var element = getZeroElement(index);
    return Objects.nonNull(element) ? element.getData() : null;
  }

  @Override
  public void removeElementAt(int index) {
    array.remove(index);
  }

  @Override
  public int size() {
    return array.size();
  }

  @Override
  public boolean isNull(int index) {
    var element = getZeroElement(index);
    return Objects.nonNull(element) && element.getType() == ZeroType.NULL;
  }

  @Override
  public Boolean getBoolean(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Boolean) element.getData();
  }

  @Override
  public Byte getByte(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Byte) element.getData();
  }

  @Override
  public Short getShort(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Short) element.getData();
  }

  @Override
  public Integer getInteger(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Integer) element.getData();
  }

  @Override
  public Long getLong(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Long) element.getData();
  }

  @Override
  public Float getFloat(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Float) element.getData();
  }

  @Override
  public Double getDouble(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Double) element.getData();
  }

  @Override
  public String getString(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (String) element.getData();
  }

  @Override
  public ZeroArray getZeroArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (ZeroArray) element.getData();
  }

  @Override
  public ZeroMap getZeroMap(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (ZeroMap) element.getData();
  }

  @Override
  public ZeroElement getZeroElement(int index) {
    return array.get(index);
  }

  @Override
  public ZeroArray addNull() {
    return addElement(ZeroType.NULL, null);
  }

  @Override
  public ZeroArray addBoolean(boolean data) {
    return addElement(ZeroType.BOOLEAN, data);
  }

  @Override
  public ZeroArray addByte(byte data) {
    return addElement(ZeroType.BYTE, data);
  }

  @Override
  public ZeroArray addShort(short data) {
    return addElement(ZeroType.SHORT, data);
  }

  @Override
  public ZeroArray addInteger(int data) {
    return addElement(ZeroType.INTEGER, data);
  }

  @Override
  public ZeroArray addLong(long data) {
    return addElement(ZeroType.LONG, data);
  }

  @Override
  public ZeroArray addFloat(float data) {
    return addElement(ZeroType.FLOAT, data);
  }

  @Override
  public ZeroArray addDouble(double data) {
    return addElement(ZeroType.DOUBLE, data);
  }

  @Override
  public ZeroArray addString(String data) {
    return addElement(ZeroType.STRING, data);
  }

  @Override
  public ZeroArray addZeroArray(ZeroArray data) {
    return addElement(ZeroType.ZERO_ARRAY, data);
  }

  @Override
  public ZeroArray addZeroMap(ZeroMap data) {
    return addElement(ZeroType.ZERO_MAP, data);
  }

  @Override
  public ZeroArray addZeroElement(ZeroElement element) {
    array.add(element);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Boolean> getBooleanArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<Boolean>) element.getData();
  }

  @Override
  public byte[] getByteArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (byte[]) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Short> getShortArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<Short>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Integer> getIntegerArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<Integer>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Long> getLongArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<Long>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Float> getFloatArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<Float>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Double> getDoubleArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<Double>) element.getData();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<String> getStringArray(int index) {
    var element = getZeroElement(index);
    return Objects.isNull(element) ? null : (Collection<String>) element.getData();
  }

  @Override
  public ZeroArray addBooleanArray(Collection<Boolean> data) {
    return addElement(ZeroType.BOOLEAN_ARRAY, data);
  }

  @Override
  public ZeroArray addByteArray(byte[] data) {
    return addElement(ZeroType.BYTE_ARRAY, data);
  }

  @Override
  public ZeroArray addShortArray(Collection<Short> data) {
    return addElement(ZeroType.SHORT_ARRAY, data);
  }

  @Override
  public ZeroArray addIntegerArray(Collection<Integer> data) {
    return addElement(ZeroType.INTEGER_ARRAY, data);
  }

  @Override
  public ZeroArray addLongArray(Collection<Long> data) {
    return addElement(ZeroType.LONG_ARRAY, data);
  }

  @Override
  public ZeroArray addFloatArray(Collection<Float> data) {
    return addElement(ZeroType.FLOAT_ARRAY, data);
  }

  @Override
  public ZeroArray addDoubleArray(Collection<Double> data) {
    return addElement(ZeroType.DOUBLE_ARRAY, data);
  }

  @Override
  public ZeroArray addStringArray(Collection<String> data) {
    return addElement(ZeroType.STRING_ARRAY, data);
  }

  @Override
  public ReadonlyZeroArray getReadonlyZeroArray() {
    return new ZeroArrayImpl(List.copyOf(array));
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("ZeroArray{");

    Object toString;
    ZeroElement zeroElement;
    for (var iterator = iterator(); iterator.hasNext(); builder.append(" (")
        .append(zeroElement.getType().toString().toLowerCase()).append(") ").append(toString)
        .append(';')) {
      zeroElement = iterator.next();
      if (zeroElement.getType() == ZeroType.ZERO_MAP) {
        toString = zeroElement.getData().toString();
      } else if (zeroElement.getType() == ZeroType.ZERO_ARRAY) {
        toString = zeroElement.getData().toString();
      } else if (zeroElement.getType() == ZeroType.BYTE_ARRAY) {
        toString = String.format("byte[%d]", ((byte[]) zeroElement.getData()).length);
      } else {
        toString = zeroElement.getData().toString();
      }
    }

    if (size() > 0) {
      builder.setLength(builder.length() - 1);
    }

    builder.append(" }");
    return builder.toString();
  }

  private ZeroArray addElement(ZeroType type, Object data) {
    array.add(ZeroUtility.newZeroElement(type, data));
    return this;
  }
}
