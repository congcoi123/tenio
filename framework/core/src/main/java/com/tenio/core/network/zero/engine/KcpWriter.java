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

package com.tenio.core.network.zero.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;

/**
 * The class provides interfaces to allow KCP channel sending data via its conveying channel.
 *
 * @param <T> the conveying channel
 * @since 0.3.0
 */
public interface KcpWriter<T> {

  /**
   * Retrieves the current local address (the source).
   *
   * @return the {@link InetAddress} of source
   */
  InetAddress getLocalAddress();

  /**
   * Retrieves the current remote address (the destination).
   *
   * @return the {@link SocketAddress} of destination
   */
  SocketAddress getRemoteAddress();

  /**
   * Retrieves the current local port (the source).
   *
   * @return the {@code integer} value of source's port
   */
  int getLocalPort();

  /**
   * Retrieves the writer which is using for real transmitting data.
   *
   * @return the instance of {@link T}
   */
  T getWriter();

  /**
   * Sends packet to client.
   *
   * @param binaries the sending data in {@code byte} array
   * @param size     the sending data's size
   * @return the size of sent data
   * @throws IOException any exception occurred while sending data
   */
  int send(byte[] binaries, int size) throws IOException;
}
