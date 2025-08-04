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
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.OsUtility;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.utility.SocketUtility;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Handles read/write events on datagram channels using a {@link Selector}.
 *
 * <p>This class is part of the NIO event-driven loop that processes IO on
 * accepted bound UDP datagram channels.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Register datagram channels for read/write events</li>
 *   <li>Dispatch readable/writable keys to appropriate handlers</li>
 *   <li>Manage selection loop and wakeup mechanisms</li>
 *   <li>Notify {@link DatagramIoHandler} for channel lifecycle events</li>
 * </ul>
 *
 * <p>Each reader thread runs in a loop, polling its selector and reacting
 * to channel readiness, ensuring non-blocking high-performance IO handling.
 *
 * @see DatagramIoHandler
 * @since 0.6.6
 */

public final class DatagramReaderHandler extends SystemLogger {

  /**
   * This selector manages {@link DatagramChannel} instances.
   */
  private final Selector readableSelector;
  private final ByteBuffer readerBuffer;
  private final SessionManager sessionManager;
  private final BinaryPacketDecoder binaryPacketDecoder;
  private final NetworkReaderStatistic networkReaderStatistic;
  private final DatagramIoHandler datagramIoHandler;
  private final DatagramPacketPolicy datagramPacketPolicy;

  /**
   * Constructor.
   *
   * @param readerBuffer           instance of {@link ByteBuffer}
   * @param sessionManager         instance of {@link SessionManager}
   * @param binaryPacketDecoder    instance of {@link BinaryPacketDecoder}
   * @param networkReaderStatistic instance of {@link NetworkReaderStatistic}
   * @param datagramIoHandler      instance of {@link DatagramIoHandler}
   * @param datagramPacketPolicy   instance of {@link DatagramPacketPolicy}
   * @throws IOException whenever any IO exception thrown
   */
  public DatagramReaderHandler(ByteBuffer readerBuffer,
                               SessionManager sessionManager,
                               BinaryPacketDecoder binaryPacketDecoder,
                               NetworkReaderStatistic networkReaderStatistic,
                               DatagramIoHandler datagramIoHandler,
                               DatagramPacketPolicy datagramPacketPolicy) throws IOException {
    this.readerBuffer = readerBuffer;
    this.sessionManager = sessionManager;
    this.binaryPacketDecoder = binaryPacketDecoder;
    this.networkReaderStatistic = networkReaderStatistic;
    this.datagramIoHandler = datagramIoHandler;
    this.datagramPacketPolicy = datagramPacketPolicy;

    readableSelector = Selector.open();
  }

  /**
   * Shutdown processing.
   *
   * @throws IOException whenever IO exceptions thrown
   */
  public void shutdown() throws IOException {
    readableSelector.wakeup();
    SocketUtility.shutdownSelector(readableSelector);
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
        var datagramChannel = (DatagramChannel) selectableChannel;
        readUpdData(datagramChannel, selectionKey, readerBuffer);
      }
    }
  }

  /**
   * Open datagram channels.
   *
   * @param serverAddress the server IP address
   * @param port          datagram (UDP) port
   * @param cacheSize     the number of datagram channels that registers in the same selector
   * @throws ServiceRuntimeException whenever there is exception occurred
   */
  public void openDatagramChannels(String serverAddress, int port, int cacheSize)
      throws ServiceRuntimeException {
    if (cacheSize <= 0) {
      throw new IllegalArgumentException("The cache size of datagram channels must be greater " +
          "than 0");
    }
    try {
      for (int i = 0; i < cacheSize; i++) {
        var datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        // this does not guarantee expected behaviours on windows or macOS
        if (OsUtility.getOperatingSystemType() == OsUtility.OsType.WINDOWS) {
          datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        } else {
          datagramChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true);
        }
        datagramChannel.setOption(StandardSocketOptions.SO_BROADCAST, true);
        datagramChannel.bind(new InetSocketAddress(serverAddress, port));
        // udp datagram is a connectionless protocol, we don't need to create
        // bi-direction connection, that why it's not necessary to register it to
        // acceptable selector. Just leave it to the reader selector later
        datagramChannel.register(readableSelector, SelectionKey.OP_READ);
      }
      if (isInfoEnabled()) {
        info("UDP CHANNEL(S)", buildgen("Opened at address: ", serverAddress, ", port: ",
            port, ", cacheSize: ", cacheSize));
      }
    } catch (IOException exception) {
      throw new ServiceRuntimeException(exception.getMessage());
    }
  }

  private void readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {
    Session session;

    if (selectionKey.isValid() && selectionKey.isReadable()) {
      // prepares the buffer first
      readerBuffer.clear();
      // reads data from socket and write them to buffer
      SocketAddress remoteAddress;
      try {
        remoteAddress = datagramChannel.receive(readerBuffer);
      } catch (IOException exception) {
        if (isErrorEnabled()) {
          error(exception, "An exception was occurred on channel: ", datagramChannel.toString());
        }
        datagramIoHandler.channelException(datagramChannel, exception);
        return;
      }

      if (remoteAddress == null) {
        var addressNotFoundException =
            new RuntimeException("Remove address for the datagram channel");
        if (isErrorEnabled()) {
          error(addressNotFoundException, "An exception was occurred on channel: ",
              datagramChannel.toString());
        }
        datagramIoHandler.channelException(datagramChannel, addressNotFoundException);
        return;
      }

      int byteCount = readerBuffer.position();

      // update statistic data
      networkReaderStatistic.updateReadBytes(byteCount);
      // ready to read data from buffer
      readerBuffer.flip();
      // reads data from buffer and transfers them to the next process
      byte[] binaries = new byte[readerBuffer.limit()];
      readerBuffer.get(binaries);

      // convert binary to dataCollection object
      var dataCollection = binaryPacketDecoder.decode(binaries);

      // retrieves session by its datagram channel, hence we are using only one
      // datagram channel for all sessions, we use incoming request convey ID to
      // distinguish them
      var processedDataCollection = datagramPacketPolicy.applyPolicy(dataCollection);
      int udpConvey = processedDataCollection.getLeft();
      DataCollection message = processedDataCollection.getRight();

      session = sessionManager.getSessionByDatagram(udpConvey);

      if (session == null) {
        datagramIoHandler.channelRead(datagramChannel, remoteAddress, message);
      } else {
        if (session.isActivated()) {
          // When a client (like A or B) is behind a NAT (Network Address Translation), its
          // private/internal IP and port are not directly visible to your server.
          // Instead, the NAT device assigns a temporary public IP + port mapping like:
          // Private client (B): 192.168.1.5:54321
          // → NAT mapping →
          // Public IP: 203.0.113.42:61724
          // But these mappings are temporary and will expire after some idle time (often 30
          // seconds to a few minutes) if there's no traffic.
          // Solution:
          // Every 10 to 30 seconds, the client sends this to the server.
          // The effects:
          // - Keeps the NAT mapping alive (prevents expiry).
          // - Server can update the client’s SocketAddress if the NAT changes the port dynamically.
          session.setDatagramRemoteAddress(remoteAddress);
          session.addReadBytes(byteCount);
          datagramIoHandler.sessionRead(session, message);
        } else {
          if (isDebugEnabled()) {
            debug("READ UDP CHANNEL", "Session is inactivated: ", session.toString());
          }
        }
      }
    }
  }
}
