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

package com.tenio.common.data.utility;

import com.tenio.common.data.ZeroArray;
import com.tenio.common.data.ZeroDataType;
import com.tenio.common.data.ZeroElement;
import com.tenio.common.data.ZeroObject;
import com.tenio.common.data.element.ZeroData;
import com.tenio.common.data.implement.ZeroArrayImpl;
import com.tenio.common.data.implement.ZeroObjectImpl;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides all necessary methods to work with the self-definition classes.
 */
public final class ZeroDataSerializerUtility {

  private static final int BUFFER_CHUNK_BYTES = 512;

  private ZeroDataSerializerUtility() {
    throw new UnsupportedOperationException("This class does not support to create an instance");
  }

  /**
   * Deserialize a stream of bytes to a zero element.
   *
   * @param binary the stream of bytes
   * @return a new zero element instance
   */
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

  /**
   * Deserialize a stream of bytes to a zero array.
   *
   * @param binary the stream of bytes
   * @return a new zero array instance
   */
  public static ZeroArray binaryToArray(byte[] binary) {
    if (binary.length < 3) {
      throw new IllegalStateException(String.format(
          "Unable to decode a ZeroArray because binary data size is not big enough to work on it."
              + " Size: %d bytes",
          binary.length));
    }

    var buffer = ByteBuffer.allocate(binary.length);
    buffer.put(binary);
    buffer.flip();

    return decodeZeroArray(buffer);
  }

  /**
   * Deserialize a stream of bytes to a zero object.
   *
   * @param binary the stream of bytes
   * @return a new zero object instance
   */
  public static ZeroObject binaryToObject(byte[] binary) {
    if (binary.length < 3) {
      throw new IllegalStateException(String.format(
          "Unable to decode a ZeroObject because binary data size is not big enough to work on it"
              + ". Size: %d bytes",
          binary.length));
    }

    var buffer = ByteBuffer.allocate(binary.length);
    buffer.put(binary);
    buffer.flip();

    return decodeZeroObject(buffer);
  }

  /**
   * Serialize an object to a stream of bytes.
   *
   * @param object the object
   * @return the stream of bytes converted from the object
   */
  public static byte[] objectToBinary(ZeroObject object) {
    var buffer = ByteBuffer.allocate(BUFFER_CHUNK_BYTES);
    buffer.put((byte) ZeroDataType.ZERO_OBJECT.getValue());
    buffer.putShort((short) object.size());

    return objectToBinary(object, buffer);
  }

  private static byte[] objectToBinary(ZeroObject object, ByteBuffer buffer) {
    var keys = object.getKeys();
    ZeroData zeroData = null;
    Object element = null;

    for (var iterator = keys.iterator(); iterator
        .hasNext(); buffer = encodeObject(buffer, zeroData.getType(), element)) {
      var key = iterator.next();
      zeroData = object.getZeroData(key);
      element = zeroData.getElement();
      buffer = encodeZeroObjectKey(buffer, key);
    }

    var position = buffer.position();
    var result = new byte[position];
    buffer.flip();
    buffer.get(result, 0, position);

    return result;
  }

  /**
   * Serialize an array to a stream of bytes.
   *
   * @param array the array
   * @return the stream of bytes converted from the array
   */
  public static byte[] arrayToBinary(ZeroArray array) {
    var buffer = ByteBuffer.allocate(BUFFER_CHUNK_BYTES);
    buffer.put((byte) ZeroDataType.ZERO_ARRAY.getValue());
    buffer.putShort((short) array.size());

    return arrayToBinary(array, buffer);
  }

  private static byte[] arrayToBinary(ZeroArray array, ByteBuffer buffer) {
    ZeroData zeroData = null;
    Object element = null;

    for (var iterator = array.iterator(); iterator
        .hasNext(); buffer = encodeObject(buffer, zeroData.getType(), element)) {
      zeroData = iterator.next();
      element = zeroData.getElement();
    }

    var position = buffer.position();
    var result = new byte[position];
    buffer.flip();
    buffer.get(result, 0, position);

    return result;
  }

