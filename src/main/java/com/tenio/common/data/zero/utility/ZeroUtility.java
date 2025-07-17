/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.common.data.zero.utility;

import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.DataCollection;
import com.tenio.common.data.zero.ZeroElement;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.ZeroType;
import com.tenio.common.data.zero.implement.ZeroArrayImpl;
import com.tenio.common.data.zero.implement.ZeroElementImpl;
import com.tenio.common.data.zero.implement.ZeroMapImpl;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class provides all necessary methods to work with the self-definition data elements.
 */
public final class ZeroUtility {

  private static final int BUFFER_CHUNK_BYTES = 512;
  private static final int ENCODE_SHORT_BYTES = Byte.BYTES + Short.BYTES;
  private static final int ENCODE_INTEGER_BYTES = Byte.BYTES + Integer.BYTES;
  private static final int ENCODE_LONG_BYTES = Byte.BYTES + Long.BYTES;
  private static final int ENCODE_FLOAT_BYTES = Byte.BYTES + Float.BYTES;
  private static final int ENCODE_DOUBLE_BYTES = Byte.BYTES + Double.BYTES;
  private static final int ENCODE_HEADER_STRING_BYTES = Byte.BYTES + Short.BYTES;
  private static final int ENCODE_HEADER_BOOLEAN_ARRAY_BYTES = Byte.BYTES + Short.BYTES;
  private static final int ENCODE_HEADER_BYTE_ARRAY_BYTES = Byte.BYTES + Integer.BYTES;
  private static final int ENCODE_HEADER_NUMERIC_ARRAY_BYTES = Byte.BYTES + Short.BYTES;
  private static final int ENCODE_HEADER_STRING_ARRAY_BYTES = Byte.BYTES + Short.BYTES;

  private ZeroUtility() {
    throw new UnsupportedOperationException("This class does not support to create an instance");
  }

  /**
   * Creates a new instance of {@link ZeroElement} class.
   *
   * @param type the type of element in {@link ZeroType}
   * @param data the data of element in variety of types
   * @return new instance of zero element
   * @see Boolean
   * @see Byte
   * @see Short
   * @see Integer
   * @see Long
   * @see Float
   * @see Double
   * @see String
   * @see Collection
   * @see ZeroArray
   * @see ZeroMap
   */
  public static ZeroElement newZeroElement(ZeroType type, Object data) {
    return new ZeroElementImpl(type, data);
  }

  /**
   * Creates a new instance of {@link ZeroArray} class.
   *
   * @return new instance of zero array
   */
  public static ZeroArray newZeroArray() {
    return new ZeroArrayImpl();
  }

  /**
   * Creates a new instance of {@link ZeroMap} class.
   *
   * @return new instance of zero map
   */
  public static ZeroMap newZeroMap() {
    return new ZeroMapImpl();
  }

  /**
   * Deserializes a stream of bytes to a zero collection.
   *
   * @param binary the stream of bytes
   * @return a new zero collection instance
   */
  public static DataCollection binaryToCollection(byte[] binary) {
    return switch (ZeroType.getByValue(binary[0])) {
      case ZERO_MAP -> binaryToMap(binary);
      case ZERO_ARRAY -> binaryToArray(binary);
      default -> throw new UnsupportedOperationException(
          String.format("Unsupported value: %s", ZeroType.getByValue(binary[0])));
    };
  }

  /**
   * Deserializes a stream of bytes to a zero array.
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
   * Deserializes a stream of bytes to a zero map.
   *
   * @param binary the stream of bytes
   * @return a new zero map instance
   */
  public static ZeroMap binaryToMap(byte[] binary) {
    if (binary.length < 3) {
      throw new IllegalStateException(String.format(
          "Unable to decode a ZeroMap because binary data size is not big enough to work on it"
              + ". Size: %d bytes",
          binary.length));
    }

    var buffer = ByteBuffer.allocate(binary.length);
    buffer.put(binary);
    buffer.flip();

    return decodeZeroMap(buffer);
  }

  /**
   * Serialize a map to a stream of bytes.
   *
   * @param map the map
   * @return the stream of bytes converted from the map
   */
  public static byte[] mapToBinary(ZeroMap map) {
    var buffer = ByteBuffer.allocate(BUFFER_CHUNK_BYTES);
    buffer.put((byte) ZeroType.ZERO_MAP.getValue());
    buffer.putShort((short) map.size());

    return mapToBinary(map, buffer);
  }

