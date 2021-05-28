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
package com.tenio.core.network.defines.data;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class HttpConfig {

	private final String __name;
	private final int __port;
	private final List<PathConfig> __paths;

	public HttpConfig(String name, int port) {
		__paths = new ArrayList<PathConfig>();
		__name = name;
		__port = port;
	}

	public String getName() {
		return __name;
	}

	public List<PathConfig> getPaths() {
		synchronized (__paths) {
			return __paths;
		}
	}

	public void addPath(PathConfig path) {
		synchronized (__paths) {
			__paths.add(path);
		}
	}

	public int getPort() {
		return __port;
	}

	@Override
	public String toString() {
		return String.format("{ paths:%s, name:%s, port:%d}", __paths.toString(), __name, __port);
	}

}
