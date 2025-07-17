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
import com.tenio.core.network.zero.engine.reader.SocketReaderHandler;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

/**
 * The reader engine listener. Invoked by the acceptor engine, reader engine.
 *
 * @see ZeroAcceptor
 * @see ZeroReader
 */
public interface ZeroReaderListener {

  /**
   * When a new coming socket connected to the server, and it is accepted by the acceptor. This
   * socket then is added to the reader engine to manage communication.
   *
   * @param socketChannel the accepted {@link SocketChannel}
   * @param onSuccess     the callback when the reader {@link SelectionKey} is ready
   * @param onFailed      it's failed to register this channel to a reader handler
   * @throws ClosedChannelException when a channel is closed unexpectedly
   * @see ZeroAcceptor
   * @see SocketReaderHandler
   */
  void acceptClientSocketChannel(SocketChannel socketChannel, Consumer<SelectionKey> onSuccess,
                                 Runnable onFailed)
      throws ClosedChannelException;
}
