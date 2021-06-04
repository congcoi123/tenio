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
package com.tenio.common.configuration;

import java.util.HashMap;
import java.util.Map;

import com.tenio.common.loggers.SystemLogger;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 */
public abstract class CommonConfiguration extends SystemLogger implements Configuration {

	/**
	 * All configuration values will be held in this map. You access values by your
	 * defined keys.
	 */
	private final Map<ConfigurationType, Object> __configuration;

	public CommonConfiguration() {
		__configuration = new HashMap<ConfigurationType, Object>();
	}

	@Override
	public boolean getBoolean(ConfigurationType key) {
		return Boolean.parseBoolean((String) __configuration.get(key));
	}

	@Override
	public int getInt(ConfigurationType key) {
		return Integer.parseInt((String) __configuration.get(key));
	}

	@Override
	public float getFloat(ConfigurationType key) {
		return Float.parseFloat((String) __configuration.get(key));
	}

	@Override
	public String getString(ConfigurationType key) {
		return (String) __configuration.get(key);
	}

	@Override
	public Object get(ConfigurationType key) {
		return __configuration.get(key);
	}

	@Override
	public boolean isDefined(ConfigurationType key) {
		return __configuration.get(key) == null ? false : (getString(key).equals("-1") ? false : true);
	}

	@Override
	public String toString() {
		return __configuration.toString();
	}

	@Override
	public void clear() {
		__configuration.clear();
	}

	/**
	 * Put new configuration
	 * 
	 * @param key   key
	 * @param value value
	 */
	protected void __push(ConfigurationType key, Object value) {
		if (key == null) {
			return;
		}
		if (__configuration.containsKey(key)) {
			info("CONFIGURATION", buildgen("Configuration key [", key, "] attempted to replace the old value ",
					__configuration.get(key), " by the new one ", value));
			return;
		}
		__configuration.put(key, value);
	}

	/**
	 * Your extension part can be handled here. Check the examples for more details
	 * about how to use it.
	 * 
	 * @param extProperties the extension data in key-value format (see {@link Map})
	 */
	protected abstract void __extend(Map<String, String> extProperties);

}
