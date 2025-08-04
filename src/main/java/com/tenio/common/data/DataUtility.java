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

package com.tenio.common.data;

import com.tenio.common.data.msgpack.MsgPackUtility;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.data.zero.ZeroArray;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.data.zero.implement.ZeroArrayImpl;
import com.tenio.common.data.zero.implement.ZeroMapImpl;
import com.tenio.common.data.zero.utility.ZeroUtility;

/**
 * This class provides all necessary methods to work with the data elements.
 */
public final class DataUtility {

  private DataUtility() {
    throw new UnsupportedOperationException("This class does not support creating a new instance");
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
   * Creates a new instance of {@link MsgPackMap} class.
   *
   * @return new instance of msgpck map
   */
  public static MsgPackMap newMsgMap() {
    return MsgPackMap.newInstance();
  }

  /**
   * Deserializes a stream of bytes to a zero collection.
   *
   * @param type     the serialization tool is using which is declared by {@link DataType}
   * @param binaries the stream of bytes
   * @return a new collection instance
   */
  public static DataCollection binariesToCollection(DataType type, byte[] binaries) {
    return switch (type) {
      case ZERO -> ZeroUtility.binariesToCollection(binaries);
      case MSG_PACK -> MsgPackUtility.deserialize(binaries);
    };
  }
}
