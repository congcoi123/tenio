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
package com.tenio.examples.example8;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.element.CommonObject;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.core.network.define.RestMethod;
import com.tenio.examples.server.TestConfiguration;

/**
 * This class demonstrate how to handle HTTP requests
 * 
 * @author kong
 *
 */
public final class TestServerRestful extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] params) {
		var game = new TestServerRestful();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public TestConfiguration getConfiguration() {
		return new TestConfiguration("TenIOConfig.restful.xml");
	}

	@Override
	public void onStarted(IExtension extension, Configuration configuration) {

	}

	@Override
	public void onShutdown() {

	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@SuppressWarnings("unchecked")
		@Override
		public void initialize(Configuration configuration) {

			_on(ExtensionEvent.HTTP_REQUEST_VALIDATE, params -> {
				var method = _getRestMethod(params[0]);
				// var request = _getHttpServletRequest(params[1]);
				var response = _getHttpServletResponse(params[2]);

				if (method.equals(RestMethod.DELETE)) {
					var json = new JSONObject();
					json.putAll(CommonObject.newInstance().add("status", "failed").add("message", "not supported"));
					try {
						response.getWriter().println(json.toString());
					} catch (IOException e) {
						_error(e, "request");
					}
					return response;
				}

				return null;
			});

			_on(ExtensionEvent.HTTP_REQUEST_HANDLE, params -> {
				// var method = _getRestMethod(params[0]);
				// var request = _getHttpServletRequest(params[1]);
				var response = _getHttpServletResponse(params[2]);

				var json = new JSONObject();
				json.putAll(CommonObject.newInstance().add("status", "ok").add("message", "handler"));
				try {
					response.getWriter().println(json.toString());
				} catch (IOException e) {
					_error(e, "handler");
				}

				return null;
			});

		}

	}

}
