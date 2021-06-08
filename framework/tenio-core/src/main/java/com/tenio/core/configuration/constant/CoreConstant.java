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
package com.tenio.core.configuration.constant;

public final class CoreConstant {

	private CoreConstant() {

	}

	/**
	 * The HTTP response with UTF-8 encoding
	 */
	public static final String UTF_8 = "UTF-8";

	/**
	 * The HTTP response with content type in JSON
	 */
	public static final String CONTENT_TYPE_JSON = "application/json";

	/**
	 * The HTTP response with content type in text
	 */
	public static final String CONTENT_TYPE_TEXT = "text/html";

	/**
	 * The default URI path when a HTTP server was started (To confirm if the server
	 * was started or not)
	 */
	public static final String PING_PATH = "/ping";

	public static final String LOCAL_HOST = "localhost";

	public static final String DEFAULT_CONNFIGURATION_FILE = "configuration.xml";

}
