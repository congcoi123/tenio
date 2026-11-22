/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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
public final class ZeroReaderImpl extends AbstractZeroEngine implements ZeroReader, ZeroReaderListener {

  private final AtomicInteger socketReaderSelectionIndexer = new AtomicInteger();
  private final AtomicInteger socketReaderWorkerIndexer = new AtomicInteger();

  private volatile List<SocketReaderHandler> socketReaderHandlers = List.of();
  private volatile List<DatagramReaderHandler> datagramReaderHandlers = List.of();
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
    var handlers = socketReaderHandlers;
    if (handlers.isEmpty()) {
      throw new IllegalStateException("No socket reader handlers are available");
    }
    int index = Math.floorMod(socketReaderSelectionIndexer.getAndIncrement(), handlers.size());
    return handlers.get(index);
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
    socketReaderHandlers = List.of();
    datagramReaderHandlers = List.of();
  }

  @Override
  protected void onStarting() {
    socketReaderSelectionIndexer.set(0);
    socketReaderWorkerIndexer.set(0);

    var initializedSocketReaderHandlers = new ArrayList<SocketReaderHandler>(
            getThreadPoolSize() - getNumberOfExtraWorkers());
    var initializedDatagramReaderHandlers = new ArrayList<DatagramReaderHandler>(
            getNumberOfExtraWorkers());

    try {
      for (int i = 0; i < getThreadPoolSize() - getNumberOfExtraWorkers(); i++) {
        initializedSocketReaderHandlers.add(new SocketReaderHandler(
                SocketUtility.createReaderBuffer(getMaxBufferSize()), getSessionManager(),
                getNetworkReaderStatistic(), getSocketIoHandler()));
      }

      if (udpChannelConfiguration != null) {
        for (int i = 0; i < getNumberOfExtraWorkers(); i++) {
          var datagramReaderHandler = new DatagramReaderHandler(
                  SocketUtility.createReaderBuffer(getMaxBufferSize()), getSessionManager(),
                  getSocketIoHandler().getPacketDecoder(), getNetworkReaderStatistic(),
                  getDatagramIoHandler(), datagramPacketPolicy);
          initializedDatagramReaderHandlers.add(datagramReaderHandler);
          datagramReaderHandler.openDatagramChannels(serverAddress, udpChannelConfiguration.port(),
                  udpChannelConfiguration.cacheSize());
        }
      }
    } catch (IOException | ServiceRuntimeException exception) {
      shutdownSocketReaderHandlers(initializedSocketReaderHandlers);
      shutdownDatagramReaderHandlers(initializedDatagramReaderHandlers);
      throw new ServiceRuntimeException("Unable to initialize reader handlers", exception);
    }

    socketReaderHandlers = List.copyOf(initializedSocketReaderHandlers);
    datagramReaderHandlers = List.copyOf(initializedDatagramReaderHandlers);
  }

  @Override
  public void onStarted() {
    for (DatagramReaderHandler datagramReaderHandler : datagramReaderHandlers) {
      run(() -> runDatagramReaderHandler(datagramReaderHandler), "datagram");
    }
  }

  @Override
  public void onRunning() {
    int index = socketReaderWorkerIndexer.getAndIncrement();
    var handlers = socketReaderHandlers;
    if (index >= handlers.size()) {
      throw new IllegalStateException("Socket reader worker started without an assigned handler");
    }
    var socketReaderHandler = handlers.get(index);

    while (!Thread.currentThread().isInterrupted() && !isStopping()) {
      if (isActivated()) {
        try {
          socketReaderHandler.running();
        } catch (Throwable cause) {
          if (isErrorEnabled()) {
            error(cause);
          }
        }
      }
    }
  }

  @Override
  public int getNumberOfExtraWorkers() {
    return udpChannelConfiguration != null ? getThreadPoolSize() <= 1 ? 0 : getThreadPoolSize() / 2 : 0;
  }

  @Override
  public void onShutdown() {
    shutdownSocketReaderHandlers(socketReaderHandlers);
    shutdownDatagramReaderHandlers(datagramReaderHandlers);
  }

  private void runDatagramReaderHandler(DatagramReaderHandler datagramReaderHandler) {
    while (!Thread.currentThread().isInterrupted() && !isStopping()) {
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
  }

  private void shutdownSocketReaderHandlers(List<SocketReaderHandler> handlers) {
    for (SocketReaderHandler socketReaderHandler : handlers) {
      try {
        socketReaderHandler.shutdown();
      } catch (Exception exception) {
        if (isErrorEnabled()) {
          error(exception, "Exception while closing socket reader");
        }
      }
    }
  }

  private void shutdownDatagramReaderHandlers(List<DatagramReaderHandler> handlers) {
    for (DatagramReaderHandler datagramReaderHandler : handlers) {
      try {
        datagramReaderHandler.shutdown();
      } catch (Exception exception) {
        if (isErrorEnabled()) {
          error(exception, "Exception while closing datagram reader");
        }
      }
    }
  }

  @Override
  public void onDestroyed() {
    // Do nothing
  }
}
