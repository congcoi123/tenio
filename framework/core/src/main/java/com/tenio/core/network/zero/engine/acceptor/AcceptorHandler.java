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

package com.tenio.core.network.zero.engine.acceptor;

import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.OsUtility;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.utility.SocketUtility;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles incoming TCP/UDP connections using Java NIO's {@link Selector}.
 *
 * <p>This class manages a dedicated selector for accept operations, registers
 * server socket channels and optionally datagram channels, and delegates accepted
 * connections to a {@link ZeroReaderListener} for further processing.
 *
 * <p>Supports multiple concurrent acceptor threads, though on macOS and Windows,
 * only one thread may effectively accept due to OS limitations with {@code SO_REUSEPORT}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Bind and register TCP server sockets (OP_ACCEPT)</li>
 *   <li>Bind UDP channels (delegated to reader selector)</li>
 *   <li>Accept new client sockets and hand off to readers</li>
 *   <li>Filter client IPs with {@link ConnectionFilter}</li>
 *   <li>Clean shutdown of all resources (client/server channels, selector)</li>
 * </ul>
 *
 * @see ZeroReaderListener
 * @see ConnectionFilter
 * @since 0.6.5
 */
public final class AcceptorHandler extends SystemLogger {

  private final String serverAddress;
  private final Selector acceptableSelector;
  private final List<SelectableChannel> serverChannels;
  private final List<SocketChannel> clientChannels;
  private final ConnectionFilter connectionFilter;
  private final ZeroReaderListener zeroReaderListener;
  private final SocketIoHandler socketIoHandler;

  /**
   * Constructor.
   *
   * @param serverAddress          the server IP address
   * @param connectionFilter       instance of {@link ConnectionFilter}
   * @param zeroReaderListener     instance of {@link ZeroReaderListener}
   * @param tcpSocketConfiguration instance of {@link SocketConfiguration} for TCP
   * @param socketIoHandler        instance of {@link SocketIoHandler}
   */
  public AcceptorHandler(String serverAddress,
                         ConnectionFilter connectionFilter,
                         ZeroReaderListener zeroReaderListener,
                         SocketConfiguration tcpSocketConfiguration,
                         SocketIoHandler socketIoHandler) {
    this.serverAddress = serverAddress;
    this.connectionFilter = connectionFilter;
    this.zeroReaderListener = zeroReaderListener;
    this.socketIoHandler = socketIoHandler;

    clientChannels = new ArrayList<>();
    serverChannels = new ArrayList<>();

    // opens a selector to handle server socket and accept all incoming client sockets
    try {
      acceptableSelector = Selector.open();
    } catch (IOException exception) {
      throw new ServiceRuntimeException(exception.getMessage());
    }

    // each socket configuration constructs a server socket
    bindServerSocketChannel(tcpSocketConfiguration.port());
  }

