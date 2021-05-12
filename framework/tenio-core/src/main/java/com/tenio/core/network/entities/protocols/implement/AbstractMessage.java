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
package com.tenio.core.network.entities.protocols.implement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tenio.core.network.entities.protocols.Message;

/**
 * @author kong
 */
// TODO: Add description
public abstract class AbstractMessage implements Message {
	
	private long __id;
	private byte[] __content;
	private Map<String, Object> __attributes;

	public long getId() {
		return __id;
	}

	protected void __setId(long id) {
		__id = id;
	}

	public byte[] getContent() {
		return __content;
	}

	public void setContent(byte[] content) {
		__content = content;
	}

	public Object getAttribute(String key) {
		if (__attributes != null) {
			return __attributes.get(key);
		}

		return null;
	}

	public void setAttribute(String key, Object value) {
		if (__attributes == null) {
			__attributes = new ConcurrentHashMap<String, Object>();
		}

		__attributes.put(key, value);
	}

}
