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

package com.tenio.common.data.element;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is an element object in your server. You can use it for holding array
 * data and make it serialize to send through the network.
 */
public final class CommonObjectArray extends ArrayList<Object> implements Serializable {

  private static final long serialVersionUID = -5100842875580575666L;

  public CommonObjectArray() {
  }

  public static CommonObjectArray newInstance() {
    return new CommonObjectArray();
  }

  public CommonObjectArray put(Object value) {
    add(value);
    return this;
  }

  public double getDouble(int index) {
    return (double) get(index);
  }

  public float getFloat(int index) {
    return (float) get(index);
  }

  public long getLong(int index) {
    return (long) get(index);
  }

  public int getInt(int index) {
    return (int) get(index);
  }

  public boolean getBoolean(int index) {
    return (boolean) get(index);
  }

  public String getString(int index) {
    return (String) get(index);
  }

  public Object getObject(int index) {
    return get(index);
  }

  public CommonObjectArray getObjectAt(int index) {
    return (CommonObjectArray) get(index);
  }
}
