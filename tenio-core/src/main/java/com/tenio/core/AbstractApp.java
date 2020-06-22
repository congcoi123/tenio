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
package com.tenio.core;

import java.io.IOException;

import com.tenio.common.extension.IExtension;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.configuration.BaseConfiguration;
import com.tenio.core.exception.DuplicatedUriAndMethodException;
import com.tenio.core.exception.NotDefinedSocketConnectionException;
import com.tenio.core.exception.NotDefinedSubscribersException;
import com.tenio.core.server.Server;

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
		var server = Server.getInstance();
		server.setExtension(getExtension());
		try {
			server.start(getConfiguration());
		} catch (IOException | InterruptedException | NotDefinedSocketConnectionException
				| NotDefinedSubscribersException | DuplicatedUriAndMethodException e) {
			error(e, "Application start");
			server.shutdown();
		}
	}

	/**
	 * @return an extension for handling your own logic class
	 */
	public abstract IExtension getExtension();

	/**
	 * 
	 * @param <T> The derived class of {@link BaseConfiguration}
	 * @return your own class that derived from {@link BaseConfiguration} class
	 */
	public abstract <T extends BaseConfiguration> T getConfiguration();

}
