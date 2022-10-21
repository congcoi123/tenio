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

package com.tenio.core.network.zero.handler;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * The Datagram IO handler.
 */
public interface DatagramIoHandler extends BaseIoHandler {

  /**
   * When a new message comes from client side then this method is invoked.
   *
   * @param datagramChannel a {@link DatagramChannel} created on the server
   * @param remoteAddress   a remote {@link SocketAddress} of client side
   * @param binary          an array of {@code byte} data sent by client side
   */
  void channelRead(DatagramChannel datagramChannel, SocketAddress remoteAddress, byte[] binary);

  /**
   * When any exception occurred on the Datagram channel then this method is invoked.
   *
   * @param datagramChannel the {@link DatagramChannel} created on the server
   * @param exception       an {@link Exception} emerging
   */
  void channelException(DatagramChannel datagramChannel, Exception exception);
}
