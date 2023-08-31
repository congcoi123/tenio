/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.common.data.msgpack.element;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an element array class holds data in a map. All message comes from other services
 * will be converted to this object. That helps normalize the way to communicate and be easy to use.
 */
public final class MsgPackArray extends ArrayList<Object> implements Serializable {

  @Serial
  private static final long serialVersionUID = -2718756636745901607L;

  /**
   * Create a fresh instance.
   */
  public MsgPackArray() {
  }

  /**
   * Retrieves new instance of the class.
   *
   * @return an instance
   */
  public static MsgPackArray newInstance() {
    return new MsgPackArray();
  }

  /**
   * Appends a new value into the array.
   *
   * @param value the data value in common {@link MsgPackArray} type
   * @return the pointer of this instance
   */
  public MsgPackArray addMsgPackArray(MsgPackArray value) {
    add(value);
    return this;
  }

  /**
   * Appends a new value into the array.
   *
   * @param value the data value in common {@link String} type
   * @return the pointer of this instance
   */
  public MsgPackArray addString(String value) {
    add(value);
    return this;
  }

  /**
   * Appends a new value into the array.
   *
   * @param value the data value in common {@code float} type
   * @return the pointer of this instance
   */
  public MsgPackArray addFloat(float value) {
    add(value);
    return this;
  }

  /**
   * Appends a new value into the array.
   *
   * @param value the data value in common {@code integer} type
   * @return the pointer of this instance
   */
  public MsgPackArray addInteger(int value) {
    add(value);
    return this;
  }

  /**
   * Appends a new value into the array.
   *
   * @param value the data value in common {@code boolean} type
   * @return the pointer of this instance
   */
  public MsgPackArray addBoolean(boolean value) {
    add(value);
    return this;
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>float</code> type at index in the array
   */
  public float getFloat(int index) {
    return (float) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>integer</code> type at index in the array
   */
  public int getInteger(int index) {
    return (int) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in <code>boolean</code> type at index in the array
   */
  public boolean getBoolean(int index) {
    return (boolean) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in {@link String} type at index in the array
   */
  public String getString(int index) {
    return (String) get(index);
  }

  /**
   * Retrieves value in the array by its index.
   *
   * @param index the <code>integer</code> index in array
   * @return the value converted in {@link MsgPackArray} type at index in the array
   */
  public MsgPackArray getMsgPackArray(int index) {
    return (MsgPackArray) get(index);
  }

  /**
   * Retrieves a new copy of the interior array.
   *
   * @return a new copy of the array in unmodified mode.
   */
  public List<Object> getReadonlyList() {
    return List.copyOf(this);
  }
}
