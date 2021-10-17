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

package com.tenio.common.utility;

/**
 * This class provides utility methods to work with stream of bytes.
 */
public final class ByteUtility {

  private ByteUtility() {
    throw new UnsupportedOperationException("This class does not support to create a new instance");
  }

  public static byte[] intToBytes(int value) {
    return new byte[] {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8),
        (byte) value};
  }

  public static int bytesToInt(byte[] bytes) {
    return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8)
        | ((bytes[3] & 0xFF) << 0);
  }

  public static byte[] shortToBytes(short value) {
    return new byte[] {(byte) (value >> 8), (byte) value};
  }

  public static short bytesToShort(byte[] bytes) {
    return (short) (((bytes[0] & 0xFF) << 8) | ((bytes[1] & 0xFF) << 0));
  }

  /**
   * Resizes the current array of bytes, create a new one.
   *
   * @param source   the array of binary source
   * @param position the position in where the new array will start
   * @param newSize  the new array's size
   * @return a new byte array
   */
  public static byte[] resizeBytesArray(byte[] source, int position, int newSize) {
    byte[] binary = new byte[newSize];
    System.arraycopy(source, position, binary, 0, newSize);

    return binary;
  }
}