  private void bindServerSocketChannel(int port)
      throws ServiceRuntimeException {
    try {
      var serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      if (OsUtility.getOperatingSystemType() == OsUtility.OsType.WINDOWS) {
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
      } else {
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true);
      }
      serverSocketChannel.bind(new InetSocketAddress(serverAddress, port));
      if (isInfoEnabled()) {
        info("TCP SOCKET", buildgen("Started at address: ", serverAddress, ", port: ",
            serverSocketChannel.socket().getLocalPort()));
      }
      // only server socket should interest in this key OP_ACCEPT
      serverSocketChannel.register(acceptableSelector, SelectionKey.OP_ACCEPT);
      synchronized (serverChannels) {
        serverChannels.add(serverSocketChannel);
      }
    } catch (IOException e) {
      throw new ServiceRuntimeException(e.getMessage());
    }
  }

  private void registerClientChannel(SocketChannel socketChannel,
                                     SelectionKey acceptorSelectionKey) {
    if (socketChannel == null) {
      if (isDebugEnabled()) {
        debug("ACCEPTABLE CHANNEL", "Acceptor handles a null socket channel");
      }
    } else {
      var socket = socketChannel.socket();

      if (socket == null) {
        if (isDebugEnabled()) {
          debug("ACCEPTABLE CHANNEL", "Acceptor handles a null socket");
        }
      } else {
        var inetAddress = socket.getInetAddress();
        if (inetAddress != null) {
          try {
            connectionFilter.validateAndAddAddress(inetAddress.getHostAddress());
            socketChannel.configureBlocking(false);
            socketChannel.socket().setTcpNoDelay(true);
            zeroReaderListener.acceptClientSocketChannel(socketChannel,
                readerSelectionKey -> {
                  socketIoHandler.channelActive(socketChannel, readerSelectionKey);
                  synchronized (clientChannels) {
                    clientChannels.add(socketChannel);
                  }
                }, () -> {
                  try {
                    SocketUtility.closeSocket(socketChannel, acceptorSelectionKey);
                  } catch (IOException exception) {
                    error(exception, "It was unable to close this accepted channel: ",
                        exception.getMessage());
                  }
                }
            );
          } catch (RefusedConnectionAddressException exception1) {
            if (isErrorEnabled()) {
              error(exception1, "Refused connection with address: ", exception1.getMessage());
            }
            socketIoHandler.channelException(socketChannel, exception1);
            socketIoHandler.channelInactive(socketChannel,
                acceptorSelectionKey, ConnectionDisconnectMode.REFUSED_CONNECTION);
          } catch (IOException exception2) {
            if (isErrorEnabled()) {
              var logger = buildgen("Failed accepting connection: ");
              if (socketChannel.socket() != null) {
                logger.append(socketChannel.socket().getInetAddress().getHostAddress());
              }
              error(exception2, logger);
            }
            socketIoHandler.channelException(socketChannel, exception2);
            socketIoHandler.channelInactive(socketChannel, acceptorSelectionKey,
                ConnectionDisconnectMode.UNKNOWN);
          }
        }
      }
    }
  }

  private void shutdownClientChannels() {
    synchronized (clientChannels) {
      // iterates the list of client socket channels
      var socketIterator = clientChannels.iterator();

      while (socketIterator.hasNext()) {
        var socketChannel = socketIterator.next();
        var selectionKey = socketChannel.keyFor(acceptableSelector);
        socketIterator.remove();
        socketIoHandler.channelInactive(socketChannel, selectionKey,
            ConnectionDisconnectMode.SERVER_DOWN);
      }
    }
  }

  private void shutdownServerChannels() {
    synchronized (serverChannels) {
      // iterates the list of server socket channels, datagram channels
      var boundSocketIterator = serverChannels.iterator();

      while (boundSocketIterator.hasNext()) {
        var socketChannel = boundSocketIterator.next();
        boundSocketIterator.remove();

        try {
          socketChannel.close();
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }
    }
  }

  private void shutdownSelector() {
    try {
      Thread.sleep(500L);
      acceptableSelector.close();
    } catch (IOException | InterruptedException exception) {
      if (isErrorEnabled()) {
        error(exception);
      }
    }
  }

  /**
   * Processing. This should be run in a loop.
   *
   * @throws IOException whenever an IO exception thrown
   */
  public void running() throws IOException {
    // blocks until at least one channel is ready for the events you registered for
    int countReadyKeys = acceptableSelector.select();

    if (countReadyKeys == 0) {
      return;
    }

    // retrieves a set of selected keys
    var selectedKeys = acceptableSelector.selectedKeys();
    var keyIterator = selectedKeys.iterator();

    while (keyIterator.hasNext()) {
      // iterates the list of selection keys
      var selectionKey = keyIterator.next();
      // once a key is proceeded, it should be removed from the process to prevent
      // duplicating manipulation
      keyIterator.remove();

      // a client socket was accepted by a server socket
      // we only interest in this event
      if (selectionKey.isAcceptable()) {
        try {
          // get the server socket channel from the selector
          var serverChannel = (ServerSocketChannel) selectionKey.channel();
          // and accept the incoming request from client
          var clientChannel = serverChannel.accept();

          // make sure that the socket is available
          if (clientChannel != null) {
            registerClientChannel(clientChannel, selectionKey);
          }
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }
    }
  }

  /**
   * Shutdown processing.
   */
  public void shutdown() {
    shutdownServerChannels();
    shutdownClientChannels();
    shutdownSelector();
  }
}
