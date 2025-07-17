/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

import com.tenio.common.logger.SystemLogger;
import java.util.HashMap;
import java.util.Map;

/**
 * This server needs some basic configuration to start running. The configuration file can be
 * defined as an XML file. See an example in <code>configuration.example.xml</code>. You can also
 * extend this file to create your own configuration settings.
 */
public abstract class CommonConfiguration extends SystemLogger implements Configuration {

  /**
   * All configuration values will be held in this map. You access values by your
   * defined keys.
   */
  private final Map<ConfigurationType, Object> configuration;

  /**
   * Creates a new instance.
   */
  public CommonConfiguration() {
    configuration = new HashMap<>();
  }

  @Override
  public boolean getBoolean(ConfigurationType key) {
    return Boolean.parseBoolean((String) configuration.get(key));
  }

  @Override
  public int getInt(ConfigurationType key) {
    return Integer.parseInt((String) configuration.get(key));
  }

  @Override
  public float getFloat(ConfigurationType key) {
    return Float.parseFloat((String) configuration.get(key));
  }

  @Override
  public String getString(ConfigurationType key) {
    return (String) configuration.get(key);
  }

  @Override
  public Object get(ConfigurationType key) {
    return configuration.get(key);
  }

  @Override
  public boolean isDefined(ConfigurationType key) {
    return getString(key) != null && !getString(key).equals("-1");
  }

  @Override
  public String toString() {
    return configuration.toString();
  }

  @Override
  public void clear() {
    configuration.clear();
  }

  /**
   * Put a new configuration into the map.
   *
   * @param key   a key for the map
   * @param value a value for the map
   */
  protected void push(ConfigurationType key, Object value) {
    if (key == null) {
      return;
    }

    if (configuration.containsKey(key)) {
      if (isInfoEnabled()) {
        info("CONFIGURATION",
            buildgen("Configuration key [", key, "] attempted to replace the old value ",
                configuration.get(key), " by the new one ", value));
      }
      return;
    }

    configuration.put(key, value);
  }

  /**
   * Your extension part can be handled here. Check the examples for more details
   * about how to use it.
   *
   * @param extProperties the extension data in key-value format
   * @see Map
   */
  protected abstract void extend(Map<String, String> extProperties);
}
