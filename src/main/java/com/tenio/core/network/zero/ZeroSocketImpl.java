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

package com.tenio.core.network.zero;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.ZeroWriter;
import com.tenio.core.network.zero.engine.implement.ZeroAcceptorImpl;
import com.tenio.core.network.zero.engine.implement.ZeroReaderImpl;
import com.tenio.core.network.zero.engine.implement.ZeroWriterImpl;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import com.tenio.core.network.zero.handler.implement.DatagramIoHandlerImpl;
import com.tenio.core.network.zero.handler.implement.SocketIoHandlerImpl;

/**
 * The implementation for the socket service manager.
 *
 * @see ZeroSocket
 */
public final class ZeroSocketImpl extends AbstractManager implements ZeroSocket {

  private final ZeroAcceptor acceptor;
  private final ZeroReader reader;
  private final ZeroWriter writer;

  private final DatagramIoHandler datagramIoHandler;
  private final SocketIoHandler socketIoHandler;

  private boolean initialized;

  private ZeroSocketImpl(EventManager eventManager) {
    super(eventManager);

    acceptor = ZeroAcceptorImpl.newInstance(eventManager);
    reader = ZeroReaderImpl.newInstance(eventManager);
    writer = ZeroWriterImpl.newInstance(eventManager);

    datagramIoHandler = DatagramIoHandlerImpl.newInstance(eventManager);
    socketIoHandler = SocketIoHandlerImpl.newInstance(eventManager);

    initialized = false;
  }

  /**
   * Creates a new instance of the socket service.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link ZeroSocket}
   */
  public static ZeroSocket newInstance(EventManager eventManager) {
    return new ZeroSocketImpl(eventManager);
  }

  private void setupAcceptor() {
    acceptor.setDatagramIoHandler(datagramIoHandler);
    acceptor.setSocketIoHandler(socketIoHandler);
    acceptor.setZeroReaderListener((ZeroReaderListener) reader);
  }

  private void setupReader() {
    reader.setDatagramIoHandler(datagramIoHandler);
    reader.setSocketIoHandler(socketIoHandler);
  }

  private void setupWriter() {
    writer.setDatagramIoHandler(datagramIoHandler);
    writer.setSocketIoHandler(socketIoHandler);
  }

  @Override
  public void initialize() {
    setupAcceptor();
    setupReader();
    setupWriter();

    reader.initialize();
    writer.initialize();
    acceptor.initialize();

    initialized = true;
  }

  @Override
  public void start() {
    if (!initialized) {
      return;
    }

    reader.start();
    writer.start();
    acceptor.start();
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }

    acceptor.shutdown();
    reader.shutdown();
    writer.shutdown();
  }

  @Override
  public void activate() {
    reader.activate();
    writer.activate();
    acceptor.activate();
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
    acceptor.setServerAddress(serverAddress);
    reader.setServerAddress(serverAddress);
  }

  @Override
  public void setAcceptorBufferSize(int bufferSize) {
    acceptor.setMaxBufferSize(bufferSize);
  }

  @Override
  public void setAcceptorWorkerSize(int workerSize) {
    acceptor.setThreadPoolSize(workerSize);
  }

  @Override
  public void setReaderBufferSize(int bufferSize) {
    reader.setMaxBufferSize(bufferSize);
  }

  @Override
  public void setReaderWorkerSize(int workerSize) {
    reader.setThreadPoolSize(workerSize);
  }

  @Override
  public void setWriterBufferSize(int bufferSize) {
    writer.setMaxBufferSize(bufferSize);
  }

  @Override
  public void setWriterWorkerSize(int workerSize) {
    writer.setThreadPoolSize(workerSize);
  }

  @Override
  public void setConnectionFilter(ConnectionFilter connectionFilter) {
    acceptor.setConnectionFilter(connectionFilter);
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    acceptor.setSessionManager(sessionManager);
    reader.setSessionManager(sessionManager);
    writer.setSessionManager(sessionManager);

    datagramIoHandler.setSessionManager(sessionManager);
    socketIoHandler.setSessionManager(sessionManager);
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    reader.setNetworkReaderStatistic(networkReaderStatistic);

    datagramIoHandler.setNetworkReaderStatistic(networkReaderStatistic);
    socketIoHandler.setNetworkReaderStatistic(networkReaderStatistic);
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    writer.setNetworkWriterStatistic(networkWriterStatistic);
  }

  @Override
  public void setSocketConfigurations(SocketConfiguration tcpSocketConfiguration,
                                      SocketConfiguration udpChannelConfiguration) {
    acceptor.setSocketConfiguration(tcpSocketConfiguration);
    reader.setUdpChannelConfiguration(udpChannelConfiguration);
  }

  @Override
  public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
    writer.setPacketEncoder(packetEncoder);
  }

  @Override
  public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
    socketIoHandler.setPacketDecoder(packetDecoder);
  }

  @Override
  public void setDatagramPacketPolicy(DatagramPacketPolicy datagramPacketPolicy) {
    reader.setDatagramPacketPolicy(datagramPacketPolicy);
  }

  @Override
  public int getMaximumStartingTimeInMilliseconds() {
    int acceptorStartingTime = acceptor.getMaximumStartingTimeInMilliseconds();
    int readerStartingTime = reader.getMaximumStartingTimeInMilliseconds();
    int writerStartingTime = writer.getMaximumStartingTimeInMilliseconds();
    return Math.max(acceptorStartingTime, Math.max(readerStartingTime, writerStartingTime));
  }

  @Override
  public void write(Packet packet) {
    writer.enqueuePacket(packet);
  }
}