  private static byte[] mapToBinary(ZeroMap map, ByteBuffer buffer) {
    var keys = map.getKeys();
    ZeroElement zeroElement;
    Object data;

    for (var iterator = keys.iterator(); iterator
        .hasNext(); buffer = encodeElement(buffer, zeroElement.getType(), data)) {
      var key = iterator.next();
      zeroElement = map.getZeroElement(key);
      data = zeroElement.getData();
      buffer = encodeZeroMapKey(buffer, key);
    }

    var position = buffer.position();
    var result = new byte[position];
    buffer.flip();
    buffer.get(result, 0, position);

    return result;
  }

  /**
   * Serializes an array to a stream of bytes.
   *
   * @param array the array
   * @return the stream of bytes converted from the array
   */
  public static byte[] arrayToBinary(ZeroArray array) {
    var buffer = ByteBuffer.allocate(BUFFER_CHUNK_BYTES);
    buffer.put((byte) ZeroType.ZERO_ARRAY.getValue());
    buffer.putShort((short) array.size());

    return arrayToBinary(array, buffer);
  }

  private static byte[] arrayToBinary(ZeroArray array, ByteBuffer buffer) {
    ZeroElement zeroElement;
    Object data;

    for (var iterator = array.iterator(); iterator
        .hasNext(); buffer = encodeElement(buffer, zeroElement.getType(), data)) {
      zeroElement = iterator.next();
      data = zeroElement.getData();
    }

    var position = buffer.position();
    var result = new byte[position];
    buffer.flip();
    buffer.get(result, 0, position);

    return result;
  }

