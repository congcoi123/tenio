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
package com.tenio.core.entities.data;

import com.tenio.common.data.ZeroElement;
import com.tenio.common.utilities.TimeUtility;

public final class ServerMessage {

	private long __createdTimestamp;
	private ZeroElement __data;

	public static ServerMessage newInstance() {
		return new ServerMessage();
	}

	private ServerMessage() {
		__createdTimestamp = TimeUtility.currentTimeMillis();
	}

	public long getCreatedTimestamp() {
		return __createdTimestamp;
	}

	public ZeroElement getData() {
		return __data;
	}

	public ServerMessage setData(ZeroElement data) {
		__data = data;
		return this;
	}

	@Override
	public String toString() {
		return String.format("{ timestamp: %d, data: %s }", __createdTimestamp, __data.toString());
	}

}
