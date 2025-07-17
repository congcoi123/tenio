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

package com.tenio.core.network.zero.engine.reader;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.acceptor.AcceptorHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

/**
 * Handles read/write events on socket channels using a {@link Selector}.
 *
 * <p>This class is part of the NIO event-driven loop that processes IO on
 * accepted TCP connections.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Register socket channels for read/write events</li>
 *   <li>Dispatch readable/writable keys to appropriate handlers</li>
 *   <li>Manage selection loop and wakeup mechanisms</li>
 *   <li>Notify {@link SocketIoHandler} for channel lifecycle events</li>
 * </ul>
 *
 * <p>Each reader thread runs in a loop, polling its selector and reacting
 * to channel readiness, ensuring non-blocking high-performance IO handling.
 *
 * @see AcceptorHandler
 * @see SocketIoHandler
 * @since 0.6.5
 */

public final class SocketReaderHandler extends SystemLogger {

  private final Selector readableSelector;
  private final ByteBuffer readerBuffer;
  private final SessionManager sessionManager;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final SocketIoHandler socketIoHandler;

  /**
   * Constructor.
   *
   * @param readerBuffer           instance of {@link ByteBuffer}
   * @param sessionManager         instance of {@link SessionManager}
   * @param networkReaderStatistic instance of {@link NetworkReaderStatistic}
   * @param socketIoHandler        instance of {@link SocketIoHandler}
   * @throws IOException whenever any IO exception thrown
   */
  public SocketReaderHandler(ByteBuffer readerBuffer,
                             SessionManager sessionManager,
                             NetworkReaderStatistic networkReaderStatistic,
                             SocketIoHandler socketIoHandler) throws IOException {
    this.readerBuffer = readerBuffer;
    this.sessionManager = sessionManager;
    this.networkReaderStatistic = networkReaderStatistic;
    this.socketIoHandler = socketIoHandler;

    readableSelector = Selector.open();
  }

  /**
   * Registers a client socket to the reader selector.
   *
   * @param socketChannel {@link SocketChannel} a client channel
   * @param onSuccess  when its {@link SelectionKey} is ready
   * @param onFailed      it's failed to register this channel to a reader handler
   */
  public void registerClientSocketChannel(SocketChannel socketChannel,
                                          Consumer<SelectionKey> onSuccess,
                                          Runnable onFailed) {
    // register channels to selector
    // readable selector was registered by OP_READ interested only socket channels,
    // but in some cases, we can receive "can writable" signal from those sockets
    SelectionKey selectionKey;
    try {
      selectionKey = socketChannel.register(readableSelector, SelectionKey.OP_READ);
      readableSelector.wakeup(); // this helps unblock the instruction select() in the method running()
      onSuccess.accept(selectionKey);
    } catch (ClosedChannelException exception) {
      error(exception, "It was unable to register this channel to to selector: ",
          exception.getMessage());
      onFailed.run();
    }
  }

  /**
   * Shutdown processing.
   *
   * @throws IOException whenever IO exceptions thrown
   */
  public void shutdown() throws IOException {
    readableSelector.wakeup(); // this helps unblock the instruction select() in the method running()
    readableSelector.close();
  }

  /**
   * Processing. This should be run in a loop.
   */
  public void running() {
    int countReadyKeys = 0;
    try {
      // blocks until at least one channel is ready for the events you registered for
      countReadyKeys = readableSelector.select();
    } catch (IOException exception) {
      error(exception, "I/O reading/selection error: ", exception.getMessage());
    }

    if (countReadyKeys == 0) {
      return;
    }

    var readyKeys = readableSelector.selectedKeys();
    var keyIterator = readyKeys.iterator();

    while (keyIterator.hasNext()) {
      SelectionKey selectionKey = keyIterator.next();
      // once a key is proceeded, it should be removed from the process to prevent
      // duplicating manipulation
      keyIterator.remove();

      if (selectionKey.isValid()) {
        var selectableChannel = selectionKey.channel();
        var socketChannel = (SocketChannel) selectableChannel;
        readTcpData(socketChannel, selectionKey, readerBuffer);
      }
    }

    // clear canceled keys
    try {
      readableSelector.selectNow(); // helps to quickly remove stale keys
    } catch (IOException exception) {
      error(exception, "I/O reading/selection error: ", exception.getMessage());
    }
  }

  private void readTcpData(SocketChannel socketChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {
    // retrieves session by its socket channel
    var session = sessionManager.getSessionBySocket(socketChannel);

    if (session == null) {
      if (isDebugEnabled()) {
        debug("READ TCP CHANNEL", "Reader handle a null session with the socket channel: ",
            socketChannel.toString());
      }
      return;
    }

    if (!session.isActivated()) {
      if (isDebugEnabled()) {
        debug("READ TCP CHANNEL", "Session is inactivated: ", session.toString());
      }
      return;
    }

    if (selectionKey.isValid() && selectionKey.isReadable()) {
      // prepares the buffer first
      readerBuffer.clear();
      // reads data from socket and write them to buffer
      int byteCount = 0;
      try {
        // this isOpen() && isConnected() method can only work if the server side decides to close
        // the socket. There is no way to know if the connection is closed on the client side
        byteCount = socketChannel.read(readerBuffer);
        if (byteCount == -1) {
          // no left data is available, should close the connection
          socketIoHandler.channelInactive(socketChannel, selectionKey,
              ConnectionDisconnectMode.LOST_IN_READ);
          return;
        }
      } catch (IOException exception) {
        if (isErrorEnabled()) {
          error(exception, "An exception was occurred on channel: ", socketChannel.toString());
        }
        socketIoHandler.sessionException(session, exception);
      }
      if (byteCount > 0) {
        // update statistic data
        session.addReadBytes(byteCount);
        networkReaderStatistic.updateReadBytes(byteCount);
        // ready to read data from buffer
        readerBuffer.flip();
        // reads data from buffer and transfers them to the next process
        byte[] binary = new byte[readerBuffer.limit()];
        readerBuffer.get(binary);

        socketIoHandler.sessionRead(session, binary);
      }
    }
  }
}
