package com.tenio.common.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.tenio.common.data.element.ZeroData;

public interface ZeroObject {

	byte[] toBinary();

	boolean isNull(String key);

	boolean containsKey(String key);

	boolean removeElement(String key);

	Set<String> getKeys();

	int size();

	Iterator<Entry<String, ZeroData>> iterator();

	Boolean getBoolean(String key);

	Byte getByte(String key);

	Short getShort(String key);

	Integer getInteger(String key);

	Long getLong(String key);

	Float getFloat(String key);

	Double getDouble(String key);

	String getString(String key);

	ZeroArray getZeroArray(String key);

	ZeroObject getZeroObject(String key);

	ZeroData getZeroData(String key);

	ZeroObject putNullElement(String key);

	ZeroObject putBoolean(String key, boolean element);

	ZeroObject putByte(String key, byte element);

	ZeroObject putShort(String key, short element);

	ZeroObject putInteger(String key, int element);

	ZeroObject putLong(String key, long element);

	ZeroObject putFloat(String key, float element);

	ZeroObject putDouble(String key, double element);

	ZeroObject putString(String key, String element);

	ZeroObject putZeroArray(String key, ZeroArray element);

	ZeroObject putZeroObject(String key, ZeroObject element);

	ZeroObject putZeroData(String key, ZeroData data);

	Collection<Boolean> getBooleanArray(String key);

	byte[] getByteArray(String key);

	Collection<Short> getShortArray(String key);

	Collection<Integer> getIntegerArray(String key);

	Collection<Long> getLongArray(String key);

	Collection<Float> getFloatArray(String key);

	Collection<Double> getDoubleArray(String key);

	Collection<String> getStringArray(String key);

	ZeroObject putBooleanArray(String key, Collection<Boolean> element);

	ZeroObject putByteArray(String key, byte[] element);

	ZeroObject putShortArray(String key, Collection<Short> element);

	ZeroObject putIntegerArray(String key, Collection<Integer> element);

	ZeroObject putLongArray(String key, Collection<Long> element);

	ZeroObject putFloatArray(String key, Collection<Float> element);

	ZeroObject putDoubleArray(String key, Collection<Double> element);

	ZeroObject putStringArray(String key, Collection<String> element);

}
