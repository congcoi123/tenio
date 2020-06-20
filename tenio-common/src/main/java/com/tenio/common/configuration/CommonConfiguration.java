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
package com.tenio.common.configuration;

import java.util.HashMap;
import java.util.Map;

import com.tenio.common.logger.AbstractLogger;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * <h1>Configuration for game server, declared in properties file</h1> <br>
 * 
 * @author kong
 * 
 */
public abstract class CommonConfiguration extends AbstractLogger {

	/**
	 * All configuration values will be held in this map. You access values by your
	 * defined keys.
	 */
	private final Map<String, String> __configuration = new HashMap<String, String>();

	/**
	 * Put new configuration
	 * 
	 * @param key   key
	 * @param value value
	 */
	protected void _put(final String key, final String value) {
		__configuration.put(key, value);
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link Boolean}
	 */
	public boolean getBoolean(final String key) {
		return Boolean.parseBoolean(__configuration.get(key));
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link Integer}
	 */
	public int getInt(final String key) {
		return Integer.parseInt(__configuration.get(key));
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link Float}
	 */
	public float getFloat(final String key) {
		return Float.parseFloat(__configuration.get(key));
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link String}
	 */
	public String getString(final String key) {
		return __configuration.get(key);
	}

	/**
	 * Determine if this configuration is existed or defined. If you want some
	 * configuration value to be treated as an "undefined" status, let its value
	 * "-1".
	 * 
	 * @param key The desired configuration's key
	 * @return <b>true</b> if the configuration is defined and otherwise return
	 *         <b>false</b>
	 */
	public boolean isDefined(final String key) {
		return __configuration.get(key) == null ? false : (getString(key).equals("-1") ? false : true);
	}

	@Override
	public String toString() {
		return __configuration.toString();
	}

	/**
	 * Your extension part can be handled here. Check the examples for more details
	 * about how to use it.
	 * 
	 * @param extProperties the extension data in key-value format (see {@link Map})
	 */
	protected abstract void _extend(Map<String, String> extProperties);

}
