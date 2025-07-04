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

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.acceptor.AcceptorHandler;
import com.tenio.core.network.zero.engine.listener.ZeroWriterListener;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Handles read/write events on client and datagram channels using a {@link Selector}.
 *
 * <p>This class is part of the NIO event-driven loop that processes IO on
 * accepted TCP connections and bound UDP datagram channels.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Register socket and datagram channels for read/write events</li>
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

public final class ReaderHandler extends SystemLogger {

  private final DataType dataType;
  private final Selector readableSelector;
  private final ByteBuffer readerBuffer;
  private final Queue<Pair<SelectableChannel, Consumer<SelectionKey>>> pendingChannels;
  private final ZeroWriterListener zeroWriterListener;
  private final SessionManager sessionManager;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final SocketIoHandler socketIoHandler;
  private final DatagramIoHandler datagramIoHandler;

  /**
   * Constructor.
   *
   * @param dataType               the {@link DataType}
   * @param readerBuffer           instance of {@link ByteBuffer}
   * @param zeroWriterListener     instance of {@link ZeroWriterListener}
   * @param sessionManager         instance of {@link SessionManager}
   * @param networkReaderStatistic instance of {@link NetworkReaderStatistic}
   * @param socketIoHandler        instance of {@link SocketIoHandler}
   * @param datagramIoHandler      instance of {@link DatagramIoHandler}
   * @throws IOException whenever any IO exception thrown
   */
  public ReaderHandler(DataType dataType,
                       ByteBuffer readerBuffer,
                       ZeroWriterListener zeroWriterListener,
                       SessionManager sessionManager,
                       NetworkReaderStatistic networkReaderStatistic,
                       SocketIoHandler socketIoHandler,
                       DatagramIoHandler datagramIoHandler) throws IOException {
    this.dataType = dataType;
    this.readerBuffer = readerBuffer;
    this.zeroWriterListener = zeroWriterListener;
    this.sessionManager = sessionManager;
    this.networkReaderStatistic = networkReaderStatistic;
    this.socketIoHandler = socketIoHandler;
    this.datagramIoHandler = datagramIoHandler;

    readableSelector = Selector.open();
    pendingChannels = new ConcurrentLinkedQueue<>();
  }

  /**
   * Wakeup the reader selector.
   */
  public void wakeup() {
    readableSelector.wakeup();
  }

  /**
   * Registers a client socket to the reader selector.
   *
   * @param channel         {@link SocketChannel} a client channel
   * @param onKeyRegistered when its {@link SelectionKey} is ready
   */
  public void registerSocketChannel(SocketChannel channel, Consumer<SelectionKey> onKeyRegistered) {
    pendingChannels.add(Pair.of(channel, onKeyRegistered));
    wakeup();
  }

  /**
   * Registers a client socket to the reader selector.
   *
   * @param channel {@link DatagramChannel} a client channel
   */
  public void registerDatagramChannel(DatagramChannel channel) {
    pendingChannels.add(Pair.of(channel, null));
    wakeup();
  }

  /**
   * Shutdown processing.
   *
   * @throws IOException whenever IO exceptions thrown
   */
  public void shutdown() throws IOException {
    wakeup();
    readableSelector.close();
  }

