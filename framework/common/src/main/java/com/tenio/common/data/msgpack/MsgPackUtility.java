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

package com.tenio.common.data.msgpack;

import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.exception.MsgPackOperationException;
import com.tenio.common.exception.UnsupportedMsgPackDataTypeException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

/**
 * <a href="https://msgpack.org/index.html">MessagePack</a> is an efficient
 * binary serialization format. It lets you exchange data among multiple
 * languages like JSON. But it's faster and smaller. Small integers are encoded
 * into a single byte, and typical short strings require only one extra byte in
 * addition to the strings themselves. This class helps you convert one system
 * object ({@link Map}) to MsgPack data and vice versa.
 */
public final class MsgPackUtility {

  private MsgPackUtility() {
    throw new UnsupportedOperationException("This class does not support creating a new instance");
  }

  /**
   * Serialize an object to an array of bytes data.
   *
   * @param msgPackMap a {@link MsgPackMap} type object
   * @return an array of bytes data
   */
  public static byte[] serialize(MsgPackMap msgPackMap) {
    return MsgPackConverter.pack(MessagePack.newDefaultBufferPacker(), msgPackMap);
  }

  /**
   * Deserialize an array of bytes data to a {@link MsgPackMap} object.
   *
   * @param binaries an array of bytes data
   * @return a message object in {@link MsgPackMap} type
   */
  public static MsgPackMap deserialize(byte[] binaries) {
    return MsgPackConverter.unpack(binaries);
  }

  /**
   * Retrieves new instance of the {@link MsgPackMap} class.
   *
   * @return an instance
   */
  public static MsgPackMap newMsgPackMap() {
    return MsgPackMap.newInstance();
  }
}

class MsgPackConverter {

  private static final int MAX_INPUT_BYTES = 16 * 1024 * 1024;
  private static final int MAX_COLLECTION_SIZE = 1_000_000;
  private static final int MAX_NESTING_DEPTH = 64;

  /**
   * Converting an object ({@link Map}) to array of bytes data.
   *
   * @param packer an instance of {@link MessageBufferPacker}
   * @param map    an object in {@link Map} type
   * @return an array of bytes data
   */
  public static byte[] pack(MessageBufferPacker packer, Map<String, Object> map) {
    try (packer) {
      packMap(packer, map, 0);
      return packer.toByteArray();
    } catch (IOException exception) {
      throw new MsgPackOperationException(exception);
    }
  }

