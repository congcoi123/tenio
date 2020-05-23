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
package com.tenio.network.http.servlet.main;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tenio.network.http.servlet.base.BaseProcessServlet;
import com.tenio.network.http.servlet.base.BaseServlet;

/**
 * @author kong
 */
public final class MainServlet extends BaseServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1971993446960398293L;

	private ProcessPost __processPost = new ProcessPost();
	private ProcessPut __processPut = new ProcessPut();
	private ProcessGet __processGet = new ProcessGet();
	private ProcessDelete __processDelete = new ProcessDelete();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		__processGet.handle(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		__processPost.handle(request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		__processPut.handle(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		__processDelete.handle(request, response);
	}

	private final class ProcessPost extends BaseProcessServlet {

		@Override
		protected void _handleImpl(HttpServletRequest request, HttpServletResponse response) {

		}

	}

	private final class ProcessPut extends BaseProcessServlet {

		@Override
		protected void _handleImpl(HttpServletRequest request, HttpServletResponse response) {

		}

	}

	private final class ProcessGet extends BaseProcessServlet {

		@Override
		protected void _handleImpl(HttpServletRequest request, HttpServletResponse response) {

		}

	}

	private final class ProcessDelete extends BaseProcessServlet {

		@Override
		protected void _handleImpl(HttpServletRequest request, HttpServletResponse response) {

		}

	}

}