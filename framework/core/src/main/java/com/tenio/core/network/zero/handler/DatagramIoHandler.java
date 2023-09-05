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

package com.tenio.core.network.zero.handler;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.core.network.entity.session.Session;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * The Datagram IO handler.
 */
public interface DatagramIoHandler extends BaseIoHandler {

  /**
   * Set the data serialization type.
   *
   * @param dataType the {@link DataType} value
   */
  void setDataType(DataType dataType);

  /**
   * When a new message comes from client side then this method is invoked.
   *
   * @param datagramChannel a {@link DatagramChannel} created on the server
   * @param remoteAddress   a remote {@link SocketAddress} of client side*
   * @param message         an instance of {@link DataCollection} sent from client side, it can be in
   *                        different formats base on {@link DataType} value defined in {@code
   *                        configuration.xml}. This message should contain credential
   *                        information to verify the sender
   */
  void channelRead(DatagramChannel datagramChannel, SocketAddress remoteAddress,
                   DataCollection message);

  /**
   * When a new message comes from a session then this method is invoked.
   *
   * @param session the {@link Session} using to communicate to client side
   * @param message an instance of {@link DataCollection} sent from client side, it can be in
   *                different formats base on {@link DataType} value defined in {@code
   *                configuration.xml}
   */
  void sessionRead(Session session, DataCollection message);

  /**
   * When any exception occurred on the Datagram channel then this method is invoked.
   *
   * @param datagramChannel the {@link DatagramChannel} created on the server
   * @param exception       an {@link Exception} emerging
   */
  void channelException(DatagramChannel datagramChannel, Exception exception);
}
