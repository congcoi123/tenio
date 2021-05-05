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
package com.tenio.core.network.jetty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.tenio.common.logger.ZeroLogger;
import com.tenio.common.task.schedule.ITask;
import com.tenio.core.configuration.constant.CoreConstants;
import com.tenio.core.configuration.data.PathConfig;
import com.tenio.core.event.IEventManager;
import com.tenio.core.exception.DuplicatedUriAndMethodException;
import com.tenio.core.network.define.RestMethod;
import com.tenio.core.network.jetty.servlet.PingServlet;
import com.tenio.core.network.jetty.servlet.ServletManager;

/**
 * The HTTP request and response handlers class
 * 
 * @author kong
 *
 */
public final class HttpManagerTask extends ZeroLogger implements ITask {

	private Server __server;
	private final IEventManager __eventManager;
	private final String __name;
	private final int __port;
	private final List<PathConfig> __pathConfigs;

	public HttpManagerTask(IEventManager eventManager, String name, int port, List<PathConfig> pathConfigs) {
		__eventManager = eventManager;
		__name = name;
		__port = port;
		__pathConfigs = pathConfigs;
	}

	public void setup() throws DuplicatedUriAndMethodException {
		// Collect the same URI path for one servlet
		Map<String, List<PathConfig>> servlets = new HashMap<String, List<PathConfig>>();
		for (var path : __pathConfigs) {
			if (!servlets.containsKey(path.getUri())) {
				var servlet = new ArrayList<PathConfig>();
				servlet.add(path);
				servlets.put(path.getUri(), servlet);
			} else {
				servlets.get(path.getUri()).add(path);
			}
		}

		for (var entry : servlets.entrySet()) {
			if (__isUriHasDuplicatedMethod(RestMethod.POST, entry.getValue())) {
				throw new DuplicatedUriAndMethodException("post");
			}
			if (__isUriHasDuplicatedMethod(RestMethod.PUT, entry.getValue())) {
				throw new DuplicatedUriAndMethodException("put");
			}
			if (__isUriHasDuplicatedMethod(RestMethod.GET, entry.getValue())) {
				throw new DuplicatedUriAndMethodException("get");
			}
			if (__isUriHasDuplicatedMethod(RestMethod.DELETE, entry.getValue())) {
				throw new DuplicatedUriAndMethodException("delete");
			}
		}

		// Create a Jetty server
		__server = new Server(__port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		// Configuration
		context.addServlet(new ServletHolder(new PingServlet()), CoreConstants.PING_PATH);
		servlets.forEach((uri, list) -> {
			context.addServlet(new ServletHolder(new ServletManager(__eventManager, list)), uri);
		});

		__server.setHandler(context);
	}

	@Override
	public ScheduledFuture<?> run() {
		return Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			try {
				_info("HTTP SERVICE", _buildgen("Name: ", __name, " > Start at port: ", __port));

				__server.start();
				__server.join();
			} catch (Exception e) {
				_error(e);
			}
		}, 0, TimeUnit.SECONDS);
	}

	private boolean __isUriHasDuplicatedMethod(RestMethod method, List<PathConfig> pathConfigs) {
		return pathConfigs.stream().filter(pathConfig -> pathConfig.getMethod().equals(method)).count() > 1 ? true
				: false;
	}

}
