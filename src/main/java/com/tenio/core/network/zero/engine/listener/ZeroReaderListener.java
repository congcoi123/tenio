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

package com.tenio.core.network.zero.engine.listener;

import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.ZeroReader;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * The reader engine listener. Invoked by the acceptor engine, reader engine.
 *
 * @see ZeroAcceptor
 * @see ZeroReader
 */
public interface ZeroReaderListener {

  /**
   * The UDP datagram is a connectionless protocol, we don't need to create bi-direction
   * connection, that why it's not necessary to register it to acceptable selector. Just leave it
   * to the reader selector later.
   *
   * @param datagramChannel the {@link DatagramChannel} created on the server
   * @throws ClosedChannelException when a channel is closed unexpectedly
   * @see ZeroAcceptor
   */
  void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException;

  /**
   * When a new coming socket connected to the server, and it is accepted by the acceptor. This
   * socket then is added to the reader engine to manage communication.
   *
   * @param socketChannel the accepted {@link SocketChannel}
   * @return the corresponding {@link SelectionKey} for management
   * @throws ClosedChannelException when a channel is closed unexpectedly
   * @see ZeroAcceptor
   */
  SelectionKey acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException;

  /**
   * Wakes up the reader selector in the reader engine for channels handling
   *
   * @see ZeroAcceptor
   * @see ZeroReader
   */
  void wakeup();
}
