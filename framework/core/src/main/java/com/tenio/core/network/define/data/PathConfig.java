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

import com.tenio.core.network.define.RestMethod;

/**
 * Forms a configuration for an HTTP's REST method.
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
   * @param name        the {@link String} configuration name
   * @param method      the {@link RestMethod}
   * @param uri         the URI {@link String} value
   * @param description the {@link String} description
   * @param version     the current API version ({@code integer} value)
   */
  public PathConfig(String name, RestMethod method, String uri, String description,
                    int version) {
    this.name = name;
    this.method = method;
    this.uri = uri;
    this.description = description;
    this.version = version;
  }

  /**
   * Retrieves the endpoint's name.
   *
   * @return the {@link String} endpoint's name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves the REST method of the endpoint.
   *
   * @return the {@link RestMethod} in use
   */
  public RestMethod getMethod() {
    return method;
  }

  /**
   * Retrieves the endpoint's URI.
   *
   * @return the {@link String} endpoint's URI
   */
  public String getUri() {
    return uri;
  }

  /**
   * Retrieves the endpoint's description.
   *
   * @return the {@link String} endpoint's description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Retrieves the endpoint's version.
   *
   * @return the {@link String} endpoint's version
   */
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
