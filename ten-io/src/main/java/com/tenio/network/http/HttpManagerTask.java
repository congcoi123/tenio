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
package com.tenio.network.http;

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

import com.tenio.configuration.Path;
import com.tenio.configuration.constant.Constants;
import com.tenio.configuration.constant.RestMethod;
import com.tenio.event.IEventManager;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.http.servlet.PingServlet;
import com.tenio.network.http.servlet.main.MainServlet;
import com.tenio.task.schedule.ITask;

/**
 * 
 * 
 * @author kong
 *
 */
public final class HttpManagerTask extends AbstractLogger implements ITask {

	private final Server __server;
	private final String __name;
	private final int __port;

	public HttpManagerTask(IEventManager eventManager, String name, int port, List<Path> paths) {
		__name = name;
		__port = port;

		// Create a Jetty server
		__server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		// Configuration
		context.addServlet(new ServletHolder(new PingServlet()), Constants.PING_PATH);
		// Collect the same URI path for one servlet
		Map<String, List<Path>> servlets = new HashMap<String, List<Path>>();
		for (var path : paths) {
			if (!servlets.containsKey(path.getUri())) {
				var servlet = new ArrayList<Path>();
				servlet.add(path);
				servlets.put(path.getUri(), servlet);
			} else {
				servlets.get(path.getUri()).add(path);
			}
		}

		servlets.forEach((uri, list) -> {
			context.addServlet(new ServletHolder(new MainServlet(eventManager)), uri);
		});

		__server.setHandler(context);
	}

	@Override
	public ScheduledFuture<?> run() {
		return Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			try {
				info("HTTP", buildgen("Name: ", __name, " > Start at port: ", __port));

				__server.start();
				__server.join();
			} catch (Exception e) {
				error(e, "EXCEPTION START", "system");
			}
		}, 0, TimeUnit.SECONDS);
	}

	private boolean __isUriHasDuplicatedMethod(RestMethod method, List<Path> servlet) {
		return servlet.stream().filter(s -> s.getMethod().equals(method)).count() > 1 ? false : true;
	}

}
