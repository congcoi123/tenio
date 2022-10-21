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

package com.tenio.core.network.zero;

import com.tenio.common.data.DataType;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.ZeroWriter;
import com.tenio.core.network.zero.engine.implement.ZeroAcceptorImpl;
import com.tenio.core.network.zero.engine.implement.ZeroReaderImpl;
import com.tenio.core.network.zero.engine.implement.ZeroWriterImpl;
import com.tenio.core.network.zero.engine.listener.ZeroAcceptorListener;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.engine.listener.ZeroWriterListener;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import com.tenio.core.network.zero.handler.implement.DatagramIoHandlerImpl;
import com.tenio.core.network.zero.handler.implement.SocketIoHandlerImpl;
import java.util.List;

/**
 * The implementation for the socket service manager.
 *
 * @see ZeroSocketService
 */
public final class ZeroSocketServiceImpl extends AbstractManager implements ZeroSocketService {

  private ZeroAcceptor acceptorEngine;
  private ZeroReader readerEngine;
  private ZeroWriter writerEngine;

  private DatagramIoHandler datagramIoHandler;
  private SocketIoHandler socketIoHandler;

  private boolean initialized;

  private ZeroSocketServiceImpl(EventManager eventManager) {
    super(eventManager);

    acceptorEngine = ZeroAcceptorImpl.newInstance(eventManager);
    readerEngine = ZeroReaderImpl.newInstance(eventManager);
    writerEngine = ZeroWriterImpl.newInstance(eventManager);

    datagramIoHandler = DatagramIoHandlerImpl.newInstance(eventManager);
    socketIoHandler = SocketIoHandlerImpl.newInstance(eventManager);

    initialized = false;
  }

  public static ZeroSocketService newInstance(EventManager eventManager) {
    return new ZeroSocketServiceImpl(eventManager);
  }

  private void setupAcceptor() {
    acceptorEngine.setDatagramIoHandler(datagramIoHandler);
    acceptorEngine.setSocketIoHandler(socketIoHandler);
    acceptorEngine.setZeroReaderListener((ZeroReaderListener) readerEngine);
  }

  private void setupReader() {
    readerEngine.setDatagramIoHandler(datagramIoHandler);
    readerEngine.setSocketIoHandler(socketIoHandler);
    readerEngine.setZeroAcceptorListener((ZeroAcceptorListener) acceptorEngine);
    readerEngine.setZeroWriterListener((ZeroWriterListener) writerEngine);
  }

  private void setupWriter() {
    writerEngine.setDatagramIoHandler(datagramIoHandler);
    writerEngine.setSocketIoHandler(socketIoHandler);
  }

  @Override
  public void initialize() {
    setupAcceptor();
    setupReader();
    setupWriter();

    readerEngine.initialize();
    writerEngine.initialize();
    acceptorEngine.initialize();

    initialized = true;
  }

  @Override
  public void start() {
    if (!initialized) {
      return;
    }

    acceptorEngine.start();
    readerEngine.start();
    writerEngine.start();
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }

    acceptorEngine.shutdown();
    readerEngine.shutdown();
    writerEngine.shutdown();

    destroy();
  }

  private void destroy() {
    datagramIoHandler = null;
    socketIoHandler = null;

    acceptorEngine = null;
    readerEngine = null;
    writerEngine = null;
  }

  @Override
  public boolean isActivated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "zero-socket";
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAcceptorServerAddress(String serverAddress) {
    acceptorEngine.setServerAddress(serverAddress);
  }

  @Override
  public void setAcceptorAmountUdpWorkers(int amountUdpWorkers) {
    acceptorEngine.setAmountUdpWorkers(amountUdpWorkers);
  }

  @Override
  public void setAcceptorBufferSize(int bufferSize) {
    acceptorEngine.setMaxBufferSize(bufferSize);
  }

  @Override
  public void setAcceptorWorkerSize(int workerSize) {
    acceptorEngine.setThreadPoolSize(workerSize);
  }

  @Override
  public void setAcceptorEnabledKcp(boolean enabledKcp) {
    acceptorEngine.setEnabledKcp(enabledKcp);
  }

  @Override
  public void setReaderBufferSize(int bufferSize) {
    readerEngine.setMaxBufferSize(bufferSize);
  }

  @Override
  public void setReaderWorkerSize(int workerSize) {
    readerEngine.setThreadPoolSize(workerSize);
  }

  @Override
  public void setWriterBufferSize(int bufferSize) {
    writerEngine.setMaxBufferSize(bufferSize);
  }

  @Override
  public void setWriterWorkerSize(int workerSize) {
    writerEngine.setThreadPoolSize(workerSize);
  }

  @Override
  public void setConnectionFilter(ConnectionFilter connectionFilter) {
    acceptorEngine.setConnectionFilter(connectionFilter);
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    acceptorEngine.setSessionManager(sessionManager);
    readerEngine.setSessionManager(sessionManager);
    writerEngine.setSessionManager(sessionManager);

    datagramIoHandler.setSessionManager(sessionManager);
    socketIoHandler.setSessionManager(sessionManager);
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    readerEngine.setNetworkReaderStatistic(networkReaderStatistic);

    datagramIoHandler.setNetworkReaderStatistic(networkReaderStatistic);
    socketIoHandler.setNetworkReaderStatistic(networkReaderStatistic);
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    writerEngine.setNetworkWriterStatistic(networkWriterStatistic);
  }

  @Override
  public void setSocketConfigs(List<SocketConfig> socketConfigs) {
    acceptorEngine.setSocketConfigs(socketConfigs);
  }

  @Override
  public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
    writerEngine.setPacketEncoder(packetEncoder);
  }

  @Override
  public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
    socketIoHandler.setPacketDecoder(packetDecoder);
  }

  @Override
  public void setDataType(DataType dataType) {
    datagramIoHandler.setDataType(dataType);
    socketIoHandler.setDataType(dataType);
  }

  @Override
  public void write(Packet packet) {
    writerEngine.enqueuePacket(packet);
  }
}
