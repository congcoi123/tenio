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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.DuplicatedUriAndMethodException;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.defines.RestMethod;
import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.jetty.servlet.PingServlet;
import com.tenio.core.network.jetty.servlet.ServletManager;
import com.tenio.core.service.Service;

public final class JettyHttpService extends AbstractManager implements Service, Runnable {

	private Server __server;
	private ExecutorService __executor;
	private int __port;
	private List<PathConfig> __pathConfigs;
	private boolean __initialized;

	public static JettyHttpService newInstance(EventManager eventManager) {
		return new JettyHttpService(eventManager);
	}

	private JettyHttpService(EventManager eventManager) {
		super(eventManager);

		__initialized = false;
	}

	private void __setup() throws DuplicatedUriAndMethodException {
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
				throw new DuplicatedUriAndMethodException(RestMethod.POST, entry.getValue());
			}
			if (__isUriHasDuplicatedMethod(RestMethod.PUT, entry.getValue())) {
				throw new DuplicatedUriAndMethodException(RestMethod.PUT, entry.getValue());
			}
			if (__isUriHasDuplicatedMethod(RestMethod.GET, entry.getValue())) {
				throw new DuplicatedUriAndMethodException(RestMethod.GET, entry.getValue());
			}
			if (__isUriHasDuplicatedMethod(RestMethod.DELETE, entry.getValue())) {
				throw new DuplicatedUriAndMethodException(RestMethod.DELETE, entry.getValue());
			}
		}

		// Create a Jetty server
		__server = new Server(__port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		// Configuration
		context.addServlet(new ServletHolder(new PingServlet()), CoreConstant.PING_PATH);
		servlets.forEach((uri, list) -> {
			context.addServlet(new ServletHolder(new ServletManager(__eventManager, list)), uri);
		});

		__server.setHandler(context);
	}

	@Override
	public void run() {
		try {
			info("START SERVICE", buildgen(getName(), " (", 1, ")"));

			info("Http Info", buildgen("Started at port: ", __port, ", Configuration: ", __pathConfigs.toString()));

			__server.start();
			__server.join();
		} catch (Exception e) {
			error(e);
		}
	}

	private boolean __isUriHasDuplicatedMethod(RestMethod method, List<PathConfig> pathConfigs) {
		return pathConfigs.stream().filter(pathConfig -> pathConfig.getMethod().equals(method)).count() > 1 ? true
				: false;
	}

	@Override
	public void initialize() {
		try {
			__setup();
		} catch (DuplicatedUriAndMethodException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}
		__initialized = true;
	}

	@Override
	public void start() {
		if (!__initialized) {
			return;
		}

		__executor = Executors.newSingleThreadExecutor();
		__executor.execute(this);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (__executor != null && !__executor.isShutdown()) {
					try {
						shutdown();
					} catch (Exception e) {
						error(e);
					}
				}
			}
		});
	}

	@Override
	public void shutdown() {
		if (!__initialized) {
			return;
		}

		try {
			__server.stop();
			__executor.shutdownNow();
		} catch (Exception e) {
			error(e);
		}
		info("STOPPED SERVICE", buildgen(getName(), " (", 1, ")"));
		__destroy();
		info("DESTROYED SERVICE", buildgen(getName(), " (", 1, ")"));
	}

	private void __destroy() {
		__server = null;
		__executor = null;
	}

	@Override
	public boolean isActivated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return "jetty-http";
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public void setPort(int port) {
		__port = port;
	}

	public void setPathConfigs(List<PathConfig> pathConfigs) {
		__pathConfigs = pathConfigs;
	}

}
