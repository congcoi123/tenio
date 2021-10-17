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

package com.tenio.core.network.define.data;

import com.tenio.core.network.define.RestMethod;

/**
 * Forms a configuration for the HTTP's REST method.
 */
public final class PathConfig {

  private final String name;
  private final String description;
  private final int version;
  private final RestMethod method;
  private final String uri;

  /**
   * Initialization.
   *
   * @param name        the configuration name
   * @param method      the REST method
   * @param uri         the URI
   * @param description the description
   * @param version     the current API version
   */
  public PathConfig(String name, RestMethod method, String uri, String description,
                    int version) {
    this.name = name;
    this.method = method;
    this.uri = uri;
    this.description = description;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public RestMethod getMethod() {
    return method;
  }

  public String getUri() {
    return uri;
  }

  public String getDescription() {
    return description;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return String.format("{ name:%s, method:%s, uri:%s, description:%s, version:%d}", name,
        method.name(),
        uri, description, version);
  }
}