  private static ZeroElement decodeElement(ByteBuffer buffer) throws RuntimeException {
    var headerByte = buffer.get();
    var type = ZeroType.getByValue(headerByte);
    if (type == null) {
      return null;
    }

    return switch (type) {
      case NULL -> decodeNull();
      case BOOLEAN -> decodeBoolean(buffer);
      case BYTE -> decodeByte(buffer);
      case SHORT -> decodeShort(buffer);
      case INTEGER -> decodeInteger(buffer);
      case FLOAT -> decodeFloat(buffer);
      case LONG -> decodeLong(buffer);
      case DOUBLE -> decodeDouble(buffer);
      case STRING -> decodeString(buffer);
      case BOOLEAN_ARRAY -> decodeBooleanArray(buffer);
      case BYTE_ARRAY -> decodeByteArray(buffer);
      case SHORT_ARRAY -> decodeShortArray(buffer);
      case INTEGER_ARRAY -> decodeIntegerArray(buffer);
      case FLOAT_ARRAY -> decodeFloatArray(buffer);
      case LONG_ARRAY -> decodeLongArray(buffer);
      case DOUBLE_ARRAY -> decodeDoubleArray(buffer);
      case STRING_ARRAY -> decodeStringArray(buffer);
      case ZERO_ARRAY -> {
        buffer.position(buffer.position() - Byte.BYTES);
        yield newZeroElement(ZeroType.ZERO_ARRAY, decodeZeroArray(buffer));
      }
      case ZERO_MAP -> {
        buffer.position(buffer.position() - Byte.BYTES);
        yield newZeroElement(ZeroType.ZERO_MAP, decodeZeroMap(buffer));
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static ByteBuffer encodeElement(ByteBuffer buffer, ZeroType type, Object data) {
    switch (type) {
      case NULL -> buffer = encodeNull(buffer);
      case BOOLEAN -> buffer = encodeBoolean(buffer, (Boolean) data);
      case BYTE -> buffer = encodeByte(buffer, (Byte) data);
      case SHORT -> buffer = encodeShort(buffer, (Short) data);
      case INTEGER -> buffer = encodeInteger(buffer, (Integer) data);
      case LONG -> buffer = encodeLong(buffer, (Long) data);
      case FLOAT -> buffer = encodeFloat(buffer, (Float) data);
      case DOUBLE -> buffer = encodeDouble(buffer, (Double) data);
      case STRING -> buffer = encodeString(buffer, (String) data);
      case BOOLEAN_ARRAY -> buffer = encodeBooleanArray(buffer, (Collection<Boolean>) data);
      case BYTE_ARRAY -> buffer = encodeByteArray(buffer, (byte[]) data);
      case SHORT_ARRAY -> buffer = encodeShortArray(buffer, (Collection<Short>) data);
      case INTEGER_ARRAY -> buffer = encodeIntegerArray(buffer, (Collection<Integer>) data);
      case LONG_ARRAY -> buffer = encodeLongArray(buffer, (Collection<Long>) data);
      case FLOAT_ARRAY -> buffer = encodeFloatArray(buffer, (Collection<Float>) data);
      case DOUBLE_ARRAY -> buffer = encodeDoubleArray(buffer, (Collection<Double>) data);
      case STRING_ARRAY -> buffer = encodeStringArray(buffer, (Collection<String>) data);
      case ZERO_ARRAY -> buffer = appendBinaryToBuffer(buffer, arrayToBinary((ZeroArray) data));
      case ZERO_MAP -> buffer = appendBinaryToBuffer(buffer, mapToBinary((ZeroMap) data));
    }

    return buffer;
  }

  private static ZeroElement decodeNull() {
    return newZeroElement(ZeroType.NULL, null);
  }

  private static ZeroElement decodeBoolean(ByteBuffer buffer) {
    var bool = buffer.get();
    Boolean data;

    if (bool == 0) {
      data = Boolean.FALSE;
    } else {
      if (bool != 1) {
        throw new IllegalStateException(
            String.format("Expected value of 0 or 1, but found: %d", bool));
      }

      data = Boolean.TRUE;
    }

    return newZeroElement(ZeroType.BOOLEAN, data);
  }

  private static ZeroElement decodeByte(ByteBuffer buffer) {
    var data = buffer.get();
    return newZeroElement(ZeroType.BYTE, data);
  }

  private static ZeroElement decodeShort(ByteBuffer buffer) {
    var data = buffer.getShort();
    return newZeroElement(ZeroType.SHORT, data);
  }

  private static ZeroElement decodeInteger(ByteBuffer buffer) {
    var data = buffer.getInt();
    return newZeroElement(ZeroType.INTEGER, data);
  }

  private static ZeroElement decodeLong(ByteBuffer buffer) {
    var data = buffer.getLong();
    return newZeroElement(ZeroType.LONG, data);
  }

  private static ZeroElement decodeFloat(ByteBuffer buffer) {
    var data = buffer.getFloat();
    return newZeroElement(ZeroType.FLOAT, data);
  }

  private static ZeroElement decodeDouble(ByteBuffer buffer) {
    var data = buffer.getDouble();
    return newZeroElement(ZeroType.DOUBLE, data);
  }

  private static ZeroElement decodeString(ByteBuffer buffer) {
    var strLen = buffer.getShort();

    if (strLen < 0) {
      throw new IllegalStateException(
          String.format("The length of string is incorrect: %d", strLen));
    }

    var strData = new byte[strLen];
    buffer.get(strData, 0, strLen);
    var data = new String(strData);

    return newZeroElement(ZeroType.STRING, data);
  }

  private static ZeroElement decodeBooleanArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<Boolean>();

    for (int i = 0; i < collectionSize; ++i) {
      var bool = buffer.get();
      if (bool == 0) {
        data.add(false);
      } else {
        if (bool != 1) {
          throw new IllegalStateException(
              String.format("Expected value of 0 or 1, but found: %d", bool));
        }

        data.add(true);
      }
    }

    return newZeroElement(ZeroType.BOOLEAN_ARRAY, data);
  }

  private static ZeroElement decodeByteArray(ByteBuffer buffer) {
    var arraySize = buffer.getInt();
    if (arraySize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create an array with negative size value: %d", arraySize));
    }

    var byteData = new byte[arraySize];
    buffer.get(byteData, 0, arraySize);

    return newZeroElement(ZeroType.BYTE_ARRAY, byteData);
  }

  private static ZeroElement decodeShortArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<Short>();

    for (int i = 0; i < collectionSize; ++i) {
      var shortValue = buffer.getShort();
      data.add(shortValue);
    }

    return newZeroElement(ZeroType.SHORT_ARRAY, data);
  }

  private static ZeroElement decodeIntegerArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<Integer>();

    for (int i = 0; i < collectionSize; ++i) {
      var intValue = buffer.getInt();
      data.add(intValue);
    }

    return newZeroElement(ZeroType.INTEGER_ARRAY, data);
  }

  private static ZeroElement decodeLongArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<Long>();

    for (int i = 0; i < collectionSize; ++i) {
      var longValue = buffer.getLong();
      data.add(longValue);
    }

