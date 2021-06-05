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
package com.tenio.common.data.utilities;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tenio.common.data.ZeroArray;
import com.tenio.common.data.ZeroDataType;
import com.tenio.common.data.ZeroElement;
import com.tenio.common.data.ZeroObject;
import com.tenio.common.data.elements.ZeroData;
import com.tenio.common.data.implement.ZeroArrayImpl;
import com.tenio.common.data.implement.ZeroObjectImpl;

/**
 * @author kong
 */
public final class ZeroDataSerializerUtility {

	private static int BUFFER_CHUNK_BYTES = 512;

	private ZeroDataSerializerUtility() {

	}

	public static ZeroElement binaryToElement(byte[] binary) {
		switch (ZeroDataType.getByValue(binary[0])) {
		case ZERO_OBJECT:
			return binaryToObject(binary);

		case ZERO_ARRAY:
			return binaryToArray(binary);

		default:
			return null;
		}
	}

	public static ZeroArray binaryToArray(byte[] binary) {
		if (binary.length < 3) {
			throw new IllegalStateException(String.format(
					"Unable to decode a ZeroArray because binary data size is not big enough to work on it. Size: %d bytes",
					binary.length));
		}

		ByteBuffer buffer = ByteBuffer.allocate(binary.length);
		buffer.put(binary);
		buffer.flip();

		return __decodeZeroArray(buffer);
	}

	public static ZeroObject binaryToObject(byte[] binary) {
		if (binary.length < 3) {
			throw new IllegalStateException(String.format(
					"Unable to decode a ZeroObject because binary data size is not big enough to work on it. Size: %d bytes",
					binary.length));
		}

		ByteBuffer buffer = ByteBuffer.allocate(binary.length);
		buffer.put(binary);
		buffer.flip();

		return __decodeZeroObject(buffer);
	}

