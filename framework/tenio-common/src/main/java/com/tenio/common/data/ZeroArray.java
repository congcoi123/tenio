package com.tenio.common.data;

import java.util.Collection;
import java.util.Iterator;

public interface ZeroArray {

	byte[] toBinary();

	boolean contains(Object element);

	Iterator<ZeroData> iterator();

	Object getElementAt(int index);

	void removeElementAt(int index);

	int size();

	boolean isNull(int index);

	Boolean getBoolean(int index);

	Byte getByte(int index);

	Short getShort(int index);

	Integer getInteger(int index);

	Long getLong(int index);

	Float getFloat(int index);

	Double getDouble(int index);

	String getString(int index);

	ZeroArray getZeroArray(int index);

	ZeroObject getZeroObject(int index);

	ZeroData getZeroData(int index);

	ZeroArray addNullElement();

	ZeroArray addBoolean(boolean element);

	ZeroArray addByte(byte element);

	ZeroArray addShort(short element);

	ZeroArray addInteger(int element);

	ZeroArray addLong(long element);

	ZeroArray addFloat(float element);

	ZeroArray addDouble(double element);

	ZeroArray addString(String element);

	ZeroArray addZeroArray(ZeroArray element);

	ZeroArray addZeroObject(ZeroObject element);

	ZeroArray addZeroData(ZeroData data);

	Collection<Boolean> getBooleanArray(int index);

	byte[] getByteArray(int index);

	Collection<Short> getShortArray(int index);

	Collection<Integer> getIntegerArray(int index);

	Collection<Long> getLongArray(int index);

	Collection<Float> getFloatArray(int index);

	Collection<Double> getDoubleArray(int index);

	Collection<String> getStringArray(int index);

	ZeroArray addBooleanArray(Collection<Boolean> element);

	ZeroArray addByteArray(byte[] element);

	ZeroArray addShortArray(Collection<Short> element);

	ZeroArray addIntegerArray(Collection<Integer> element);

	ZeroArray addLongArray(Collection<Long> element);

	ZeroArray addFloatArray(Collection<Float> element);

	ZeroArray addDoubleArray(Collection<Double> element);

	ZeroArray addStringArray(Collection<String> element);

}