  /**
   * Converting an array of bytes data to a {@link MsgPackMap} object.
   *
   * @param binaries an array of bytes
   * @return an object in map type
   */
  public static MsgPackMap unpack(byte[] binaries) {
    if (binaries == null || binaries.length == 0 || binaries.length > MAX_INPUT_BYTES) {
      throw new IllegalArgumentException("MessagePack payload size is invalid");
    }
    try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(binaries)) {
      int size = unpacker.unpackMapHeader();
      checkCollectionSize(size);
      var msgPackMap = new MsgPackMap();
      for (int indexKey = 0; indexKey < size; indexKey++) {
        String key = unpacker.unpackString();
        msgPackMap.put(key, decodeValue(unpacker.unpackValue(), 1));
      }
      if (unpacker.hasNext()) {
        throw new IllegalArgumentException("Trailing MessagePack values are not allowed");
      }
      return msgPackMap;
    } catch (IOException exception) {
      throw new MsgPackOperationException(exception);
    }
  }

  private static void packMap(MessageBufferPacker packer, Map<?, ?> map, int depth)
      throws IOException {
    checkDepth(depth);
    checkCollectionSize(map.size());
    packer.packMapHeader(map.size());
    for (var entry : map.entrySet()) {
      if (!(entry.getKey() instanceof String key)) {
        throw new UnsupportedMsgPackDataTypeException();
      }
      packer.packString(key);
      packValue(packer, entry.getValue(), depth + 1);
    }
  }

  private static void packValue(MessageBufferPacker packer, Object value, int depth) throws IOException {
    checkDepth(depth);
    if (value == null) {
      packer.packNil();
    } else if (value instanceof Boolean booleanValue) {
      packer.packBoolean(booleanValue);
    } else if (value instanceof Byte byteValue) {
      packer.packByte(byteValue);
    } else if (value instanceof Short shortValue) {
      packer.packShort(shortValue);
    } else if (value instanceof Integer integerValue) {
      packer.packInt(integerValue);
    } else if (value instanceof Long longValue) {
      packer.packLong(longValue);
    } else if (value instanceof Float floatValue) {
      packer.packFloat(floatValue);
    } else if (value instanceof Double doubleValue) {
      packer.packDouble(doubleValue);
    } else if (value instanceof String stringValue) {
      packer.packString(stringValue);
    } else if (value instanceof byte[] bytes) {
      packer.packBinaryHeader(bytes.length);
      packer.writePayload(bytes);
    } else if (value instanceof Map<?, ?> map) {
      packMap(packer, map, depth);
    } else if (value instanceof Iterable<?> iterable) {
      var values = new ArrayList<>();
      iterable.forEach(values::add);
      checkCollectionSize(values.size());
      packer.packArrayHeader(values.size());
      for (var item : values) {
        packValue(packer, item, depth + 1);
      }
    } else if (value.getClass().isArray()) {
      int length = Array.getLength(value);
      checkCollectionSize(length);
      packer.packArrayHeader(length);
      for (int i = 0; i < length; i++) {
        packValue(packer, Array.get(value, i), depth + 1);
      }
    } else {
      throw new UnsupportedMsgPackDataTypeException();
    }
  }

  private static Object decodeValue(Value value, int depth) {
    checkDepth(depth);
    return switch (value.getValueType()) {
      case NIL -> null;
      case BOOLEAN -> value.asBooleanValue().getBoolean();
      case INTEGER -> value.asIntegerValue().isInIntRange()
          ? value.asIntegerValue().asInt() : value.asIntegerValue().asLong();
      case FLOAT -> value.asFloatValue().toDouble();
      case STRING -> value.asStringValue().asString();
      case BINARY -> value.asBinaryValue().asByteArray();
      case ARRAY -> decodeArray(value.asArrayValue(), depth + 1);
      case MAP -> decodeMap(value, depth + 1);
      case EXTENSION -> throw new UnsupportedMsgPackDataTypeException();
    };
  }

  private static MsgPackMap decodeMap(Value value, int depth) {
    checkDepth(depth);
    var mapValue = value.asMapValue();
    checkCollectionSize(mapValue.size());
    var result = new MsgPackMap();
    for (var entry : mapValue.entrySet()) {
      if (!entry.getKey().isStringValue()) {
        throw new IllegalArgumentException("MessagePack map keys must be strings");
      }
      result.put(entry.getKey().asStringValue().asString(), decodeValue(entry.getValue(), depth + 1));
    }
    return result;
  }

  private static Object decodeArray(ArrayValue array, int depth) {
    checkDepth(depth);
    checkCollectionSize(array.size());
    if (array.size() == 0) {
      return new Object[0];
    }
    ValueType firstType = array.get(0).getValueType();
    boolean homogeneous = true;
    for (int i = 1; i < array.size(); i++) {
      if (array.get(i).getValueType() != firstType) {
        homogeneous = false;
        break;
      }
    }
    if (homogeneous && firstType == ValueType.BOOLEAN) {
      var result = new boolean[array.size()];
      for (int i = 0; i < result.length; i++) {
        result[i] = array.get(i).asBooleanValue().getBoolean();
      }
      return result;
    }
    if (homogeneous && firstType == ValueType.STRING) {
      var result = new String[array.size()];
      for (int i = 0; i < result.length; i++) {
        result[i] = array.get(i).asStringValue().asString();
      }
      return result;
    }
    if (homogeneous && firstType == ValueType.INTEGER) {
      boolean fitsInt = true;
      for (int i = 0; i < array.size(); i++) {
        if (!array.get(i).asIntegerValue().isInIntRange()) {
          fitsInt = false;
          break;
        }
      }
      if (fitsInt) {
        var result = new int[array.size()];
        for (int i = 0; i < result.length; i++) {
          result[i] = array.get(i).asIntegerValue().asInt();
        }
        return result;
      }
      var result = new long[array.size()];
      for (int i = 0; i < result.length; i++) {
        result[i] = array.get(i).asIntegerValue().asLong();
      }
      return result;
    }
    if (homogeneous && firstType == ValueType.FLOAT) {
      var result = new double[array.size()];
      for (int i = 0; i < result.length; i++) {
        result[i] = array.get(i).asFloatValue().toDouble();
      }
      return result;
    }
    var result = new Object[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = decodeValue(array.get(i), depth + 1);
    }
    return result;
  }

  private static void checkDepth(int depth) {
    if (depth > MAX_NESTING_DEPTH) {
      throw new IllegalArgumentException("MessagePack nesting is too deep");
    }
  }

  private static void checkCollectionSize(int size) {
    if (size < 0 || size > MAX_COLLECTION_SIZE) {
      throw new IllegalArgumentException("MessagePack collection is too large: " + size);
    }
  }
}
