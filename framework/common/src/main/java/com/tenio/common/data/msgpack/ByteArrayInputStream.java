/*
 * Copyright 2011 The Buzz Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenio.common.data.msgpack;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
public final class ByteArrayInputStream extends InputStream implements Serializable {

  private static final long serialVersionUID = -5334077014767018880L;

  private int offset;
  private int length;
  private byte[] buffer;

  private ByteArrayInputStream() {
  }

  public static ByteArrayInputStream newInstance() {
    return new ByteArrayInputStream();
  }

  public static ByteArrayInputStream valueOf(byte[] data) throws IllegalArgumentException {
    var array = new ByteArrayInputStream();
    array.reset(data);
    return array;
  }

  public static ByteArrayInputStream valueOf(byte[] data, int offset, int length)
      throws IllegalArgumentException {
    var array = new ByteArrayInputStream();
    array.reset(data, offset, length);
    return array;
  }

  @Override
  public int available() throws IOException {
    return (length - offset);
  }

  @Override
  public long skip(long number) throws IllegalArgumentException {
    if (number < 0) {
      throw new IllegalArgumentException("n [" + number + "] must be >= 0");
    }

    // Calculate remaining skip-able bytes.
    int range = (length - offset);

    // Trim to the smaller of the two values for our skip amount.
    number = (number < range ? number : range);

    // Skip the bytes
    offset += number;

    return number;
  }

  @Override
  public int read() throws IOException {
    return (offset < length ? buffer[offset++] & 0xFF : -1);
  }

  @Override
  public int read(byte[] buffer, int offset, int length)
      throws IllegalArgumentException, IOException {
    if (buffer == null) {
      throw new IllegalArgumentException("buffer cannot be null");
    }
    if (offset < 0 || length < 0 || (offset + length) > buffer.length) {
      throw new IllegalArgumentException(
          "offset [" + offset + "] and length [" + length + "] must be >= 0 and (offset + length)["
              + (offset + length) + "] must be <= buffer.length [" + buffer.length + "]");
    }

    // Calculate bytes remaining in the stream.
    int r = (this.length - this.offset);

    /*
     * If no bytes are remaining, update the length we return to -1, otherwise begin
     * the copy operation on the remaining bytes.
     */
    if (r < 1) {
      length = -1;
    } else {
      /*
       * Trim the copy length to the smaller of the two values: how many bytes were
       * requested or how many are left.
       */
      length = Math.min(length, r);

      // Copy data into buffer.
      System.arraycopy(this.buffer, this.offset, buffer, offset, length);
      this.offset += length;
    }

    return length;
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public void mark(int readlimit) {
    // do nothing
  }

  @Override
  public void reset() throws IOException {
    offset = 0;
    length = 0;
    buffer = null;
  }

  public void reset(int offset) throws IllegalArgumentException {
    if (offset < 0 || (offset + length) > buffer.length) {
      throw new IllegalArgumentException(
          "offset [" + offset +
              "] must be >= 0 and (offset + getLength()) must be <= getArray().length ["
              + buffer.length + "]");
    }

    this.offset = offset;
  }

  public void reset(byte[] data) throws IllegalArgumentException {
    if (data == null) {
      throw new IllegalArgumentException("data cannot be null");
    }

    reset(data, 0, data.length);
  }

  public void reset(byte[] data, int offset, int length) throws IllegalArgumentException {
    if (data == null) {
      throw new IllegalArgumentException("data cannot be null");
    }
    if (offset < 0 || length < 0 || (offset + length) > data.length) {
      throw new IllegalArgumentException(
          "offset [" + offset + "] and length [" + length + "] must be >= 0 and (offset + length)["
              + (offset + length) + "] must be <= data.length [" + data.length + "]");
    }

    this.offset = 0;
    this.length = length;
    buffer = data;
  }

  public byte[] getArray() {
    return buffer;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }
}
