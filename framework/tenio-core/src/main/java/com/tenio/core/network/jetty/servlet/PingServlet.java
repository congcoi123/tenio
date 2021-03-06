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
package com.tenio.core.network.jetty.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.tenio.common.data.elements.CommonObject;
import com.tenio.core.network.jetty.servlet.support.BaseProcessServlet;

public final class PingServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5999711002391728401L;

	private Process __process = new Process();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		__process.handle(request, response);
	}

	private final class Process extends BaseProcessServlet {

		@Override
		protected void __handleImpl(HttpServletRequest request, HttpServletResponse response) {
			response.setStatus(HttpServletResponse.SC_OK);
			try {
				var json = new JSONObject();
				CommonObject.newInstance().add("status", "ok").add("message", "PING PONG").forEach((key, value) -> {
					json.put(key, value);
				});
				response.getWriter().println(json.toString());
			} catch (IOException e) {
				error(e);
			}
		}
	}

}
