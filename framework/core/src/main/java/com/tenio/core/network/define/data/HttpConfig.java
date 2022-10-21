/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.define.data;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Holds configurations for the HTTP service.
 */
@ThreadSafe
public final class HttpConfig {

  private final String name;
  private final int port;
  private final List<PathConfig> paths;

  /**
   * Initialization.
   *
   * @param name a {@link String} configuration name
   * @param port an associated port ({@code integer} value)
   */
  public HttpConfig(String name, int port) {
    paths = new ArrayList<>();
    this.name = name;
    this.port = port;
  }

  /**
   * Retrieves the configuration name.
   *
   * @return a {@link String} name for the configuration
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves paths associated with the configuration.
   *
   * @return a list of {@link PathConfig}
   * @see List
   */
  public List<PathConfig> getPaths() {
    synchronized (paths) {
      return paths;
    }
  }

  /**
   * Adds a new path to the paths' list.
   *
   * @param path the new {@link PathConfig}
   */
  public void addPath(PathConfig path) {
    synchronized (paths) {
      paths.add(path);
    }
  }

  /**
   * Retrieves the port number.
   *
   * @return the port number ({@code integer} value)
   */
  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return "HttpConfig{" +
        "name='" + name + '\'' +
        ", port=" + port +
        ", paths=" + paths +
        '}';
  }
}
