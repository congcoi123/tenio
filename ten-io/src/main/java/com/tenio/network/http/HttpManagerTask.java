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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.tenio.configuration.constant.Constants;
import com.tenio.event.IEventManager;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.http.servlet.PingServlet;
import com.tenio.task.schedule.ITask;

/**
 * 
 * 
 * @author kong
 *
 */
public final class HttpManagerTask extends AbstractLogger implements ITask {

	private final IEventManager __eventManager;
	private final Server __server;

	public HttpManagerTask(IEventManager eventManager, int port) {
		__eventManager = eventManager;

		// Create a jetty server
		__server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		// Configuration
		context.addServlet(new ServletHolder(new PingServlet()), Constants.PING_PATH);

		__server.setHandler(context);

	}

	@Override
	public ScheduledFuture<?> run() {
		return Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			try {
				__server.start();
				__server.join();
			} catch (Exception e) {
				error(e, "EXCEPTION START", "system");
			}
		}, 1, TimeUnit.SECONDS);
	}

	public void shutdown() {
		try {
			__server.stop();
		} catch (Exception e) {
			error(e, "EXCEPTION STOP", "system");
		}
	}

}