  private static ZeroData decodeObject(ByteBuffer buffer) throws RuntimeException {
    var headerByte = buffer.get();
    var type = ZeroDataType.getByValue(headerByte);

    switch (type) {
      case NULL:
        return decodeNull(buffer);
      case BOOLEAN:
        return decodeBoolean(buffer);
      case BYTE:
        return decodeByte(buffer);
      case SHORT:
        return decodeShort(buffer);
      case INTEGER:
        return decodeInteger(buffer);
      case FLOAT:
        return decodeFloat(buffer);
      case LONG:
        return decodeLong(buffer);
      case DOUBLE:
        return decodeDouble(buffer);
      case STRING:
        return decodeString(buffer);
      case BOOLEAN_ARRAY:
        return decodeBooleanArray(buffer);
      case BYTE_ARRAY:
        return decodeByteArray(buffer);
      case SHORT_ARRAY:
        return decodeShortArray(buffer);
      case INTEGER_ARRAY:
        return decodeIntegerArray(buffer);
      case FLOAT_ARRAY:
        return decodeFloatArray(buffer);
      case LONG_ARRAY:
        return decodeLongArray(buffer);
      case DOUBLE_ARRAY:
        return decodeDoubleArray(buffer);
      case STRING_ARRAY:
        return decodeStringArray(buffer);
      case ZERO_ARRAY:
        buffer.position(buffer.position() - Byte.BYTES);
        return ZeroData.newInstance(ZeroDataType.ZERO_ARRAY, decodeZeroArray(buffer));
      case ZERO_OBJECT:
        buffer.position(buffer.position() - Byte.BYTES);
        return ZeroData.newInstance(ZeroDataType.ZERO_OBJECT, decodeZeroObject(buffer));
      default:
        return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static ByteBuffer encodeObject(ByteBuffer buffer, ZeroDataType type, Object element) {
    switch (type) {
      case NULL:
        buffer = encodeNull(buffer);
        break;
      case BOOLEAN:
        buffer = encodeBoolean(buffer, (Boolean) element);
        break;
      case BYTE:
        buffer = encodeByte(buffer, (Byte) element);
        break;
      case SHORT:
        buffer = encodeShort(buffer, (Short) element);
        break;
      case INTEGER:
        buffer = encodeInteger(buffer, (Integer) element);
        break;
      case LONG:
        buffer = encodeLong(buffer, (Long) element);
        break;
      case FLOAT:
        buffer = encodeFloat(buffer, (Float) element);
        break;
      case DOUBLE:
        buffer = encodeDouble(buffer, (Double) element);
        break;
      case STRING:
        buffer = encodeString(buffer, (String) element);
        break;
      case BOOLEAN_ARRAY:
        buffer = encodeBooleanArray(buffer, (Collection<Boolean>) element);
        break;
      case BYTE_ARRAY:
        buffer = encodeByteArray(buffer, (byte[]) element);
        break;
      case SHORT_ARRAY:
        buffer = encodeShortArray(buffer, (Collection<Short>) element);
        break;
      case INTEGER_ARRAY:
        buffer = encodeIntegerArray(buffer, (Collection<Integer>) element);
        break;
      case LONG_ARRAY:
        buffer = encodeLongArray(buffer, (Collection<Long>) element);
        break;
      case FLOAT_ARRAY:
        buffer = encodeFloatArray(buffer, (Collection<Float>) element);
        break;
      case DOUBLE_ARRAY:
        buffer = encodeDoubleArray(buffer, (Collection<Double>) element);
        break;
      case STRING_ARRAY:
        buffer = encodeStringArray(buffer, (Collection<String>) element);
        break;
      case ZERO_ARRAY:
        buffer = appendBinaryToBuffer(buffer, arrayToBinary((ZeroArray) element));
        break;
      case ZERO_OBJECT:
        buffer = appendBinaryToBuffer(buffer, objectToBinary((ZeroObject) element));
        break;
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported data type: %s", type));
    }

    return buffer;
  }

  private static ZeroData decodeNull(ByteBuffer buffer) {
    return ZeroData.newInstance(ZeroDataType.NULL, null);
  }

  private static ZeroData decodeBoolean(ByteBuffer buffer) {
    var bool = buffer.get();
    Boolean element = null;

    if (bool == 0) {
      element = Boolean.FALSE;
    } else {
      if (bool != 1) {
        throw new IllegalStateException(
            String.format("Expected value of 0 or 1, but found: %d", bool));
      }

      element = Boolean.TRUE;
    }

    return ZeroData.newInstance(ZeroDataType.BOOLEAN, element);
  }

  private static ZeroData decodeByte(ByteBuffer buffer) {
    var element = buffer.get();
    return ZeroData.newInstance(ZeroDataType.BYTE, element);
  }

  private static ZeroData decodeShort(ByteBuffer buffer) {
    var element = buffer.getShort();
    return ZeroData.newInstance(ZeroDataType.SHORT, element);
  }

  private static ZeroData decodeInteger(ByteBuffer buffer) {
    var element = buffer.getInt();
    return ZeroData.newInstance(ZeroDataType.INTEGER, element);
  }

  private static ZeroData decodeLong(ByteBuffer buffer) {
    var element = buffer.getLong();
    return ZeroData.newInstance(ZeroDataType.LONG, element);
  }

  private static ZeroData decodeFloat(ByteBuffer buffer) {
    var element = buffer.getFloat();
    return ZeroData.newInstance(ZeroDataType.FLOAT, element);
  }

  private static ZeroData decodeDouble(ByteBuffer buffer) {
    var element = buffer.getDouble();
    return ZeroData.newInstance(ZeroDataType.DOUBLE, element);
  }

  private static ZeroData decodeString(ByteBuffer buffer) {
    var strLen = buffer.getShort();

    if (strLen < 0) {
      throw new IllegalStateException(
          String.format("The length of string is incorrect: %d", strLen));
    }

    var strData = new byte[strLen];
    buffer.get(strData, 0, strLen);
    var element = new String(strData);

    return ZeroData.newInstance(ZeroDataType.STRING, element);
  }

  private static ZeroData decodeBooleanArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<Boolean>();

    for (int i = 0; i < collectionSize; ++i) {
      var bool = buffer.get();
      if (bool == 0) {
        element.add(false);
      } else {
        if (bool != 1) {
          throw new IllegalStateException(
              String.format("Expected value of 0 or 1, but found: %d", bool));
        }

        element.add(true);
      }
    }

    return ZeroData.newInstance(ZeroDataType.BOOLEAN_ARRAY, element);
  }

  private static ZeroData decodeByteArray(ByteBuffer buffer) {
    var arraySize = buffer.getInt();
    if (arraySize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create an array with negative size value: %d", arraySize));
    }

    var byteData = new byte[arraySize];
    buffer.get(byteData, 0, arraySize);

    return ZeroData.newInstance(ZeroDataType.BYTE_ARRAY, byteData);
  }

  private static ZeroData decodeShortArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<Short>();

    for (int i = 0; i < collectionSize; ++i) {
      var shortValue = buffer.getShort();
      element.add(shortValue);
    }

    return ZeroData.newInstance(ZeroDataType.SHORT_ARRAY, element);
  }