  /**
   * Processing. This should be run in a loop.
   */
  public void running() {
    try {
      // register channels to selector
      if (!pendingChannels.isEmpty()) {
        // readable selector was registered by OP_READ interested only socket channels,
        // but in some cases, we can receive "can writable" signal from those sockets
        Pair<SelectableChannel, Consumer<SelectionKey>> callbackableChannel;
        while ((callbackableChannel = pendingChannels.poll()) != null) {
          SelectableChannel channel = callbackableChannel.getKey();
          Consumer<SelectionKey> callback = callbackableChannel.getValue();

          if (channel instanceof SocketChannel socketChannel) {
            SelectionKey selectionKey = socketChannel.register(readableSelector,
                SelectionKey.OP_READ);
            callback.accept(selectionKey);
          } else if (channel instanceof DatagramChannel datagramChannel) {
            datagramChannel.register(readableSelector, SelectionKey.OP_READ);
          }
        }
      }

      // blocks until at least one channel is ready for the events you registered for
      int countReadyKeys = readableSelector.select();

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
          // we already registered 2 types of channels for this selector and need to
          // separate the processes
          if (selectableChannel instanceof SocketChannel socketChannel) {
            readTcpData(socketChannel, selectionKey, readerBuffer);
          } else if (selectableChannel instanceof DatagramChannel datagramChannel) {
            readUpdData(datagramChannel, selectionKey, readerBuffer);
          }
        }
      }
    } catch (ClosedSelectorException exception1) {
      error(exception1, "Selector is closed: ", exception1.getMessage());
    } catch (CancelledKeyException exception2) {
      error(exception2, "Cancelled key: ", exception2.getMessage());
    } catch (IOException exception3) {
      error(exception3, "I/O reading/selection error: ", exception3.getMessage());
    } catch (Exception exception4) {
      error(exception4, "Generic reading/selection error: ", exception4.getMessage());
    }
  }

  private void readTcpData(SocketChannel socketChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {
    // retrieves session by its socket channel
    var session = sessionManager.getSessionBySocket(socketChannel);

    if (Objects.isNull(session)) {
      debug("READ_TCP_CHANNEL", "Reader handle a null session with the socket channel: ",
          socketChannel.toString());
      return;
    }

    if (!session.isActivated()) {
      debug("READ_TCP_CHANNEL", "Session is inactivated: ", session.toString());
      return;
    }

    // when a socket channel is writable, should make it the highest priority
    // manipulation
    if (selectionKey.isValid() && selectionKey.isWritable()) {
      // should continually put this session for sending all left packets first
      zeroWriterListener.continueWriteInterestOp(session);
      // now we should set it back to interest in OP_READ
      selectionKey.interestOps(SelectionKey.OP_READ);
    }

    if (selectionKey.isValid() && selectionKey.isReadable()) {
      // prepares the buffer first
      readerBuffer.clear();
      // reads data from socket and write them to buffer
      int byteCount = -1;
      try {
        // this isConnected() method can only work if the server side decides to close the socket
        // there is no way to know if the connection is closed on the client side
        if (socketChannel.isConnected()) {
          byteCount = socketChannel.read(readerBuffer);
        }
      } catch (IOException exception) {
        // so I guess we can ignore this kind of exception or wait until we have proper solutions
        // this checking may not work with other languages (e.g: japanese)
        if (!exception.getMessage().contains("Connection reset")) {
          error(exception, "An exception was occurred on channel: ", socketChannel.toString());
          socketIoHandler.sessionException(session, exception);
        }
      }
      // no left data is available, should close the connection
      if (byteCount == -1) {
        closeTcpConnection(socketChannel);
      } else if (byteCount > 0) {
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

  private void closeTcpConnection(SelectableChannel channel) {
    var socketChannel = (SocketChannel) channel;
    socketIoHandler.channelInactive(socketChannel);
    if (!socketChannel.socket().isClosed()) {
      try {
        socketChannel.socket().shutdownInput();
        socketChannel.socket().shutdownOutput();
        socketChannel.close();
      } catch (IOException exception) {
        error(exception, "Error on closing socket channel: ", socketChannel.toString());
      }
    }
  }

  private void readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {
    Session session = null;

    if (selectionKey.isValid() && selectionKey.isReadable()) {
      // prepares the buffer first
      readerBuffer.clear();
      // reads data from socket and write them to buffer
      SocketAddress remoteAddress;
      try {
        remoteAddress = datagramChannel.receive(readerBuffer);
      } catch (IOException exception) {
        error(exception, "An exception was occurred on channel: ", datagramChannel.toString());
        datagramIoHandler.channelException(datagramChannel, exception);
        return;
      }

      if (Objects.isNull(remoteAddress)) {
        var addressNotFoundException =
            new RuntimeException("Remove address for the datagram channel");
        error(addressNotFoundException, "An exception was occurred on channel: ",
            datagramChannel.toString());
        datagramIoHandler.channelException(datagramChannel, addressNotFoundException);
        return;
      }

      int byteCount = readerBuffer.position();

      // update statistic data
      networkReaderStatistic.updateReadBytes(byteCount);
      // ready to read data from buffer
      readerBuffer.flip();
      // reads data from buffer and transfers them to the next process
      byte[] binary = new byte[readerBuffer.limit()];
      readerBuffer.get(binary);

      // convert binary to dataCollection object
      var dataCollection = DataUtility.binaryToCollection(dataType, binary);

      // retrieves session by its datagram channel, hence we are using only one
      // datagram channel for all sessions, we use incoming request convey ID to
      // distinguish them
      var udpConvey = Session.EMPTY_DATAGRAM_CONVEY_ID;
      DataCollection message = null;
      if (dataCollection instanceof ZeroMap zeroMap) {
        if (zeroMap.containsKey(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID)) {
          udpConvey = zeroMap.getInteger(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID);
        }
        if (zeroMap.containsKey(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA)) {
          message = zeroMap.getDataCollection(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA);
        }
      } else if (dataCollection instanceof MsgPackMap msgPackMap) {
        if (msgPackMap.contains(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID)) {
          udpConvey = msgPackMap.getInteger(CoreConstant.DEFAULT_KEY_UDP_CONVEY_ID);
        }
        if (msgPackMap.containsKey(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA)) {
          message = msgPackMap.getMsgPackMap(CoreConstant.DEFAULT_KEY_UDP_MESSAGE_DATA);
        }
      }

      session = sessionManager.getSessionByDatagram(udpConvey);

      if (Objects.isNull(session)) {
        datagramIoHandler.channelRead(datagramChannel, remoteAddress, message);
      } else {
        if (session.isActivated()) {
          session.setDatagramRemoteSocketAddress(remoteAddress);
          session.addReadBytes(byteCount);
          datagramIoHandler.sessionRead(session, message);
        } else {
          debug("READ_UDP_CHANNEL", "Session is inactivated: ", session.toString());
        }
      }
    }

    if (selectionKey.isValid() && selectionKey.isWritable() && Objects.nonNull(session)) {
      // should continue put this session for sending all left packets first
      zeroWriterListener.continueWriteInterestOp(session);
      // now we should set it back to interest in OP_READ
      selectionKey.interestOps(SelectionKey.OP_READ);
    }
  }
}
