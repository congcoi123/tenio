/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
import javassist.NotFoundException;

/**
 * The implementation for the reader engine.
 *
 * @see ZeroReader
 */
public final class ZeroReaderImpl extends AbstractZeroEngine
    implements ZeroReader, ZeroReaderListener {

  private ZeroAcceptorListener zeroAcceptorListener;
  private ZeroWriterListener zeroWriterListener;
  private Selector readableSelector;
  private NetworkReaderStatistic networkReaderStatistic;

  private ZeroReaderImpl(EventManager eventManager) {
    super(eventManager);

    setName("reader");
  }

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
    SocketChannel socketChannel = null;
    DatagramChannel datagramChannel = null;
    SelectionKey selectionKey = null;

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

    } catch (ClosedSelectorException e1) {
      error(e1, "Selector is closed: ", e1.getMessage());
    } catch (CancelledKeyException e2) {
      error(e2, "Cancelled key: ", e2.getMessage());
    } catch (IOException e3) {
      error(e3, "I/O reading/selection error: ", e3.getMessage());
    } catch (Exception e4) {
      error(e4, "Generic reading/selection error: ", e4.getMessage());
    }
  }

  private void readTcpData(SocketChannel socketChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {
    // retrieves session by its socket channel
    var session = getSessionManager().getSessionBySocket(socketChannel);

    if (session == null) {
      debug("READ CHANNEL", "Reader handle a null session with the socket channel: ",
          socketChannel.toString());
      return;
    }

    // when a socket channel is writable, should make it highest priority
    // manipulation
    if (selectionKey.isWritable()) {
      // should continue put this session for sending all left packets first
      zeroWriterListener.continueWriteInterestOp(session);
      // now we should set it back to interest in OP_READ
      selectionKey.interestOps(SelectionKey.OP_READ);
    }

    if (selectionKey.isReadable()) {
      // prepares the buffer first
      readerBuffer.clear();
      // reads data from socket and write them to buffer
      int byteCount = -1;
      try {
        byteCount = socketChannel.read(readerBuffer);
      } catch (IOException e) {
        error(e, "An exception was occured on channel: ", socketChannel.toString());
        getSocketIoHandler().sessionException(session, e);
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
      } catch (IOException e) {
        error(e, "Error on closing socket channel: ", socketChannel.toString());
      }
    }
  }

  private void readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey,
                           ByteBuffer readerBuffer) {

    Session session = null;

    if (selectionKey.isReadable()) {
      // prepares the buffer first
      readerBuffer.clear();
      // reads data from socket and write them to buffer
      SocketAddress remoteAddress = null;
      try {
        remoteAddress = datagramChannel.receive(readerBuffer);
      } catch (IOException e) {
        error(e, "An exception was occured on channel: ", datagramChannel.toString());
        getDatagramIoHandler().channelException(datagramChannel, e);
      }

      if (remoteAddress == null) {
        var addressNotFoundException =
            new NotFoundException("Remove addess for the datagram channel");
        error(addressNotFoundException, "An exception was occured on channel: ",
            datagramChannel.toString());
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

      // retrieves session by its datagram channel, hence we are using only one
      // datagram channel for all sessions, we use incoming request remote address to
      // distinguish them
      session = getSessionManager().getSessionByDatagram(remoteAddress);

      if (session == null) {
        getDatagramIoHandler().channelRead(datagramChannel, remoteAddress, binary);
      } else {
        session.addReadBytes(byteCount);
        getDatagramIoHandler().sessionRead(session, binary);
      }
    }

    if (selectionKey.isWritable() && session != null) {
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
        readableLoop(readerBuffer);
      }
    }
  }

  @Override
  public void onShutdown() {
    try {
      Thread.sleep(500L);
      readableSelector.close();
    } catch (IOException | InterruptedException e) {
      error(e, "Exception while closing the selector");
    }
  }

  @Override
  public void onDestroyed() {
    readableSelector = null;
  }
}