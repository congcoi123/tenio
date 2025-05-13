/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
import java.util.Map;
import java.util.Objects;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.ImmutableMapValue;
import org.msgpack.value.ImmutableValue;
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

  /**
   * Converting an object ({@link Map}) to array of bytes data.
   *
   * @param packer an instance of {@link MessageBufferPacker}
   * @param map    an object in {@link Map} type
   * @return an array of bytes data
   */
  public static byte[] pack(MessageBufferPacker packer, Map<String, Object> map) {
    byte[] binaries;
    try {
      packer.packMapHeader(map.size());
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        // Single value
        if (Objects.isNull(entry.getValue())) {
          packer.packString(entry.getKey());
          packer.packNil();
        } else if (entry.getValue() instanceof Boolean) {
          packer.packString(entry.getKey());
          packer.packBoolean((boolean) entry.getValue());
        } else if (entry.getValue() instanceof Byte) {
          packer.packString(entry.getKey());
          packer.packByte((byte) entry.getValue());
        } else if (entry.getValue() instanceof Short) {
          packer.packString(entry.getKey());
          packer.packShort((short) entry.getValue());
        } else if (entry.getValue() instanceof Integer) {
          packer.packString(entry.getKey());
          packer.packInt((int) entry.getValue());
        } else if (entry.getValue() instanceof Float) {
          packer.packString(entry.getKey());
          packer.packFloat((float) entry.getValue());
        } else if (entry.getValue() instanceof Long) {
          packer.packString(entry.getKey());
          packer.packLong((long) entry.getValue());
        } else if (entry.getValue() instanceof Double) {
          packer.packString(entry.getKey());
          packer.packDouble((double) entry.getValue());
        } else if (entry.getValue() instanceof String) {
          packer.packString(entry.getKey());
          packer.packString((String) entry.getValue());

          // Multiple values (array)
        } else if (entry.getValue() instanceof boolean[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (boolean value : values) {
            packer.packBoolean(value);
          }
        } else if (entry.getValue() instanceof byte[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (byte value : values) {
            packer.packByte(value);
          }
        } else if (entry.getValue() instanceof short[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (short value : values) {
            packer.packShort(value);
          }
        } else if (entry.getValue() instanceof int[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (int value : values) {
            packer.packInt(value);
          }
        } else if (entry.getValue() instanceof float[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (float value : values) {
            packer.packFloat(value);
          }
        } else if (entry.getValue() instanceof long[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (long value : values) {
            packer.packLong(value);
          }
        } else if (entry.getValue() instanceof double[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (double value : values) {
            packer.packDouble(value);
          }
        } else if (entry.getValue() instanceof String[] values) {
          packer.packString(entry.getKey());
          packer.packArrayHeader(values.length);
          for (String value : values) {
            packer.packString(value);
          }

          // MsgPack Map
        } else if (entry.getValue() instanceof MsgPackMap msgPackMap) {
          packer.packString(entry.getKey());
          pack(packer, msgPackMap);
        } else {
          throw new UnsupportedMsgPackDataTypeException();
        }
      }
      binaries = packer.toByteArray();
      packer.close();
    } catch (IOException exception) {
      throw new MsgPackOperationException(exception);
    }
    return binaries;
  }

  /**
   * Converting an array of bytes data to a {@link MsgPackMap} object.
   *
   * @param binaries an array of bytes
   * @return an object in map type
   */
  public static MsgPackMap unpack(byte[] binaries) {
    MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(binaries);
    MsgPackMap msgPackMap;
    try {
      int size = unpacker.unpackMapHeader();
      msgPackMap = new MsgPackMap();
      for (int indexKey = 0; indexKey < size; indexKey++) {
        String key = unpacker.unpackString();
        ImmutableValue value = unpacker.unpackValue();
        // Assign types
        ValueType valueType = value.getValueType();
        switch (valueType) {
          case NIL -> msgPackMap.putNull(key);
          case BOOLEAN -> msgPackMap.putBoolean(key, value.asBooleanValue().getBoolean());
          case INTEGER -> msgPackMap.putInteger(key, value.asIntegerValue().asInt());
          case FLOAT -> msgPackMap.putFloat(key, value.asFloatValue().toFloat());
          case STRING -> msgPackMap.putString(key, value.asStringValue().asString());
          case ARRAY -> {
            ArrayValue arrayValues = value.asArrayValue();
            int arrayIndex = 0;
            for (Value arrayValue : arrayValues) {
              ValueType arrayValueType = arrayValue.getValueType();
              // Initialize arrays
              if (!msgPackMap.contains(key)) {
                switch (arrayValueType) {
                  case BOOLEAN -> msgPackMap.putBooleanArray(key, new boolean[arrayValues.size()]);
                  case INTEGER -> msgPackMap.putIntegerArray(key, new int[arrayValues.size()]);
                  case FLOAT -> msgPackMap.putFloatArray(key, new float[arrayValues.size()]);
                  case STRING -> msgPackMap.putStringArray(key, new String[arrayValues.size()]);
                }
              }
              // Put elements into arrays
              switch (arrayValueType) {
                case BOOLEAN -> {
                  boolean[] primitiveArray = msgPackMap.getBooleanArray(key);
                  primitiveArray[arrayIndex] = arrayValue.asBooleanValue().getBoolean();
                }
                case INTEGER -> {
                  int[] primitiveArray = msgPackMap.getIntegerArray(key);
                  primitiveArray[arrayIndex] = arrayValue.asIntegerValue().asInt();
                }
                case FLOAT -> {
                  float[] primitiveArray = msgPackMap.getFloatArray(key);
                  primitiveArray[arrayIndex] = arrayValue.asFloatValue().toFloat();
                }
                case STRING -> {
                  String[] primitiveArray = msgPackMap.getStringArray(key);
                  primitiveArray[arrayIndex] = arrayValue.asStringValue().asString();
                }
              }
              arrayIndex++;
            }
          }
          case MAP -> {
            ImmutableMapValue mapValue = value.asMapValue();
            msgPackMap.putMsgPackMap(key, unpack(mapValue));
          }
        }
      }
      unpacker.close();
    } catch (IOException exception) {
      throw new MsgPackOperationException(exception);
    }
    return msgPackMap;
  }

  private static MsgPackMap unpack(ImmutableMapValue map) {
    MsgPackMap msgPackMap;
    msgPackMap = new MsgPackMap();
    for (Map.Entry<Value, Value> entry : map.entrySet()) {
      String key = entry.getKey().asStringValue().asString();
      ImmutableValue value = entry.getValue().immutableValue();
      // Assign types
      ValueType valueType = value.getValueType();
      switch (valueType) {
        case NIL -> msgPackMap.putNull(key);
        case BOOLEAN -> msgPackMap.putBoolean(key, value.asBooleanValue().getBoolean());
        case INTEGER -> msgPackMap.putInteger(key, value.asIntegerValue().asInt());
        case FLOAT -> msgPackMap.putFloat(key, value.asFloatValue().toFloat());
        case STRING -> msgPackMap.putString(key, value.asStringValue().asString());
        case ARRAY -> {
          ArrayValue arrayValues = value.asArrayValue();
          int arrayIndex = 0;
          for (Value arrayValue : arrayValues) {
            ValueType arrayValueType = arrayValue.getValueType();
            // Initialize arrays
            if (!msgPackMap.contains(key)) {
              switch (arrayValueType) {
                case BOOLEAN -> msgPackMap.putBooleanArray(key, new boolean[arrayValues.size()]);
                case INTEGER -> msgPackMap.putIntegerArray(key, new int[arrayValues.size()]);
                case FLOAT -> msgPackMap.putFloatArray(key, new float[arrayValues.size()]);
                case STRING -> msgPackMap.putStringArray(key, new String[arrayValues.size()]);
              }
            }
            // Put elements into arrays
            switch (arrayValueType) {
              case BOOLEAN -> {
                boolean[] primitiveArray = msgPackMap.getBooleanArray(key);
                primitiveArray[arrayIndex] = arrayValue.asBooleanValue().getBoolean();
              }
              case INTEGER -> {
                int[] primitiveArray = msgPackMap.getIntegerArray(key);
                primitiveArray[arrayIndex] = arrayValue.asIntegerValue().asInt();
              }
              case FLOAT -> {
                float[] primitiveArray = msgPackMap.getFloatArray(key);
                primitiveArray[arrayIndex] = arrayValue.asFloatValue().toFloat();
              }
              case STRING -> {
                String[] primitiveArray = msgPackMap.getStringArray(key);
                primitiveArray[arrayIndex] = arrayValue.asStringValue().asString();
              }
            }
            arrayIndex++;
          }
        }
      }
    }
    return msgPackMap;
  }
}
