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
package com.tenio.engine;

import com.tenio.common.extension.IExtension;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.engine.configuration.BaseConfiguration;
import com.tenio.engine.server.Engine;

/**
 * Your application will start from here.
 * 
 * @author kong
 * 
 */
public abstract class AbstractApp extends AbstractLogger {

	/**
	 * Start The Game Engine
	 */
	public void start() {
		var engine = Engine.getInstance();
		engine.setExtension(getExtension());
		try {
			engine.start(getConfiguration());
		} catch (Exception e) {
			error(e, "Application start");
			engine.shutdown();
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
