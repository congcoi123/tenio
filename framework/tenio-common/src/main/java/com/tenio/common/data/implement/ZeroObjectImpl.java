/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.common.data.implement;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tenio.common.data.ZeroArray;
import com.tenio.common.data.ZeroDataType;
import com.tenio.common.data.ZeroObject;
import com.tenio.common.data.element.ZeroData;
import com.tenio.common.data.utility.ZeroDataSerializerUtility;

/**
 * @author kong
 */
// TODO: Add description
public final class ZeroObjectImpl implements ZeroObject {

	private final Map<String, ZeroData> __data;

	public static ZeroObject newInstance() {
		return new ZeroObjectImpl();
	}

	public static ZeroObject newInstance(byte[] binary) {
		return ZeroDataSerializerUtility.binaryToObject(binary);
	}

	private ZeroObjectImpl() {
		__data = new ConcurrentHashMap<String, ZeroData>();
	}

	@Override
	public byte[] toBinary() {
		return ZeroDataSerializerUtility.objectToBinary(this);
	}

	@Override
	public boolean isNull(String key) {
		var data = getZeroData(key);
		return data == null ? false : data.getType() == ZeroDataType.NULL;
	}

	@Override
	public boolean containsKey(String key) {
		return __data.containsKey(key);
	}

	@Override
	public boolean removeElement(String key) {
		return __data.remove(key) != null;
	}

	@Override
	public Set<String> getKeys() {
		return __data.keySet();
	}

	@Override
	public int size() {
		return __data.size();
	}

	@Override
	public Iterator<Entry<String, ZeroData>> iterator() {
		return __data.entrySet().iterator();
	}

	@Override
	public Boolean getBoolean(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Boolean) data.getElement();
	}

	@Override
	public Byte getByte(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Byte) data.getElement();
	}

	@Override
	public Short getShort(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Short) data.getElement();
	}

	@Override
	public Integer getInteger(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Integer) data.getElement();
	}

	@Override
	public Long getLong(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Long) data.getElement();
	}

	@Override
	public Float getFloat(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Float) data.getElement();
	}

	@Override
	public Double getDouble(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Double) data.getElement();
	}

	@Override
	public String getString(String key) {
		var data = getZeroData(key);
		return data == null ? null : (String) data.getElement();
	}

	@Override
	public ZeroArray getZeroArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (ZeroArray) data.getElement();
	}

	@Override
	public ZeroObject getZeroObject(String key) {
		var data = getZeroData(key);
		return data == null ? null : (ZeroObject) data.getElement();
	}

	@Override
	public ZeroData getZeroData(String key) {
		return __data.get(key);
	}

	@Override
	public ZeroObject putNull(String key) {
		return __putData(key, ZeroDataType.NULL, (Object) null);
	}

	@Override
	public ZeroObject putBoolean(String key, boolean element) {
		return __putData(key, ZeroDataType.BOOLEAN, element);
	}

	@Override
	public ZeroObject putByte(String key, byte element) {
		return __putData(key, ZeroDataType.BYTE, element);
	}

	@Override
	public ZeroObject putShort(String key, short element) {
		return __putData(key, ZeroDataType.SHORT, element);
	}

	@Override
	public ZeroObject putInteger(String key, int element) {
		return __putData(key, ZeroDataType.INTEGER, element);
	}

	@Override
	public ZeroObject putLong(String key, long element) {
		return __putData(key, ZeroDataType.LONG, element);
	}

	@Override
	public ZeroObject putFloat(String key, float element) {
		return __putData(key, ZeroDataType.FLOAT, element);
	}

	@Override
	public ZeroObject putDouble(String key, double element) {
		return __putData(key, ZeroDataType.DOUBLE, element);
	}

	@Override
	public ZeroObject putString(String key, String element) {
		return __putData(key, ZeroDataType.STRING, element);
	}

	@Override
	public ZeroObject putZeroArray(String key, ZeroArray element) {
		return __putData(key, ZeroDataType.ZERO_ARRAY, element);
	}

	@Override
	public ZeroObject putZeroObject(String key, ZeroObject element) {
		return __putData(key, ZeroDataType.ZERO_OBJECT, element);
	}

	@Override
	public ZeroObject putZeroData(String key, ZeroData data) {
		__data.put(key, data);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Boolean> getBooleanArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<Boolean>) data.getElement();
	}

	@Override
	public byte[] getByteArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (byte[]) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Short> getShortArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<Short>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> getIntegerArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<Integer>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Long> getLongArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<Long>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Float> getFloatArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<Float>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Double> getDoubleArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<Double>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getStringArray(String key) {
		var data = getZeroData(key);
		return data == null ? null : (Collection<String>) data.getElement();
	}

	@Override
	public ZeroObject putBooleanArray(String key, Collection<Boolean> element) {
		return __putData(key, ZeroDataType.BOOLEAN_ARRAY, element);
	}

	@Override
	public ZeroObject putByteArray(String key, byte[] element) {
		return __putData(key, ZeroDataType.BYTE_ARRAY, element);
	}

	@Override
	public ZeroObject putShortArray(String key, Collection<Short> element) {
		return __putData(key, ZeroDataType.SHORT_ARRAY, element);
	}

	@Override
	public ZeroObject putIntegerArray(String key, Collection<Integer> element) {
		return __putData(key, ZeroDataType.INTEGER_ARRAY, element);
	}

	@Override
	public ZeroObject putLongArray(String key, Collection<Long> element) {
		return __putData(key, ZeroDataType.LONG_ARRAY, element);
	}

	@Override
	public ZeroObject putFloatArray(String key, Collection<Float> element) {
		return __putData(key, ZeroDataType.FLOAT_ARRAY, element);
	}

	@Override
	public ZeroObject putDoubleArray(String key, Collection<Double> element) {
		return __putData(key, ZeroDataType.DOUBLE_ARRAY, element);
	}

	@Override
	public ZeroObject putStringArray(String key, Collection<String> element) {
		return __putData(key, ZeroDataType.STRING_ARRAY, element);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('{');

		for (Iterator<String> iterKey = getKeys().iterator(); iterKey.hasNext(); builder.append(';')) {
			String key = (String) iterKey.next();
			ZeroData zeroData = getZeroData(key);
			builder.append(" (").append(zeroData.getType().toString().toLowerCase()).append(") ").append(key)
					.append(": ");
			if (zeroData.getType() == ZeroDataType.ZERO_OBJECT) {
				builder.append(((ZeroObject) zeroData.getElement()).toString());
			} else if (zeroData.getType() == ZeroDataType.ZERO_ARRAY) {
				builder.append(((ZeroArray) zeroData.getElement()).toString());
			} else if (zeroData.getType() == ZeroDataType.BYTE_ARRAY) {
				builder.append(String.format("byte[%d]", ((byte[]) zeroData.getElement()).length));
			} else {
				builder.append(zeroData.getElement().toString());
			}
		}

		if (size() > 0) {
			builder.setLength(builder.length() - 1);
		}

		builder.append(" }");
		return builder.toString();
	}

	private ZeroObject __putData(String key, ZeroDataType type, Object element) {
		__data.put(key, ZeroData.newInstance(type, element));
		return this;
	}

}
