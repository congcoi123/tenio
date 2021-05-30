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
package com.tenio.common.data.elements;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This is an element object in your server. It can be used to hold your map
 * data. All message comes from a client will be converted to this object. That
 * helps us normalize the way to communicate and easy to use.
 * 
 * @author kong
 * 
 */
public final class CommonObject extends HashMap<String, Object> implements Serializable {

	private static final long serialVersionUID = 8818783476027583633L;

	public static CommonObject newInstance() {
		return new CommonObject();
	}
	
	public CommonObject() {
	}

	public double getDouble(String key) {
		return (double) get(key);
	}

	public float getFloat(String key) {
		return (float) get(key);
	}

	public long getLong(String key) {
		return (long) get(key);
	}

	public int getInt(String key) {
		return (int) get(key);
	}

	public boolean getBoolean(String key) {
		return (boolean) get(key);
	}

	public String getString(String key) {
		return (String) get(key);
	}

	public Object getObject(String key) {
		return get(key);
	}

	public CommonObject getMessageObject(String key) {
		return (CommonObject) get(key);
	}

	public CommonObjectArray getMessageObjectArray(String key) {
		return (CommonObjectArray) get(key);
	}

	public boolean contain(String key) {
		return containsKey(key);
	}

	public CommonObject add(String key, Object value) {
		put(key, value);
		return this;
	}

}
