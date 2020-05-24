/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.network.http.servlet.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tenio.configuration.constant.Constants;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.http.servlet.IServletHandler;

/**
 * @author kong
 */
public abstract class BaseProcessServlet extends AbstractLogger implements IServletHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(Constants.CONTENT_TYPE_JSON);
		response.setContentType(Constants.CONTENT_TYPE_TEXT);
		response.setCharacterEncoding(Constants.UTF_8);
		_handleImpl(request, response);
	}

	protected abstract void _handleImpl(HttpServletRequest request, HttpServletResponse response);

}
