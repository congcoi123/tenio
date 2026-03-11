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
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.acceptor.AcceptorHandler;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for acceptor engine.
 *
 * @see ZeroAcceptor
 */
public final class ZeroAcceptorImpl extends AbstractZeroEngine implements ZeroAcceptor {

  private volatile List<AcceptorHandler> acceptorHandlers;
  private ConnectionFilter connectionFilter;
  private ZeroReaderListener zeroReaderListener;
  private String serverAddress;
  private SocketConfiguration tcpSocketConfiguration;

  private ZeroAcceptorImpl(EventManager eventManager) {
    super(eventManager);
    setName("acceptor");
  }

  /**
   * Creates a new instance of acceptor engine.
   *
   * @param eventManager           the instance of {@link EventManager}
   * @return a new instance of {@link ZeroAcceptor}
   */
  public static ZeroAcceptor newInstance(EventManager eventManager) {
    return new ZeroAcceptorImpl(eventManager);
  }

  @Override
  public void setConnectionFilter(ConnectionFilter filter) {
    connectionFilter = filter;
  }

  @Override
  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  @Override
  public void setSocketConfiguration(SocketConfiguration tcpSocketConfiguration) {
    this.tcpSocketConfiguration = tcpSocketConfiguration;
  }

  @Override
  public void setZeroReaderListener(ZeroReaderListener zeroReaderListener) {
    this.zeroReaderListener = zeroReaderListener;
  }

  @Override
  public void onInitialized() {
    acceptorHandlers = new ArrayList<>(getThreadPoolSize());
  }

  @Override
  public void onStarted() {
    // do nothing
  }

  @Override
  public void onRunning() {
    var acceptorHandler = new AcceptorHandler(serverAddress, connectionFilter, zeroReaderListener,
        tcpSocketConfiguration, getSocketIoHandler());
    acceptorHandlers.add(acceptorHandler);

    while (!Thread.currentThread().isInterrupted()) {
      if (isActivated()) {
        try {
          acceptorHandler.running();
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
    for (var acceptorHandler : acceptorHandlers) {
      acceptorHandler.shutdown();
    }
  }

  @Override
  public void onDestroyed() {
    // do nothing
  }
}
