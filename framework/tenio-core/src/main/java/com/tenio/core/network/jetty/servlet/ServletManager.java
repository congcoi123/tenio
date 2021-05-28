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
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.tenio.common.data.elements.CommonObject;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.defines.RestMethod;
import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.jetty.servlet.support.BaseProcessServlet;
import com.tenio.core.network.jetty.servlet.support.BaseServlet;

@ThreadSafe
public final class ServletManager extends BaseServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1971993446960398293L;

	private final EventManager __eventManager;
	private final PrivateLogger __logger;

	private ProcessPost __processPost;
	private ProcessPut __processPut;
	private ProcessGet __processGet;
	private ProcessDelete __processDelete;

	public ServletManager(EventManager eventManager, List<PathConfig> pathConfigs) {
		__eventManager = eventManager;
		__logger = new PrivateLogger();
		for (var pathConfig : pathConfigs) {
			switch (pathConfig.getMethod()) {
			case POST:
				__processPost = new ProcessPost();
				break;
			case PUT:
				__processPut = new ProcessPut();
				break;
			case GET:
				__processGet = new ProcessGet();
				break;
			case DELETE:
				__processDelete = new ProcessDelete();
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (__processGet != null) {
			__processGet.handle(request, response);
		} else {
			__sendUnsupportedMethod(response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (__processPost != null) {
			__processPost.handle(request, response);
		} else {
			__sendUnsupportedMethod(response);
		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (__processPut != null) {
			__processPut.handle(request, response);
		} else {
			__sendUnsupportedMethod(response);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (__processDelete != null) {
			__processDelete.handle(request, response);
		} else {
			__sendUnsupportedMethod(response);
		}
	}

	private final class ProcessPost extends BaseProcessServlet {

		@Override
		protected void __handleImpl(HttpServletRequest request, HttpServletResponse response) {
			var check = __eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.POST, request, response);
			if (check == null) {
				__eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.POST, request, response);
			}
		}

	}

	private final class ProcessPut extends BaseProcessServlet {

		@Override
		protected void __handleImpl(HttpServletRequest request, HttpServletResponse response) {
			var check = __eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.PUT, request, response);
			if (check == null) {
				__eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.PUT, request, response);
			}
		}

	}

	private final class ProcessGet extends BaseProcessServlet {

		@Override
		protected void __handleImpl(HttpServletRequest request, HttpServletResponse response) {
			var check = __eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.GET, request, response);
			if (check == null) {
				__eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.GET, request, response);
			}
		}

	}

	private final class ProcessDelete extends BaseProcessServlet {

		@Override
		protected void __handleImpl(HttpServletRequest request, HttpServletResponse response) {
			var check = __eventManager.emit(ServerEvent.HTTP_REQUEST_VALIDATION, RestMethod.DELETE, request, response);
			if (check == null) {
				__eventManager.emit(ServerEvent.HTTP_REQUEST_HANDLE, RestMethod.DELETE, request, response);
			}
		}

	}

	private void __sendUnsupportedMethod(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		try {
			var json = new JSONObject();
			CommonObject.newInstance().add("status", "failed").add("message", "405 Method Not Allowed")
					.forEach((key, value) -> {
						json.put(key, value);
					});
			response.getWriter().println(json.toString());
		} catch (IOException e) {
			__logger.error(e);
		}
	}

	private final class PrivateLogger extends SystemLogger {

	}

}