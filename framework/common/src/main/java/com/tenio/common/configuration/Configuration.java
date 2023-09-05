/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

/**
 * This server needs some basic configuration to start running. The configuration file can be
 * defined as an XML file. See an example in <code>configuration.example.xml</code>. You can also
 * extend this file to create your own configuration values.
 */
public interface Configuration {

  /**
   * Load the configuration by configured files.
   *
   * @param file the configuration file
   * @throws Exception something went wrong on parsing the configuration file
   */
  void load(String file) throws Exception;

  /**
   * Retrieves the value by key.
   *
   * @param key the configuration's key
   * @return the value in {@link Boolean}
   */
  boolean getBoolean(ConfigurationType key);

  /**
   * Retrieves the value by key.
   *
   * @param key the configuration's key
   * @return the value in {@link Integer}
   */
  int getInt(ConfigurationType key);

  /**
   * Retrieves the value by key.
   *
   * @param key the configuration's key
   * @return the value in {@link Float}
   */
  float getFloat(ConfigurationType key);

  /**
   * Retrieves the value by key.
   *
   * @param key the configuration's key
   * @return the value in {@link String}
   */
  String getString(ConfigurationType key);

  /**
   * Retrieves the value by key.
   *
   * @param key the configuration's key
   * @return the value in {@link Object}
   */
  Object get(ConfigurationType key);

  /**
   * Determines if this configuration is existed or defined. If you want some configuration value
   * to be treated as an <code>undefined</code> status, let its value <code>-1</code>.
   *
   * @param key The desired configuration's key
   * @return <code>true</code> if the configuration is defined, otherwise return <code>false</code>
   */
  boolean isDefined(ConfigurationType key);

  /**
   * Retrieves readable text.
   *
   * @return configuration information as human-readable data
   */
  String toString();

  /**
   * Remove all configurations.
   */
  void clear();
}