	public static byte[] objectToBinary(ZeroObject object) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_BYTES);
		buffer.put((byte) ZeroDataType.ZERO_OBJECT.getValue());
		buffer.putShort((short) object.size());

		return __objectToBinary(object, buffer);
	}

	private static byte[] __objectToBinary(ZeroObject object, ByteBuffer buffer) {
		Set<String> keys = object.getKeys();
		ZeroData zeroData;
		Object element;

		for (Iterator<String> iter = keys.iterator(); iter
				.hasNext(); buffer = __encodeObject(buffer, zeroData.getType(), element)) {
			String key = iter.next();
			zeroData = object.getZeroData(key);
			element = zeroData.getElement();
			buffer = __encodeZeroObjectKey(buffer, key);
		}

		int position = buffer.position();
		byte[] result = new byte[position];
		buffer.flip();
		buffer.get(result, 0, position);

		return result;
	}

	public static byte[] arrayToBinary(ZeroArray array) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_BYTES);
		buffer.put((byte) ZeroDataType.ZERO_ARRAY.getValue());
		buffer.putShort((short) array.size());

		return __arrayToBinary(array, buffer);
	}

	private static byte[] __arrayToBinary(ZeroArray array, ByteBuffer buffer) {
		ZeroData zeroData = null;
		Object element = null;

		for (Iterator<ZeroData> iter = array.iterator(); iter
				.hasNext(); buffer = __encodeObject(buffer, zeroData.getType(), element)) {
			zeroData = iter.next();
			element = zeroData.getElement();
		}

		int position = buffer.position();
		byte[] result = new byte[position];
		buffer.flip();
		buffer.get(result, 0, position);

		return result;
	}

	private static ZeroData __decodeObject(ByteBuffer buffer) throws RuntimeException {
		byte headerByte = buffer.get();
		ZeroDataType type = ZeroDataType.getByValue(headerByte);

		switch (type) {
		case NULL:
			return __decodeNull(buffer);
		case BOOLEAN:
			return __decodeBoolean(buffer);
		case BYTE:
			return __decodeByte(buffer);
		case SHORT:
			return __decodeShort(buffer);
		case INTEGER:
			return __decodeInteger(buffer);
		case FLOAT:
			return __decodeFloat(buffer);
		case LONG:
			return __decodeLong(buffer);
		case DOUBLE:
			return __decodeDouble(buffer);
		case STRING:
			return __decodeString(buffer);
		case BOOLEAN_ARRAY:
			return __decodeBooleanArray(buffer);
		case BYTE_ARRAY:
			return __decodeByteArray(buffer);
		case SHORT_ARRAY:
			return __decodeShortArray(buffer);
		case INTEGER_ARRAY:
			return __decodeIntegerArray(buffer);
		case FLOAT_ARRAY:
			return __decodeFloatArray(buffer);
		case LONG_ARRAY:
			return __decodeLongArray(buffer);
		case DOUBLE_ARRAY:
			return __decodeDoubleArray(buffer);
		case STRING_ARRAY:
			return __decodeStringArray(buffer);
		case ZERO_ARRAY:
			buffer.position(buffer.position() - Byte.BYTES);
			return ZeroData.newInstance(ZeroDataType.ZERO_ARRAY, __decodeZeroArray(buffer));
		case ZERO_OBJECT:
			buffer.position(buffer.position() - Byte.BYTES);
			return ZeroData.newInstance(ZeroDataType.ZERO_OBJECT, __decodeZeroObject(buffer));
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static ByteBuffer __encodeObject(ByteBuffer buffer, ZeroDataType type, Object element) {
		switch (type) {
		case NULL:
			buffer = __encodeNull(buffer);
			break;
		case BOOLEAN:
			buffer = __encodeBoolean(buffer, (Boolean) element);
			break;
		case BYTE:
			buffer = __encodeByte(buffer, (Byte) element);
			break;
		case SHORT:
			buffer = __encodeShort(buffer, (Short) element);
			break;
		case INTEGER:
			buffer = __encodeInteger(buffer, (Integer) element);
			break;
		case LONG:
			buffer = __encodeLong(buffer, (Long) element);
			break;
		case FLOAT:
			buffer = __encodeFloat(buffer, (Float) element);
			break;
		case DOUBLE:
			buffer = __encodeDouble(buffer, (Double) element);
			break;
		case STRING:
			buffer = __encodeString(buffer, (String) element);
			break;
		case BOOLEAN_ARRAY:
			buffer = __encodeBooleanArray(buffer, (Collection<Boolean>) element);
			break;
		case BYTE_ARRAY:
			buffer = __encodeByteArray(buffer, (byte[]) element);
			break;
		case SHORT_ARRAY:
			buffer = __encodeShortArray(buffer, (Collection<Short>) element);
			break;
		case INTEGER_ARRAY:
			buffer = __encodeIntegerArray(buffer, (Collection<Integer>) element);
			break;
		case LONG_ARRAY:
			buffer = __encodeLongArray(buffer, (Collection<Long>) element);
			break;
		case FLOAT_ARRAY:
			buffer = __encodeFloatArray(buffer, (Collection<Float>) element);
			break;
		case DOUBLE_ARRAY:
			buffer = __encodeDoubleArray(buffer, (Collection<Double>) element);
			break;
		case STRING_ARRAY:
			buffer = __encodeStringArray(buffer, (Collection<String>) element);
			break;
		case ZERO_ARRAY:
			buffer = __appendBinaryToBuffer(buffer, arrayToBinary((ZeroArray) element));
			break;
		case ZERO_OBJECT:
			buffer = __appendBinaryToBuffer(buffer, objectToBinary((ZeroObject) element));
			break;
		default:
			throw new IllegalArgumentException(String.format("Unsupported data type: %s", type.toString()));
		}

		return buffer;
	}

	private static ZeroData __decodeNull(ByteBuffer buffer) {
		return ZeroData.newInstance(ZeroDataType.NULL, (Object) null);
	}

	private static ZeroData __decodeBoolean(ByteBuffer buffer) {
		byte bool = buffer.get();
		Boolean element = null;

		if (bool == 0) {
			element = Boolean.FALSE;
		} else {
			if (bool != 1) {
				throw new IllegalStateException(String.format("Expected value of 0 or 1, but found: %d", bool));
			}

			element = Boolean.TRUE;
		}

		return ZeroData.newInstance(ZeroDataType.BOOLEAN, element);
	}

	private static ZeroData __decodeByte(ByteBuffer buffer) {
		byte element = buffer.get();
		return ZeroData.newInstance(ZeroDataType.BYTE, element);
	}

	private static ZeroData __decodeShort(ByteBuffer buffer) {
		short element = buffer.getShort();
		return ZeroData.newInstance(ZeroDataType.SHORT, element);
	}

	private static ZeroData __decodeInteger(ByteBuffer buffer) {
		int element = buffer.getInt();
		return ZeroData.newInstance(ZeroDataType.INTEGER, element);
	}

	private static ZeroData __decodeLong(ByteBuffer buffer) {
		long element = buffer.getLong();
		return ZeroData.newInstance(ZeroDataType.LONG, element);
	}

	private static ZeroData __decodeFloat(ByteBuffer buffer) {
		float element = buffer.getFloat();
		return ZeroData.newInstance(ZeroDataType.FLOAT, element);
	}

	private static ZeroData __decodeDouble(ByteBuffer buffer) {
		double element = buffer.getDouble();
		return ZeroData.newInstance(ZeroDataType.DOUBLE, element);
	}

	private static ZeroData __decodeString(ByteBuffer buffer) {
		short strLen = buffer.getShort();

		if (strLen < 0) {
			throw new IllegalStateException(String.format("The length of string is incorrect: %d", strLen));
		}

		byte[] strData = new byte[strLen];
		buffer.get(strData, 0, strLen);
		String element = new String(strData);

		return ZeroData.newInstance(ZeroDataType.STRING, element);
	}

	private static ZeroData __decodeBooleanArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<Boolean> element = new ArrayList<Boolean>();

		for (int i = 0; i < collectionSize; ++i) {
			byte bool = buffer.get();
			if (bool == 0) {
				element.add(false);
			} else {
				if (bool != 1) {
					throw new IllegalStateException(String.format("Expected value of 0 or 1, but found: %d", bool));
				}

				element.add(true);
			}
		}

		return ZeroData.newInstance(ZeroDataType.BOOLEAN_ARRAY, element);
	}

	private static ZeroData __decodeByteArray(ByteBuffer buffer) {
		int arraySize = buffer.getInt();
		if (arraySize < 0) {
			throw new NegativeArraySizeException(
					String.format("Could not create an array with negative size value: %d", arraySize));
		}

		byte[] byteData = new byte[arraySize];
		buffer.get(byteData, 0, arraySize);

		return ZeroData.newInstance(ZeroDataType.BYTE_ARRAY, byteData);
	}

	private static ZeroData __decodeShortArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<Short> element = new ArrayList<Short>();

		for (int i = 0; i < collectionSize; ++i) {
			short shortValue = buffer.getShort();
			element.add(shortValue);
		}

		return ZeroData.newInstance(ZeroDataType.SHORT_ARRAY, element);
	}

	private static ZeroData __decodeIntegerArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<Integer> element = new ArrayList<Integer>();

		for (int i = 0; i < collectionSize; ++i) {
			int intValue = buffer.getInt();
			element.add(intValue);
		}

		return ZeroData.newInstance(ZeroDataType.INTEGER_ARRAY, element);
	}

	private static ZeroData __decodeLongArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<Long> element = new ArrayList<Long>();

		for (int i = 0; i < collectionSize; ++i) {
			long longValue = buffer.getLong();
			element.add(longValue);
		}

		return ZeroData.newInstance(ZeroDataType.LONG_ARRAY, element);
	}

	private static ZeroData __decodeFloatArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<Float> element = new ArrayList<Float>();

		for (int i = 0; i < collectionSize; ++i) {
			float floatValue = buffer.getFloat();
			element.add(floatValue);
		}

		return ZeroData.newInstance(ZeroDataType.FLOAT_ARRAY, element);
	}

	private static ZeroData __decodeDoubleArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<Double> element = new ArrayList<Double>();

		for (int i = 0; i < collectionSize; ++i) {
			double doubleValue = buffer.getDouble();
			element.add(doubleValue);
		}

		return ZeroData.newInstance(ZeroDataType.DOUBLE_ARRAY, element);
	}

	private static ZeroData __decodeStringArray(ByteBuffer buffer) {
		short collectionSize = __getCollectionSize(buffer);
		List<String> element = new ArrayList<String>();

		for (int i = 0; i < collectionSize; ++i) {
			short strLen = buffer.getShort();
			if (strLen < 0) {
				throw new IllegalStateException(String.format("The length of string is incorrect: %d", strLen));
			}

			byte[] strData = new byte[strLen];
			buffer.get(strData, 0, strLen);
			String stringValue = new String(strData);
			element.add(stringValue);
		}

		return ZeroData.newInstance(ZeroDataType.STRING_ARRAY, element);
	}

	private static ZeroArray __decodeZeroArray(ByteBuffer buffer) {
		ZeroArray zeroArray = ZeroArrayImpl.newInstance();
		byte headerByte = buffer.get();

		if (ZeroDataType.getByValue(headerByte) != ZeroDataType.ZERO_ARRAY) {
			throw new IllegalStateException(
					String.format("Invalid ZeroDataType. Expected: %s, value: %d, but found: %s, value: %d",
							ZeroDataType.ZERO_ARRAY.toString(), ZeroDataType.ZERO_ARRAY.getValue(),
							ZeroDataType.getByValue(headerByte).toString(), headerByte));
		}

		short arraySize = buffer.getShort();
		if (arraySize < 0) {
			throw new NegativeArraySizeException(
					String.format("Could not create an array with negative size value: %d", arraySize));
		}

		try {
			for (int i = 0; i < arraySize; ++i) {
				ZeroData zeroData = __decodeObject(buffer);
				if (zeroData == null) {
					throw new IllegalStateException(
							String.format("Unable to not decode ZeroArray item at index: %d", i));
				}

				zeroArray.addZeroData(zeroData);
			}

			return zeroArray;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private static ZeroObject __decodeZeroObject(ByteBuffer buffer) {
		ZeroObject zeroObject = ZeroObjectImpl.newInstance();
		byte headerByte = buffer.get();

		if (ZeroDataType.getByValue(headerByte) != ZeroDataType.ZERO_OBJECT) {
			throw new IllegalStateException(
					String.format("Invalid ZeroDataType. Expected: %s, value: %d, but found: %s, value: %d",
							ZeroDataType.ZERO_OBJECT.toString(), ZeroDataType.ZERO_OBJECT.getValue(),
							ZeroDataType.getByValue(headerByte), headerByte));
		}

		short objectSize = buffer.getShort();
		if (objectSize < 0) {
			throw new NegativeArraySizeException(
					String.format("Could not create an object with negative size value: %d", objectSize));
		}

		try {
			for (int i = 0; i < objectSize; ++i) {
				short keySize = buffer.getShort();
				byte[] keyData = new byte[keySize];
				buffer.get(keyData, 0, keyData.length);
				String key = new String(keyData);
				ZeroData zeroData = __decodeObject(buffer);

				if (zeroData == null) {
					throw new IllegalStateException(String.format("Unable to decode value for key: %s", keyData));
				}

				zeroObject.putZeroData(key, zeroData);
			}

			return zeroObject;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private static short __getCollectionSize(ByteBuffer buffer) {
		short collectionSize = buffer.getShort();
		if (collectionSize < 0) {
			throw new NegativeArraySizeException(
					String.format("Could not create a collection with negative size value: %d", collectionSize));
		}

		return collectionSize;
	}

	private static ByteBuffer __encodeNull(ByteBuffer buffer) {
		return __appendBinaryToBuffer(buffer, new byte[1]);
	}

	private static ByteBuffer __encodeBoolean(ByteBuffer buffer, Boolean element) {
		byte[] data = new byte[] { (byte) ZeroDataType.BOOLEAN.getValue(), (byte) (element ? 1 : 0) };
		return __appendBinaryToBuffer(buffer, data);
	}

	private static ByteBuffer __encodeByte(ByteBuffer buffer, Byte element) {
		byte[] data = new byte[] { (byte) ZeroDataType.BYTE.getValue(), element };
		return __appendBinaryToBuffer(buffer, data);
	}

	private static ByteBuffer __encodeShort(ByteBuffer buffer, Short element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES);
		buf.put((byte) ZeroDataType.SHORT.getValue());
		buf.putShort(element);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeInteger(ByteBuffer buffer, Integer element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
		buf.put((byte) ZeroDataType.INTEGER.getValue());
		buf.putInt(element);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeLong(ByteBuffer buffer, Long element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
		buf.put((byte) ZeroDataType.LONG.getValue());
		buf.putLong(element);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeFloat(ByteBuffer buffer, Float element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Float.BYTES);
		buf.put((byte) ZeroDataType.FLOAT.getValue());
		buf.putFloat(element);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeDouble(ByteBuffer buffer, Double element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Double.BYTES);
		buf.put((byte) ZeroDataType.DOUBLE.getValue());
		buf.putDouble(element);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeString(ByteBuffer buffer, String element) {
		byte[] stringBytes = element.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + stringBytes.length);
		buf.put((byte) ZeroDataType.STRING.getValue());
		buf.putShort((short) stringBytes.length);
		buf.put(stringBytes);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeBooleanArray(ByteBuffer buffer, Collection<Boolean> element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + element.size());
		buf.put((byte) ZeroDataType.BOOLEAN_ARRAY.getValue());
		buf.putShort((short) element.size());
		Iterator<Boolean> iter = element.iterator();

		while (iter.hasNext()) {
			boolean boolValue = (Boolean) iter.next();
			buf.put((byte) (boolValue ? 1 : 0));
		}

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeByteArray(ByteBuffer buffer, byte[] element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + element.length);
		buf.put((byte) ZeroDataType.BYTE_ARRAY.getValue());
		buf.putInt(element.length);
		buf.put(element);

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeShortArray(ByteBuffer buffer, Collection<Short> element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Short.BYTES * element.size());
		buf.put((byte) ZeroDataType.SHORT_ARRAY.getValue());
		buf.putShort((short) element.size());
		Iterator<Short> iter = element.iterator();

		while (iter.hasNext()) {
			short shortValue = (Short) iter.next();
			buf.putShort(shortValue);
		}

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeIntegerArray(ByteBuffer buffer, Collection<Integer> element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Integer.BYTES * element.size());
		buf.put((byte) ZeroDataType.INTEGER_ARRAY.getValue());
		buf.putShort((short) element.size());
		Iterator<Integer> iter = element.iterator();

		while (iter.hasNext()) {
			int integerValue = (Integer) iter.next();
			buf.putInt(integerValue);
		}

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeLongArray(ByteBuffer buffer, Collection<Long> element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Long.BYTES * element.size());
		buf.put((byte) ZeroDataType.LONG_ARRAY.getValue());
		buf.putShort((short) element.size());
		Iterator<Long> iter = element.iterator();

		while (iter.hasNext()) {
			long longValue = (Long) iter.next();
			buf.putLong(longValue);
		}

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeFloatArray(ByteBuffer buffer, Collection<Float> element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Float.BYTES * element.size());
		buf.put((byte) ZeroDataType.FLOAT_ARRAY.getValue());
		buf.putShort((short) element.size());
		Iterator<Float> iter = element.iterator();

		while (iter.hasNext()) {
			float floatValue = (Float) iter.next();
			buf.putFloat(floatValue);
		}

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeDoubleArray(ByteBuffer buffer, Collection<Double> element) {
		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Double.BYTES * element.size());
		buf.put((byte) ZeroDataType.DOUBLE_ARRAY.getValue());
		buf.putShort((short) element.size());
		Iterator<Double> iter = element.iterator();

		while (iter.hasNext()) {
			double doubleValue = (Double) iter.next();
			buf.putDouble(doubleValue);
		}

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeStringArray(ByteBuffer buffer, Collection<String> collection) {
		int totalStringsLengthInBytes = 0;

		byte[] stringInBinary;
		for (Iterator<String> iter = collection.iterator(); iter
				.hasNext(); totalStringsLengthInBytes += Short.BYTES + stringInBinary.length) {
			String item = iter.next();
			stringInBinary = item.getBytes();
		}

		ByteBuffer buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + totalStringsLengthInBytes);
		buf.put((byte) ZeroDataType.STRING_ARRAY.getValue());
		buf.putShort((short) collection.size());
		collection.forEach(string -> {
			byte[] bytes = string.getBytes();
			buf.putShort((short) bytes.length);
			buf.put(bytes);
		});

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __encodeZeroObjectKey(ByteBuffer buffer, String key) {
		ByteBuffer buf = ByteBuffer.allocate(Short.BYTES + key.length());
		buf.putShort((short) key.length());
		buf.put(key.getBytes());

		return __appendBinaryToBuffer(buffer, buf.array());
	}

	private static ByteBuffer __appendBinaryToBuffer(ByteBuffer buffer, byte[] binary) {
		if (buffer.remaining() < binary.length) {
			int newSize = BUFFER_CHUNK_BYTES;
			if (newSize < binary.length) {
				newSize = binary.length;
			}

			ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + newSize);
			buffer.flip();
			newBuffer.put(buffer);
			buffer = newBuffer;
		}

		buffer.put(binary);
		return buffer;
	}

}
