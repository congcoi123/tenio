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

package com.tenio.core.network.zero.engine.implement;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.utility.SocketUtility;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.engine.reader.DatagramReaderHandler;
import com.tenio.core.network.zero.engine.reader.SocketReaderHandler;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * The implementation for the reader engine.
 *
 * @see ZeroReader
 */
public final class ZeroReaderImpl extends AbstractZeroEngine
    implements ZeroReader, ZeroReaderListener {

  private static final AtomicInteger INDEXER = new AtomicInteger(0);

  private volatile List<SocketReaderHandler> socketReaderHandlers;
  private DatagramReaderHandler datagramReaderHandler;
  private DatagramPacketPolicy datagramPacketPolicy;
  private String serverAddress;
  private SocketConfiguration udpChannelConfiguration;
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

  private SocketReaderHandler getSocketReaderHandler() {
    int index =
        Math.floorMod(INDEXER.getAndIncrement(), getThreadPoolSize() - getNumberOfExtraWorkers());
    return socketReaderHandlers.get(index);
  }

  @Override
  public void acceptClientSocketChannel(SocketChannel socketChannel,
                                        Consumer<SelectionKey> onSuccess,
                                        Runnable onFailed) {
    getSocketReaderHandler().registerClientSocketChannel(socketChannel, onSuccess, onFailed);
  }

  @Override
  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  @Override
  public void setUdpChannelConfiguration(SocketConfiguration udpChannelConfiguration) {
    this.udpChannelConfiguration = udpChannelConfiguration;
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
  public void setDatagramPacketPolicy(DatagramPacketPolicy datagramPacketPolicy) {
    this.datagramPacketPolicy = datagramPacketPolicy;
  }

  @Override
  public void onInitialized() {
    // multiple socket reader handlers
    socketReaderHandlers = new ArrayList<>(getThreadPoolSize() - getNumberOfExtraWorkers());
    // but only one datagram reader handler allowed
    if (udpChannelConfiguration != null) {
      try {
        datagramReaderHandler =
            new DatagramReaderHandler(SocketUtility.createReaderBuffer(getMaxBufferSize()),
                getSessionManager(), getSocketIoHandler().getPacketDecoder(),
                getNetworkReaderStatistic(), getDatagramIoHandler(), datagramPacketPolicy);
        datagramReaderHandler.openDatagramChannels(serverAddress, udpChannelConfiguration.port(),
            udpChannelConfiguration.cacheSize());
      } catch (IOException exception) {
        error(exception);
      }
    }
  }

  @Override
  public void onStarted() {
    if (datagramReaderHandler != null) {
      runningExtraWorking(() -> {
        while (!Thread.currentThread().isInterrupted()) {
          if (isActivated()) {
            try {
              datagramReaderHandler.running();
            } catch (Throwable cause) {
              if (isErrorEnabled()) {
                error(cause);
              }
            }
          }
        }
      });
    }
  }

  @Override
  public void onRunning() {
    try {
      var readerHandler =
          new SocketReaderHandler(SocketUtility.createReaderBuffer(getMaxBufferSize()),
              getSessionManager(), getNetworkReaderStatistic(), getSocketIoHandler());
      socketReaderHandlers.add(readerHandler);

      while (!Thread.currentThread().isInterrupted()) {
        if (isActivated()) {
          try {
            readerHandler.running();
          } catch (Throwable cause) {
            if (isErrorEnabled()) {
              error(cause);
            }
          }
        }
      }
    } catch (IOException exception) {
      if (isErrorEnabled()) {
        error(exception);
      }
    }
  }

  @Override
  public int getNumberOfExtraWorkers() {
    return udpChannelConfiguration != null ? 1 : 0;
  }

  @Override
  public void onShutdown() {
    try {
      for (SocketReaderHandler socketReaderHandler : socketReaderHandlers) {
        socketReaderHandler.shutdown();
      }
      if (datagramReaderHandler != null) {
        datagramReaderHandler.shutdown();
      }
    } catch (IOException exception) {
      if (isErrorEnabled()) {
        error(exception, "Exception while closing the selector");
      }
    }
  }

  @Override
  public void onDestroyed() {
    // do nothing
  }
}