  private static ZeroData decodeIntegerArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<Integer>();

    for (int i = 0; i < collectionSize; ++i) {
      var intValue = buffer.getInt();
      element.add(intValue);
    }

    return ZeroData.newInstance(ZeroDataType.INTEGER_ARRAY, element);
  }

  private static ZeroData decodeLongArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<Long>();

    for (int i = 0; i < collectionSize; ++i) {
      var longValue = buffer.getLong();
      element.add(longValue);
    }

    return ZeroData.newInstance(ZeroDataType.LONG_ARRAY, element);
  }

  private static ZeroData decodeFloatArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<Float>();

    for (int i = 0; i < collectionSize; ++i) {
      var floatValue = buffer.getFloat();
      element.add(floatValue);
    }

    return ZeroData.newInstance(ZeroDataType.FLOAT_ARRAY, element);
  }

  private static ZeroData decodeDoubleArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<Double>();

    for (int i = 0; i < collectionSize; ++i) {
      var doubleValue = buffer.getDouble();
      element.add(doubleValue);
    }

    return ZeroData.newInstance(ZeroDataType.DOUBLE_ARRAY, element);
  }

  private static ZeroData decodeStringArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var element = new ArrayList<String>();

    for (int i = 0; i < collectionSize; ++i) {
      var strLen = buffer.getShort();
      if (strLen < 0) {
        throw new IllegalStateException(
            String.format("The length of string is incorrect: %d", strLen));
      }

      var strData = new byte[strLen];
      buffer.get(strData, 0, strLen);
      var stringValue = new String(strData);
      element.add(stringValue);
    }

    return ZeroData.newInstance(ZeroDataType.STRING_ARRAY, element);
  }

  private static ZeroArray decodeZeroArray(ByteBuffer buffer) {
    var zeroArray = ZeroArrayImpl.newInstance();
    var headerByte = buffer.get();

    if (ZeroDataType.getByValue(headerByte) != ZeroDataType.ZERO_ARRAY) {
      throw new IllegalStateException(
          String.format("Invalid ZeroDataType. Expected: %s, value: %d, but found: %s, value: %d",
              ZeroDataType.ZERO_ARRAY, ZeroDataType.ZERO_ARRAY.getValue(),
              ZeroDataType.getByValue(headerByte).toString(), headerByte));
    }

    var arraySize = buffer.getShort();
    if (arraySize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create an array with negative size value: %d", arraySize));
    }

    try {
      for (int i = 0; i < arraySize; ++i) {
        var zeroData = decodeObject(buffer);
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

  private static ZeroObject decodeZeroObject(ByteBuffer buffer) {
    var zeroObject = ZeroObjectImpl.newInstance();
    var headerByte = buffer.get();

    if (ZeroDataType.getByValue(headerByte) != ZeroDataType.ZERO_OBJECT) {
      throw new IllegalStateException(
          String.format("Invalid ZeroDataType. Expected: %s, value: %d, but found: %s, value: %d",
              ZeroDataType.ZERO_OBJECT, ZeroDataType.ZERO_OBJECT.getValue(),
              ZeroDataType.getByValue(headerByte), headerByte));
    }

    var objectSize = buffer.getShort();
    if (objectSize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create an object with negative size value: %d", objectSize));
    }

    try {
      for (int i = 0; i < objectSize; ++i) {
        var keySize = buffer.getShort();
        var keyData = new byte[keySize];
        buffer.get(keyData, 0, keyData.length);
        var key = new String(keyData);
        var zeroData = decodeObject(buffer);

        if (zeroData == null) {
          throw new IllegalStateException(
              String.format("Unable to decode value for key: %s", keyData));
        }

        zeroObject.putZeroData(key, zeroData);
      }

      return zeroObject;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private static short getCollectionSize(ByteBuffer buffer) {
    var collectionSize = buffer.getShort();
    if (collectionSize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create a collection with negative size value: %d",
              collectionSize));
    }

    return collectionSize;
  }

  private static ByteBuffer encodeNull(ByteBuffer buffer) {
    return appendBinaryToBuffer(buffer, new byte[1]);
  }

  private static ByteBuffer encodeBoolean(ByteBuffer buffer, Boolean element) {
    var data = new byte[] {(byte) ZeroDataType.BOOLEAN.getValue(), (byte) (element ? 1 : 0)};
    return appendBinaryToBuffer(buffer, data);
  }

  private static ByteBuffer encodeByte(ByteBuffer buffer, Byte element) {
    var data = new byte[] {(byte) ZeroDataType.BYTE.getValue(), element};
    return appendBinaryToBuffer(buffer, data);
  }

  private static ByteBuffer encodeShort(ByteBuffer buffer, Short element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES);
    buf.put((byte) ZeroDataType.SHORT.getValue());
    buf.putShort(element);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeInteger(ByteBuffer buffer, Integer element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
    buf.put((byte) ZeroDataType.INTEGER.getValue());
    buf.putInt(element);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeLong(ByteBuffer buffer, Long element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
    buf.put((byte) ZeroDataType.LONG.getValue());
    buf.putLong(element);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeFloat(ByteBuffer buffer, Float element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Float.BYTES);
    buf.put((byte) ZeroDataType.FLOAT.getValue());
    buf.putFloat(element);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeDouble(ByteBuffer buffer, Double element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Double.BYTES);
    buf.put((byte) ZeroDataType.DOUBLE.getValue());
    buf.putDouble(element);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeString(ByteBuffer buffer, String element) {
    var stringBytes = element.getBytes();
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + stringBytes.length);
    buf.put((byte) ZeroDataType.STRING.getValue());
    buf.putShort((short) stringBytes.length);
    buf.put(stringBytes);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeBooleanArray(ByteBuffer buffer, Collection<Boolean> element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + element.size());
    buf.put((byte) ZeroDataType.BOOLEAN_ARRAY.getValue());
    buf.putShort((short) element.size());
    var iterator = element.iterator();

    while (iterator.hasNext()) {
      var boolValue = iterator.next();
      buf.put((byte) (boolValue ? 1 : 0));
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeByteArray(ByteBuffer buffer, byte[] element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + element.length);
    buf.put((byte) ZeroDataType.BYTE_ARRAY.getValue());
    buf.putInt(element.length);
    buf.put(element);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeShortArray(ByteBuffer buffer, Collection<Short> element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Short.BYTES * element.size());
    buf.put((byte) ZeroDataType.SHORT_ARRAY.getValue());
    buf.putShort((short) element.size());
    var iterator = element.iterator();

    while (iterator.hasNext()) {
      var shortValue = iterator.next();
      buf.putShort(shortValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeIntegerArray(ByteBuffer buffer, Collection<Integer> element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Integer.BYTES * element.size());
    buf.put((byte) ZeroDataType.INTEGER_ARRAY.getValue());
    buf.putShort((short) element.size());
    var iterator = element.iterator();

    while (iterator.hasNext()) {
      var integerValue = iterator.next();
      buf.putInt(integerValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeLongArray(ByteBuffer buffer, Collection<Long> element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Long.BYTES * element.size());
    buf.put((byte) ZeroDataType.LONG_ARRAY.getValue());
    buf.putShort((short) element.size());
    var iterator = element.iterator();

    while (iterator.hasNext()) {
      var longValue = iterator.next();
      buf.putLong(longValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeFloatArray(ByteBuffer buffer, Collection<Float> element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Float.BYTES * element.size());
    buf.put((byte) ZeroDataType.FLOAT_ARRAY.getValue());
    buf.putShort((short) element.size());
    var iterator = element.iterator();

    while (iterator.hasNext()) {
      var floatValue = iterator.next();
      buf.putFloat(floatValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeDoubleArray(ByteBuffer buffer, Collection<Double> element) {
    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + Double.BYTES * element.size());
    buf.put((byte) ZeroDataType.DOUBLE_ARRAY.getValue());
    buf.putShort((short) element.size());
    var iterator = element.iterator();

    while (iterator.hasNext()) {
      var doubleValue = iterator.next();
      buf.putDouble(doubleValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeStringArray(ByteBuffer buffer, Collection<String> collection) {
    var totalStringsLengthInBytes = 0;
    byte[] stringInBinary = null;

    for (var iterator = collection.iterator(); iterator
        .hasNext(); totalStringsLengthInBytes += Short.BYTES + stringInBinary.length) {
      var item = iterator.next();
      stringInBinary = item.getBytes();
    }

    var buf = ByteBuffer.allocate(Byte.BYTES + Short.BYTES + totalStringsLengthInBytes);
    buf.put((byte) ZeroDataType.STRING_ARRAY.getValue());
    buf.putShort((short) collection.size());
    collection.forEach(string -> {
      var bytes = string.getBytes();
      buf.putShort((short) bytes.length);
      buf.put(bytes);
    });

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeZeroObjectKey(ByteBuffer buffer, String key) {
    var buf = ByteBuffer.allocate(Short.BYTES + key.length());
    buf.putShort((short) key.length());
    buf.put(key.getBytes());

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer appendBinaryToBuffer(ByteBuffer buffer, byte[] binary) {
    if (buffer.remaining() < binary.length) {
      int newSize = BUFFER_CHUNK_BYTES;
      if (newSize < binary.length) {
        newSize = binary.length;
      }

      var newBuffer = ByteBuffer.allocate(buffer.capacity() + newSize);
      buffer.flip();
      newBuffer.put(buffer);
      buffer = newBuffer;
    }

    buffer.put(binary);
    return buffer;
  }
}