    return newZeroElement(ZeroType.LONG_ARRAY, data);
  }

  private static ZeroElement decodeFloatArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<Float>();

    for (int i = 0; i < collectionSize; ++i) {
      var floatValue = buffer.getFloat();
      data.add(floatValue);
    }

    return newZeroElement(ZeroType.FLOAT_ARRAY, data);
  }

  private static ZeroElement decodeDoubleArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<Double>();

    for (int i = 0; i < collectionSize; ++i) {
      var doubleValue = buffer.getDouble();
      data.add(doubleValue);
    }

    return newZeroElement(ZeroType.DOUBLE_ARRAY, data);
  }

  private static ZeroElement decodeStringArray(ByteBuffer buffer) {
    var collectionSize = getCollectionSize(buffer);
    var data = new ArrayList<String>();

    for (int i = 0; i < collectionSize; ++i) {
      var strLen = buffer.getShort();
      if (strLen < 0) {
        throw new IllegalStateException(
            String.format("The length of string is incorrect: %d", strLen));
      }

      var strData = new byte[strLen];
      buffer.get(strData, 0, strLen);
      var stringValue = new String(strData);
      data.add(stringValue);
    }

    return newZeroElement(ZeroType.STRING_ARRAY, data);
  }

  private static ZeroArray decodeZeroArray(ByteBuffer buffer) {
    var zeroArray = newZeroArray();
    var headerByte = buffer.get();

    if (ZeroType.getByValue(headerByte) != ZeroType.ZERO_ARRAY) {
      throw new IllegalStateException(
          String.format("Invalid ZeroType. Expected: %s, value: %d, but found: %s, value: %d",
              ZeroType.ZERO_ARRAY, ZeroType.ZERO_ARRAY.getValue(),
              ZeroType.getByValue(headerByte) != null ?
                  ZeroType.getByValue(headerByte).toString() : "null", headerByte));
    }

    var arraySize = buffer.getShort();
    if (arraySize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create an array with negative size value: %d", arraySize));
    }

    try {
      for (int i = 0; i < arraySize; ++i) {
        var zeroElement = decodeElement(buffer);
        if (zeroElement == null) {
          throw new IllegalStateException(
              String.format("Unable to not decode ZeroArray item at index: %d", i));
        }

        zeroArray.addZeroElement(zeroElement);
      }

      return zeroArray;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private static ZeroMap decodeZeroMap(ByteBuffer buffer) {
    var zeroMap = newZeroMap();
    var headerByte = buffer.get();

    if (ZeroType.getByValue(headerByte) != ZeroType.ZERO_MAP) {
      throw new IllegalStateException(
          String.format("Invalid ZeroType. Expected: %s, value: %d, but found: %s, value: %d",
              ZeroType.ZERO_MAP, ZeroType.ZERO_MAP.getValue(),
              ZeroType.getByValue(headerByte), headerByte));
    }

    var mapSize = buffer.getShort();
    if (mapSize < 0) {
      throw new NegativeArraySizeException(
          String.format("Could not create an object with negative size value: %d", mapSize));
    }

    try {
      for (int i = 0; i < mapSize; ++i) {
        var keySize = buffer.getShort();
        var keyData = new byte[keySize];
        buffer.get(keyData, 0, keyData.length);
        var key = new String(keyData);
        var zeroElement = decodeElement(buffer);

        if (zeroElement == null) {
          throw new IllegalStateException(
              String.format("Unable to decode value for key: %s", Arrays.toString(keyData)));
        }

        zeroMap.putZeroElement(key, zeroElement);
      }

      return zeroMap;
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

  private static ByteBuffer encodeBoolean(ByteBuffer buffer, Boolean data) {
    var binary = new byte[] {(byte) ZeroType.BOOLEAN.getValue(), (byte) (data ? 1 : 0)};
    return appendBinaryToBuffer(buffer, binary);
  }

  private static ByteBuffer encodeByte(ByteBuffer buffer, Byte data) {
    var binary = new byte[] {(byte) ZeroType.BYTE.getValue(), data};
    return appendBinaryToBuffer(buffer, binary);
  }

  private static ByteBuffer encodeShort(ByteBuffer buffer, Short data) {
    var buf = ByteBuffer.allocate(ENCODE_SHORT_BYTES);
    buf.put((byte) ZeroType.SHORT.getValue());
    buf.putShort(data);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeInteger(ByteBuffer buffer, Integer data) {
    var buf = ByteBuffer.allocate(ENCODE_INTEGER_BYTES);
    buf.put((byte) ZeroType.INTEGER.getValue());
    buf.putInt(data);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeLong(ByteBuffer buffer, Long data) {
    var buf = ByteBuffer.allocate(ENCODE_LONG_BYTES);
    buf.put((byte) ZeroType.LONG.getValue());
    buf.putLong(data);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeFloat(ByteBuffer buffer, Float data) {
    var buf = ByteBuffer.allocate(ENCODE_FLOAT_BYTES);
    buf.put((byte) ZeroType.FLOAT.getValue());
    buf.putFloat(data);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeDouble(ByteBuffer buffer, Double data) {
    var buf = ByteBuffer.allocate(ENCODE_DOUBLE_BYTES);
    buf.put((byte) ZeroType.DOUBLE.getValue());
    buf.putDouble(data);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeString(ByteBuffer buffer, String data) {
    var stringBytes = data.getBytes();
    var buf = ByteBuffer.allocate(ENCODE_HEADER_STRING_BYTES + stringBytes.length);
    buf.put((byte) ZeroType.STRING.getValue());
    buf.putShort((short) stringBytes.length);
    buf.put(stringBytes);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeBooleanArray(ByteBuffer buffer, Collection<Boolean> data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_BOOLEAN_ARRAY_BYTES + data.size());
    buf.put((byte) ZeroType.BOOLEAN_ARRAY.getValue());
    buf.putShort((short) data.size());

    for (Boolean boolValue : data) {
      buf.put((byte) (boolValue ? 1 : 0));
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeByteArray(ByteBuffer buffer, byte[] data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_BYTE_ARRAY_BYTES + data.length);
    buf.put((byte) ZeroType.BYTE_ARRAY.getValue());
    buf.putInt(data.length);
    buf.put(data);

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeShortArray(ByteBuffer buffer, Collection<Short> data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_NUMERIC_ARRAY_BYTES + Short.BYTES * data.size());
    buf.put((byte) ZeroType.SHORT_ARRAY.getValue());
    buf.putShort((short) data.size());

    for (Short shortValue : data) {
      buf.putShort(shortValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeIntegerArray(ByteBuffer buffer, Collection<Integer> data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_NUMERIC_ARRAY_BYTES + Integer.BYTES * data.size());
    buf.put((byte) ZeroType.INTEGER_ARRAY.getValue());
    buf.putShort((short) data.size());

    for (Integer integerValue : data) {
      buf.putInt(integerValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeLongArray(ByteBuffer buffer, Collection<Long> data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_NUMERIC_ARRAY_BYTES + Long.BYTES * data.size());
    buf.put((byte) ZeroType.LONG_ARRAY.getValue());
    buf.putShort((short) data.size());

    for (Long longValue : data) {
      buf.putLong(longValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeFloatArray(ByteBuffer buffer, Collection<Float> data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_NUMERIC_ARRAY_BYTES + Float.BYTES * data.size());
    buf.put((byte) ZeroType.FLOAT_ARRAY.getValue());
    buf.putShort((short) data.size());

    for (Float floatValue : data) {
      buf.putFloat(floatValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeDoubleArray(ByteBuffer buffer, Collection<Double> data) {
    var buf = ByteBuffer.allocate(ENCODE_HEADER_NUMERIC_ARRAY_BYTES + Double.BYTES * data.size());
    buf.put((byte) ZeroType.DOUBLE_ARRAY.getValue());
    buf.putShort((short) data.size());

    for (Double doubleValue : data) {
      buf.putDouble(doubleValue);
    }

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeStringArray(ByteBuffer buffer, Collection<String> collection) {
    var totalStringsLengthInBytes = 0;
    byte[] stringInBinary;

    for (var iterator = collection.iterator(); iterator
        .hasNext(); totalStringsLengthInBytes += Short.BYTES + stringInBinary.length) {
      var item = iterator.next();
      stringInBinary = item.getBytes();
    }

    var buf = ByteBuffer.allocate(ENCODE_HEADER_STRING_ARRAY_BYTES + totalStringsLengthInBytes);
    buf.put((byte) ZeroType.STRING_ARRAY.getValue());
    buf.putShort((short) collection.size());
    collection.forEach(string -> {
      var bytes = string.getBytes();
      buf.putShort((short) bytes.length);
      buf.put(bytes);
    });

    return appendBinaryToBuffer(buffer, buf.array());
  }

  private static ByteBuffer encodeZeroMapKey(ByteBuffer buffer, String key) {
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
