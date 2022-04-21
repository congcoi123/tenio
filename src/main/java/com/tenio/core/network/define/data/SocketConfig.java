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

import com.tenio.core.network.define.TransportType;

/**
 * A configuration for a socket.
 */
public final class SocketConfig {

  private final String name;
  private final TransportType type;
  private final int port;

  /**
   * Initialization.
   *
   * @param name the {@link String} configuration name
   * @param type the {@link TransportType}
   * @param port the opened port ({@code integer} value)
   */
  public SocketConfig(String name, TransportType type, int port) {
    this.name = name;
    this.type = type;
    this.port = port;
  }

  /**
   * Retrieves the socket's name.
   *
   * @return the {@link String} socket's name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves the socket's transportation type.
   *
   * @return the {@link TransportType} of socket
   */
  public TransportType getType() {
    return type;
  }

  /**
   * Retrieves the socket's port number.
   *
   * @return the socket's port number ({@code integer} value)
   */
  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return String.format("{ name:%s, type:%s, port:%d }", name, type.name(), port);
  }
}
