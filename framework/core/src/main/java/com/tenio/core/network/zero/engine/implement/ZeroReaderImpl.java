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

package com.tenio.core.network.zero.engine.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.data.DataUtility;
import com.tenio.common.data.msgpack.element.MsgPackMap;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.listener.ZeroAcceptorListener;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.engine.listener.ZeroWriterListener;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import javassist.NotFoundException;

/**
 * The implementation for the reader engine.
 *
 * @see ZeroReader
 */
public final class ZeroReaderImpl extends AbstractZeroEngine
    implements ZeroReader, ZeroReaderListener {

  private DataType dataType;
  private ZeroAcceptorListener zeroAcceptorListener;
  private ZeroWriterListener zeroWriterListener;
  private Selector readableSelector;
  private NetworkReaderStatistic networkReaderStatistic;

  private ZeroReaderImpl(EventManager eventManager) {
    super(eventManager);
    setName("reader");
  }

  /**
   * Creates a new instance of reader engine.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link ZeroReader}
   */
  public static ZeroReader newInstance(EventManager eventManager) {
    return new ZeroReaderImpl(eventManager);
  }

  private void initializeSelector() throws ServiceRuntimeException {
    try {
      readableSelector = Selector.open();
    } catch (IOException e) {
      throw new ServiceRuntimeException(e.getMessage());
    }
  }

  private void readableLoop(ByteBuffer readerBuffer) {
    zeroAcceptorListener.handleAcceptableChannels();
    readIncomingSocketData(readerBuffer);
  }

  private void readIncomingSocketData(ByteBuffer readerBuffer) {
    SocketChannel socketChannel;
    DatagramChannel datagramChannel;
    SelectionKey selectionKey;

    try {
      // blocks until at least one channel is ready for the events you registered for
      int countReadyKeys = readableSelector.selectNow();

      if (countReadyKeys == 0) {
        return;
      }

      synchronized (readableSelector) {
        // readable selector was registered by OP_READ interested only socket channels,
        // but in some cases, we can receive "can writable" signal from those sockets
        var readyKeys = readableSelector.selectedKeys();
        var keyIterator = readyKeys.iterator();

        while (keyIterator.hasNext()) {
          selectionKey = keyIterator.next();
          // once a key is proceeded, it should be removed from the process to prevent
          // duplicating manipulation
          keyIterator.remove();

          if (selectionKey.isValid()) {
            var channel = selectionKey.channel();
            // we already registered 2 types of channels for this selector and need to
            // separate the processes
            if (channel instanceof SocketChannel) {
              socketChannel = (SocketChannel) channel;
              readTcpData(socketChannel, selectionKey, readerBuffer);
            } else if (channel instanceof DatagramChannel) {
              datagramChannel = (DatagramChannel) channel;
              readUpdData(datagramChannel, selectionKey, readerBuffer);
            }
          }
        }
      }
    } catch (ClosedSelectorException exception1) {
      if (isErrorEnabled()) {
        error(exception1, "Selector is closed: ", exception1.getMessage());
      }
    } catch (CancelledKeyException exception2) {
      if (isErrorEnabled()) {
        error(exception2, "Cancelled key: ", exception2.getMessage());
      }
    } catch (IOException exception3) {
      if (isErrorEnabled()) {
        error(exception3, "I/O reading/selection error: ", exception3.getMessage());
      }
    } catch (Exception exception4) {
      if (isErrorEnabled()) {
        error(exception4, "Generic reading/selection error: ", exception4.getMessage());
      }
    }
  }

  private void readTcpData(SocketChannel socketChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {
    // retrieves session by its socket channel
    var session = getSessionManager().getSessionBySocket(socketChannel);

    if (Objects.isNull(session)) {
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
          if (isErrorEnabled()) {
            error(exception, "An exception was occurred on channel: ", socketChannel.toString());
          }
          getSocketIoHandler().sessionException(session, exception);
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

        getSocketIoHandler().sessionRead(session, binary);
      }
    }
  }

  private void closeTcpConnection(SelectableChannel channel) {
    var socketChannel = (SocketChannel) channel;
    getSocketIoHandler().channelInactive(socketChannel);
    if (!socketChannel.socket().isClosed()) {
      try {
        socketChannel.socket().shutdownInput();
        socketChannel.socket().shutdownOutput();
        socketChannel.close();
      } catch (IOException exception) {
        if (isErrorEnabled()) {
          error(exception, "Error on closing socket channel: ", socketChannel.toString());
        }
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
        if (isErrorEnabled()) {
          error(exception, "An exception was occurred on channel: ", datagramChannel.toString());
        }
        getDatagramIoHandler().channelException(datagramChannel, exception);
        return;
      }

      if (Objects.isNull(remoteAddress)) {
        var addressNotFoundException =
            new NotFoundException("Remove address for the datagram channel");
        if (isErrorEnabled()) {
          error(addressNotFoundException, "An exception was occurred on channel: ",
              datagramChannel.toString());
        }
        getDatagramIoHandler().channelException(datagramChannel, addressNotFoundException);
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

      session = getSessionManager().getSessionByDatagram(udpConvey);

      if (Objects.isNull(session)) {
        getDatagramIoHandler().channelRead(datagramChannel, remoteAddress, message);
      } else {
        if (session.isActivated()) {
          session.setDatagramRemoteSocketAddress(remoteAddress);
          session.addReadBytes(byteCount);
          if (session.containsKcp()) {
            // At this time, UKCP knows who is its session
            session.getUkcp().input(binary);
          } else {
            getDatagramIoHandler().sessionRead(session, message);
          }
        } else {
          if (isDebugEnabled()) {
            debug("READ UDP CHANNEL", "Session is inactivated: ", session.toString());
          }
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

  @Override
  public void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException {
    datagramChannel.register(readableSelector, SelectionKey.OP_READ);
  }

  @Override
  public SelectionKey acceptSocketChannel(SocketChannel socketChannel)
      throws ClosedChannelException {
    return socketChannel.register(readableSelector, SelectionKey.OP_READ);
  }

  @Override
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public void setZeroAcceptorListener(ZeroAcceptorListener zeroAcceptorListener) {
    this.zeroAcceptorListener = zeroAcceptorListener;
  }

  @Override
  public void setZeroWriterListener(ZeroWriterListener zeroWriterListener) {
    this.zeroWriterListener = zeroWriterListener;
  }

  @Override
  public NetworkReaderStatistic getNetworkReaderStatistic() {
    return networkReaderStatistic;
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    this.networkReaderStatistic = networkReaderStatistic;
  }

  @Override
  public void wakeup() {
    if (isActivated()) {
      readableSelector.wakeup();
    }
  }

  @Override
  public void onInitialized() {
    initializeSelector();
  }

  @Override
  public void onStarted() {
    // do nothing
  }

  @Override
  public void onRunning() {
    ByteBuffer readerBuffer = ByteBuffer.allocate(getMaxBufferSize());

    while (true) {
      if (isActivated()) {
        try {
          readableLoop(readerBuffer);
        } catch (Throwable cause) {
          if (isErrorEnabled()) {
            error(cause);
          }
        }
      }
    }
  }

  @Override
  public void onShutdown() {
    try {
      Thread.sleep(500L);
      readableSelector.close();
    } catch (IOException | InterruptedException exception) {
      if (isErrorEnabled()) {
        error(exception, "Exception while closing the selector");
      }
    }
  }

  @Override
  public void onDestroyed() {
  }
}
