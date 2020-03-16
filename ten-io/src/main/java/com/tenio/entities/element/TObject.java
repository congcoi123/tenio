/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.entities.element;

import java.util.HashMap;

/**
 * This is an element object in your server. It can be used to hold your map
 * data. All message comes from a client will be converted to this object. That
 * helps us normalize the way to communicate and easy to use.
 * 
 * @author kong
 * 
 */
public final class TObject extends HashMap<String, Object> {

	private static final long serialVersionUID = 8818783476027583633L;

	public static TObject newInstance() {
		return new TObject();
	}

	private TObject() {
	}

	public double getDouble(final String key) {
		return (double) get(key);
	}

	public float getFloat(final String key) {
		return (float) get(key);
	}

	public long getLong(final String key) {
		return (long) get(key);
	}

	public int getInt(final String key) {
		return (int) get(key);
	}

	public boolean getBoolean(final String key) {
		return (boolean) get(key);
	}

	public String getString(final String key) {
		return (String) get(key);
	}

	public Object getObject(final String key) {
		return get(key);
	}

	public TObject getTObject(final String key) {
		return (TObject) get(key);
	}

	public TArray getTArray(final String key) {
		return (TArray) get(key);
	}

	public boolean contain(final String key) {
		return containsKey(key);
	}

}
