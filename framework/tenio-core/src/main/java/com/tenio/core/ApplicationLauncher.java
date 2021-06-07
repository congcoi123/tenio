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
package com.tenio.core;

import com.tenio.common.configuration.constant.CommonConstant;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.bootstrap.Bootstrapper;
import com.tenio.core.server.ServerImpl;

/**
 * Your application will start from here.
 */
public final class ApplicationLauncher extends SystemLogger {

	public static void run(Class<?> entryClazz, String[] params) {
		var application = ApplicationLauncher.newInstance();
		application.start(entryClazz, params);
	}

	/**
	 * Start The Game Server With DI
	 */
	public void start(Class<?> entryClazz, String[] params) {
		Bootstrapper bootstrap = null;
		if (entryClazz != null) {
			bootstrap = Bootstrapper.newInstance();
			try {
				bootstrap.run(entryClazz, CommonConstant.DEFAULT_CONFIGURATION_PACKAGE,
						CommonConstant.DEFAULT_BOOTSTRAP_PACKAGE, CommonConstant.DEFAULT_EXTENSION_EVENT_PACKAGE,
						CommonConstant.DEFAULT_ENGINE_HEARTBEAT_PACKAGE);
			} catch (Exception e) {
				error(e, "The application started with exceptions occured: ", e.getMessage());
				System.exit(1);
			}
		}

		var server = ServerImpl.getInstance();
		try {
			server.start(bootstrap.getBootstrapHandler(), params);
		} catch (Exception e) {
			error(e, "The application started with exceptions occured: ", e.getMessage());
			server.shutdown();
			// exit with errors
			System.exit(1);
		}

		// Suddenly shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			server.shutdown();
			System.exit(0);
		}));
	}

	private static ApplicationLauncher newInstance() {
		return new ApplicationLauncher();
	}

	private ApplicationLauncher() {

	}

}
