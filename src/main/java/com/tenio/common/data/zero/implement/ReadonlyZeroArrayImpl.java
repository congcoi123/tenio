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

package com.tenio.common.data.zero.implement;

import com.tenio.common.data.DataType;
import com.tenio.common.data.zero.ReadonlyZeroArray;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroElement;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.utility.ZeroUtility;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Detached implementation of a read-only Zero array.
 */
final class ReadonlyZeroArrayImpl implements ReadonlyZeroArray {

    @Serial
    private static final long serialVersionUID = 1877260152631113355L;

    private final ZeroArray snapshot;

    ReadonlyZeroArrayImpl(ZeroArray source) {
        snapshot = ZeroUtility.binariesToArray(source.toBinaries());
    }

    @Override
    public byte[] toBinaries() {
        return snapshot.toBinaries();
    }

    @Override
    public int size() {
        return snapshot.size();
    }

    @Override
    public DataType getType() {
        return snapshot.getType();
    }

    @Override
    public boolean contains(Object element) {
        return snapshot.contains(element);
    }

    @Override
    public boolean containsValueAt(int index) {
        return snapshot.containsValueAt(index);
    }

    @Override
    public Object getDataForElementAt(int index) {
        return copyData(snapshot.getZeroElement(index));
    }

    @Override
    public boolean isNull(int index) {
        return snapshot.isNull(index);
    }

    @Override
    public Boolean getBoolean(int index) {
        return snapshot.getBoolean(index);
    }

    @Override
    public Byte getByte(int index) {
        return snapshot.getByte(index);
    }

    @Override
    public Short getShort(int index) {
        return snapshot.getShort(index);
    }

    @Override
    public Integer getInteger(int index) {
        return snapshot.getInteger(index);
    }

    @Override
    public Long getLong(int index) {
        return snapshot.getLong(index);
    }

    @Override
    public Float getFloat(int index) {
        return snapshot.getFloat(index);
    }

    @Override
    public Double getDouble(int index) {
        return snapshot.getDouble(index);
    }

    @Override
    public String getString(int index) {
        return snapshot.getString(index);
    }

    @Override
    public ZeroArray getZeroArray(int index) {
        return copy(snapshot.getZeroArray(index));
    }

    @Override
    public ZeroMap getZeroMap(int index) {
        return copy(snapshot.getZeroMap(index));
    }

    @Override
    public ZeroElement getZeroElement(int index) {
        var element = snapshot.getZeroElement(index);
        return ZeroUtility.newZeroElement(element.getType(), copyData(element));
    }

    @Override
    public Collection<Boolean> getBooleanArray(int index) {
        return immutable(snapshot.getBooleanArray(index));
    }

    @Override
    public byte[] getByteArray(int index) {
        var value = snapshot.getByteArray(index);
        return value == null ? null : value.clone();
    }

    @Override
    public Collection<Short> getShortArray(int index) {
        return immutable(snapshot.getShortArray(index));
    }

    @Override
    public Collection<Integer> getIntegerArray(int index) {
        return immutable(snapshot.getIntegerArray(index));
    }

    @Override
    public Collection<Long> getLongArray(int index) {
        return immutable(snapshot.getLongArray(index));
    }

    @Override
    public Collection<Float> getFloatArray(int index) {
        return immutable(snapshot.getFloatArray(index));
    }

    @Override
    public Collection<Double> getDoubleArray(int index) {
        return immutable(snapshot.getDoubleArray(index));
    }

    @Override
    public Collection<String> getStringArray(int index) {
        return immutable(snapshot.getStringArray(index));
    }

    private static <T> Collection<T> immutable(Collection<T> value) {
        return value == null ? null : List.copyOf(value);
    }

    private static ZeroMap copy(ZeroMap value) {
        return value == null ? null : ZeroUtility.binariesToMap(value.toBinaries());
    }

    private static ZeroArray copy(ZeroArray value) {
        return value == null ? null : ZeroUtility.binariesToArray(value.toBinaries());
    }

    private static Object copyData(ZeroElement element) {
        Object value = element.getData();
        return switch (element.getType()) {
            case BYTE_ARRAY -> value == null ? null : ((byte[]) value).clone();
            case BOOLEAN_ARRAY, SHORT_ARRAY, INTEGER_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY,
                 STRING_ARRAY -> value == null ? null : List.copyOf((Collection<?>) value);
            case ZERO_ARRAY -> copy((ZeroArray) value);
            case ZERO_MAP -> copy((ZeroMap) value);
            default -> value;
        };
    }
}
