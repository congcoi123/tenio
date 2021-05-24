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

import java.io.IOException;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.loggers.AbstractLogger;
import com.tenio.core.bootstrap.Bootstrapper;
import com.tenio.core.exceptions.DuplicatedUriAndMethodException;
import com.tenio.core.exceptions.NotDefinedSocketConnectionException;
import com.tenio.core.exceptions.NotDefinedSubscribersException;
import com.tenio.core.extension.Extension;
import com.tenio.core.server.ServerImpl;

/**
 * Your application will start from here.
 * 
 * @author kong
 * 
 */
public abstract class AbstractApp extends AbstractLogger {

	/**
	 * Start The Game Server
	 */
	public void start() {
		__start(null);
	}
	
	/**
	 * Start The Game Server With DI
	 */
	public void start(Class<?> entryClazz) {
		__start(entryClazz);
	}
	
	private void __start(Class<?> entryClazz) {
		Bootstrapper bootstrap = null;
		boolean bootstrapRunning = false;
		if (entryClazz != null) {
			bootstrap = Bootstrapper.newInstance();
			try {
				bootstrapRunning = bootstrap.run(entryClazz);
			} catch (Exception e) {
				error(e, "The application started with exceptions occured");
				System.exit(1);
			}
		}

		var configuration = getConfiguration();

		var server = ServerImpl.getInstance();
		try {
			if (bootstrap == null) {
				server.start(configuration, null);
			} else {
				server.start(configuration, bootstrap.getEventHandler());
			}
			if (bootstrapRunning) {
				info("BOOTSTRAP", "Started");
			} else {
				info("BOOTSTRAP", "Not setup");
			}
		} catch (IOException | InterruptedException | NotDefinedSocketConnectionException
				| NotDefinedSubscribersException | DuplicatedUriAndMethodException e) {
			error(e, "The application started with exceptions occured");
			server.shutdown();
			onShutdown();
			// exit with errors
			System.exit(1);
		}
		// Suddenly shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			server.shutdown();
			onShutdown();
		}));
		// The server was ready
		onStarted(configuration);
	}

	/**
	 * @return your own class that derived from {@link Configuration} class
	 */
	public abstract Configuration getConfiguration();

	/**
	 * The trigger is called when server was started
	 */
	public abstract void onStarted(Configuration configuration);

	/**
	 * The trigger is called when server was tear down
	 */
	public abstract void onShutdown();

}
