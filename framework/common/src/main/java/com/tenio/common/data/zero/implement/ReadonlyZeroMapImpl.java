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

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.data.zero.ReadonlyZeroMap;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroElement;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.ZeroType;
import com.tenio.common.data.zero.utility.ZeroUtility;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Detached implementation of a read-only Zero map.
 */
final class ReadonlyZeroMapImpl implements ReadonlyZeroMap {

    @Serial
    private static final long serialVersionUID = 5141848902265335853L;

    private final ZeroMap snapshot;

    ReadonlyZeroMapImpl(ZeroMap source) {
        snapshot = ZeroUtility.binariesToMap(source.toBinaries());
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
    public boolean isNull(String key) {
        return snapshot.isNull(key);
    }

    @Override
    public boolean containsKey(String key) {
        return snapshot.containsKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return snapshot.getReadonlyKeys();
    }

    @Override
    public Set<String> getReadonlyKeys() {
        return snapshot.getReadonlyKeys();
    }

    @Override
    public Boolean getBoolean(String key) {
        return snapshot.getBoolean(key);
    }

    @Override
    public Byte getByte(String key) {
        return snapshot.getByte(key);
    }

    @Override
    public Short getShort(String key) {
        return snapshot.getShort(key);
    }

    @Override
    public Integer getInteger(String key) {
        return snapshot.getInteger(key);
    }

    @Override
    public Long getLong(String key) {
        return snapshot.getLong(key);
    }

    @Override
    public Float getFloat(String key) {
        return snapshot.getFloat(key);
    }

    @Override
    public Double getDouble(String key) {
        return snapshot.getDouble(key);
    }

    @Override
    public String getString(String key) {
        return snapshot.getString(key);
    }

    @Override
    public DataCollection getDataCollection(String key) {
        var value = snapshot.getDataCollection(key);
        if (value instanceof ZeroMap map) {
            return copy(map);
        }
        if (value instanceof ZeroArray array) {
            return copy(array);
        }
        return null;
    }

    @Override
    public ZeroArray getZeroArray(String key) {
        return copy(snapshot.getZeroArray(key));
    }

    @Override
    public ZeroMap getZeroMap(String key) {
        return copy(snapshot.getZeroMap(key));
    }

    @Override
    public ZeroElement getZeroElement(String key) {
        var element = snapshot.getZeroElement(key);
        return element == null ? null
                : ZeroUtility.newZeroElement(element.getType(), copyData(element.getType(), element.getData()));
    }

    @Override
    public Collection<Boolean> getBooleanArray(String key) {
        return immutable(snapshot.getBooleanArray(key));
    }

    @Override
    public byte[] getByteArray(String key) {
        var value = snapshot.getByteArray(key);
        return value == null ? null : value.clone();
    }

    @Override
    public Collection<Short> getShortArray(String key) {
        return immutable(snapshot.getShortArray(key));
    }

    @Override
    public Collection<Integer> getIntegerArray(String key) {
        return immutable(snapshot.getIntegerArray(key));
    }

    @Override
    public Collection<Long> getLongArray(String key) {
        return immutable(snapshot.getLongArray(key));
    }

    @Override
    public Collection<Float> getFloatArray(String key) {
        return immutable(snapshot.getFloatArray(key));
    }

    @Override
    public Collection<Double> getDoubleArray(String key) {
        return immutable(snapshot.getDoubleArray(key));
    }

    @Override
    public Collection<String> getStringArray(String key) {
        return immutable(snapshot.getStringArray(key));
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

    private static Object copyData(ZeroType type, Object value) {
        return switch (type) {
            case BYTE_ARRAY -> value == null ? null : ((byte[]) value).clone();
            case BOOLEAN_ARRAY, SHORT_ARRAY, INTEGER_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY,
                 STRING_ARRAY -> value == null ? null : List.copyOf((Collection<?>) value);
            case ZERO_ARRAY -> copy((ZeroArray) value);
            case ZERO_MAP -> copy((ZeroMap) value);
            default -> value;
        };
    }
}
