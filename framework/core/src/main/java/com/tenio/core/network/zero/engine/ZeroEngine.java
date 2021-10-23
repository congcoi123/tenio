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

package com.tenio.core.network.zero.engine;

import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import com.tenio.core.service.Service;
import com.tenio.core.service.ServiceListener;

/**
 * The common APIs for a zero engine instance.
 */
public interface ZeroEngine extends Service, ServiceListener {

  /**
   * Retrieves the socket IO handler.
   *
   * @return the socket IO handler
   */
  SocketIoHandler getSocketIoHandler();

  /**
   * Set the socket IO handler.
   *
   * @param socketIoHandler the socket IO handler
   */
  void setSocketIoHandler(SocketIoHandler socketIoHandler);

  /**
   * Retrieves the datagram IO handler.
   *
   * @return the datagram IO handler
   */
  DatagramIoHandler getDatagramIoHandler();

  /**
   * Set the datagram IO handler.
   *
   * @param datagramIoHandler the datagram IO handler
   */
  void setDatagramIoHandler(DatagramIoHandler datagramIoHandler);

  /**
   * Retrieves the session manager.
   *
   * @return the session manager
   */
  SessionManager getSessionManager();

  /**
   * Set the session manager.
   *
   * @param sessionManager the session manager
   */
  void setSessionManager(SessionManager sessionManager);

  /**
   * Retrieves the thread pool size.
   *
   * @return the thread pool size
   */
  int getThreadPoolSize();

  /**
   * Set the thread pool size.
   *
   * @param maxSize the thread pool size
   */
  void setThreadPoolSize(int maxSize);

  /**
   * Retrieves the max buffer size.
   *
   * @return the max buffer size
   */
  int getMaxBufferSize();

  /**
   * Set the max buffer size.
   *
   * @param maxSize the max buffer size
   */
  void setMaxBufferSize(int maxSize);
}
