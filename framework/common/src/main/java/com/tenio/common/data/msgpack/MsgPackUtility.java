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

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tMap;

import com.tenio.common.data.msgpack.element.MsgPackArray;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import java.io.IOException;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;

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
    return MsgPackConverter.pack(msgPackMap);
  }

  /**
   * Deserialize an array of bytes data to a {@link MsgPackMap} object.
   *
   * @param binaries an array of bytes data
   * @return an message object in {@link MsgPackMap} type
   */
  public static MsgPackMap deserialize(byte[] binaries) {
    var msgObject = MsgPackMap.newInstance();
    var byteArrayInput = ByteArrayInputStream.newInstance();
    var dstMap = MsgPackConverter.unpack(byteArrayInput, binaries);
    if (dstMap == null || dstMap.isEmpty()) {
      return null;
    }
    dstMap.forEach((key, value) -> {
      try {
        msgObject.put(key, MsgPackConverter.valueToObject(value));
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    });
    return msgObject;
  }

  /**
   * Retrieves new instance of the {@link MsgPackMap} class.
   *
   * @return an instance
   */
  public static MsgPackMap newMsgPackMap() {
    return MsgPackMap.newInstance();
  }

  /**
   * Retrieves new instance of the {@link MsgPackArray} class.
   *
   * @return an instance
   */
  public static MsgPackArray newMsgPackArray() {
    return MsgPackArray.newInstance();
  }
}

class MsgPackConverter {

  /**
   * A MsgPack instance.
   */
  private static final MessagePack PACKER = new MessagePack();

  /**
   * Converting an object ({@link Map}) to array of bytes data.
   *
   * @param map an object in {@link Map} type
   * @return an array of bytes data
   */
  public static byte[] pack(Map<String, Object> map) {
    try {
      return PACKER.write(map);
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  /**
   * Converting an array of bytes data to a {@link Map} object.
   *
   * @param byteArrayInput object for handling byte array
   * @param binaries       an array of bytes
   * @return a object in map type
   */
  public static Map<String, Value> unpack(ByteArrayInputStream byteArrayInput, byte[] binaries) {
    var mapTmpl = tMap(TString, TValue);
    try {
      byteArrayInput.reset(binaries);
      var unpacker = PACKER.createUnpacker(byteArrayInput);
      return unpacker.read(mapTmpl);
    } catch (IOException | IllegalArgumentException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  /**
   * Converting value in MsgPack type to its corresponding in Java type.
   *
   * @param value the value in {@link Value} type
   * @return an object in Java type
   */
  public static Object valueToObject(Value value) throws IOException {
    if (value.isNilValue()) {
      return null;
    } else if (value.isRawValue()) {
      // String only
      return value.asRawValue().getString();
    } else if (value.isBooleanValue()) {
      return value.asBooleanValue().getBoolean();
    } else if (value.isFloatValue()) {
      // Float only (4 bytes)
      return value.asFloatValue().getFloat();
    } else if (value.isIntegerValue()) {
      // Integer only (4 bytes)
      return value.asIntegerValue().getInt();
    } else if (value.isArrayValue()) {
      // Convert value to list of objects (MsgPackArray)
      var arrayValue = value.asArrayValue();
      var array = MsgPackArray.newInstance();
      arrayValue.forEach(element -> {
        try {
          array.add(valueToObject(element));
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      });
      return array;
    } else if (value.isMapValue()) {
      var mapValue = value.asMapValue();
      var map = MsgPackMap.newInstance();
      for (Value key : mapValue.keySet()) {
        map.put(key.asRawValue().getString(), valueToObject(mapValue.get(key)));
      }
      return map;
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
