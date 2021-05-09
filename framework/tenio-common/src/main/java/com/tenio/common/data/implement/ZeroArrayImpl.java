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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.tenio.common.data.ZeroArray;
import com.tenio.common.data.ZeroDataType;
import com.tenio.common.data.ZeroObject;
import com.tenio.common.data.element.ZeroData;
import com.tenio.common.data.utility.ZeroDataSerializerUtility;

/**
 * @author kong
 */
// TODO: Add description
public final class ZeroArrayImpl implements ZeroArray {

	private final List<ZeroData> __data;

	public static ZeroArray newInstance() {
		return new ZeroArrayImpl();
	}

	public static ZeroArray newInstance(byte[] binary) {
		return ZeroDataSerializerUtility.binaryToArray(binary);
	}

	private ZeroArrayImpl() {
		__data = new ArrayList<ZeroData>();
	}

	@Override
	public byte[] toBinary() {
		return ZeroDataSerializerUtility.arrayToBinary(this);
	}

	@Override
	public boolean contains(Object element) {
		Optional<ZeroData> match = __data.stream().filter(data -> data.getElement().equals(element)).findFirst();
		return match.orElse(null) == null ? false : true;
	}

	@Override
	public Iterator<ZeroData> iterator() {
		return __data.iterator();
	}

	@Override
	public Object getElementAt(int index) {
		var data = getZeroData(index);
		return data != null ? data.getElement() : null;
	}

	@Override
	public void removeElementAt(int index) {
		__data.remove(index);
	}

	@Override
	public int size() {
		return __data.size();
	}

	@Override
	public boolean isNull(int index) {
		var data = getZeroData(index);
		return data == null ? false : data.getType() == ZeroDataType.NULL;
	}

	@Override
	public Boolean getBoolean(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Boolean) data.getElement();
	}

	@Override
	public Byte getByte(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Byte) data.getElement();
	}

	@Override
	public Short getShort(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Short) data.getElement();
	}

	@Override
	public Integer getInteger(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Integer) data.getElement();
	}

	@Override
	public Long getLong(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Long) data.getElement();
	}

	@Override
	public Float getFloat(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Float) data.getElement();
	}

	@Override
	public Double getDouble(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Double) data.getElement();
	}

	@Override
	public String getString(int index) {
		var data = getZeroData(index);
		return data == null ? null : (String) data.getElement();
	}

	@Override
	public ZeroArray getZeroArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (ZeroArray) data.getElement();
	}

	@Override
	public ZeroObject getZeroObject(int index) {
		var data = getZeroData(index);
		return data == null ? null : (ZeroObject) data.getElement();
	}

	@Override
	public ZeroData getZeroData(int index) {
		return __data.get(index);
	}

	@Override
	public ZeroArray addNull() {
		return __addData(ZeroDataType.NULL, (Object) null);
	}

	@Override
	public ZeroArray addBoolean(boolean element) {
		return __addData(ZeroDataType.BOOLEAN, element);
	}

	@Override
	public ZeroArray addByte(byte element) {
		return __addData(ZeroDataType.BYTE, element);
	}

	@Override
	public ZeroArray addShort(short element) {
		return __addData(ZeroDataType.SHORT, element);
	}

	@Override
	public ZeroArray addInteger(int element) {
		return __addData(ZeroDataType.INTEGER, element);
	}

	@Override
	public ZeroArray addLong(long element) {
		return __addData(ZeroDataType.LONG, element);
	}

	@Override
	public ZeroArray addFloat(float element) {
		return __addData(ZeroDataType.FLOAT, element);
	}

	@Override
	public ZeroArray addDouble(double element) {
		return __addData(ZeroDataType.DOUBLE, element);
	}

	@Override
	public ZeroArray addString(String element) {
		return __addData(ZeroDataType.STRING, element);
	}

	@Override
	public ZeroArray addZeroArray(ZeroArray element) {
		return __addData(ZeroDataType.ZERO_ARRAY, element);
	}

	@Override
	public ZeroArray addZeroObject(ZeroObject element) {
		return __addData(ZeroDataType.ZERO_OBJECT, element);
	}

	@Override
	public ZeroArray addZeroData(ZeroData data) {
		__data.add(data);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Boolean> getBooleanArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<Boolean>) data.getElement();
	}

	@Override
	public byte[] getByteArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (byte[]) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Short> getShortArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<Short>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> getIntegerArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<Integer>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Long> getLongArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<Long>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Float> getFloatArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<Float>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Double> getDoubleArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<Double>) data.getElement();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getStringArray(int index) {
		var data = getZeroData(index);
		return data == null ? null : (Collection<String>) data.getElement();
	}

	@Override
	public ZeroArray addBooleanArray(Collection<Boolean> element) {
		return __addData(ZeroDataType.BOOLEAN_ARRAY, element);
	}

	@Override
	public ZeroArray addByteArray(byte[] element) {
		return __addData(ZeroDataType.BYTE_ARRAY, element);
	}

	@Override
	public ZeroArray addShortArray(Collection<Short> element) {
		return __addData(ZeroDataType.SHORT_ARRAY, element);
	}

	@Override
	public ZeroArray addIntegerArray(Collection<Integer> element) {
		return __addData(ZeroDataType.INTEGER_ARRAY, element);
	}

	@Override
	public ZeroArray addLongArray(Collection<Long> element) {
		return __addData(ZeroDataType.LONG_ARRAY, element);
	}

	@Override
	public ZeroArray addFloatArray(Collection<Float> element) {
		return __addData(ZeroDataType.FLOAT_ARRAY, element);
	}

	@Override
	public ZeroArray addDoubleArray(Collection<Double> element) {
		return __addData(ZeroDataType.DOUBLE_ARRAY, element);
	}

	@Override
	public ZeroArray addStringArray(Collection<String> element) {
		return __addData(ZeroDataType.STRING_ARRAY, element);
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder.append('{');

		Object toString = null;
		ZeroData zeroData;
		for (Iterator<ZeroData> iter = iterator(); iter.hasNext(); builder.append(" (")
				.append(zeroData.getType().toString().toLowerCase()).append(") ").append(toString).append(';')) {
			zeroData = iter.next();
			if (zeroData.getType() == ZeroDataType.ZERO_OBJECT) {
				toString = ((ZeroObject) zeroData.getElement()).toString();
			} else if (zeroData.getType() == ZeroDataType.ZERO_ARRAY) {
				toString = ((ZeroArray) zeroData.getElement()).toString();
			} else if (zeroData.getType() == ZeroDataType.BYTE_ARRAY) {
				toString = String.format("byte[%d]", ((byte[]) zeroData.getElement()).length);
			} else {
				toString = zeroData.getElement().toString();
			}
		}

		if (size() > 0) {
			builder.setLength(builder.length() - 1);
		}

		builder.append(" }");
		return builder.toString();
	}

	private ZeroArray __addData(ZeroDataType type, Object element) {
		__data.add(ZeroData.newInstance(type, element));
		return this;
	}

}
