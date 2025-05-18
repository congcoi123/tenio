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

package com.tenio.core.network.zero.engine;

import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import com.tenio.core.service.Service;
import com.tenio.core.service.ServiceListener;

/**
 * The common APIs for a zero engine.
 */
public interface ZeroEngine extends Service, ServiceListener {

  /**
   * Retrieves the socket IO handler.
   *
   * @return a {@link SocketIoHandler} instance
   */
  SocketIoHandler getSocketIoHandler();

  /**
   * Sets the socket IO handler.
   *
   * @param socketIoHandler a {@link SocketIoHandler} instance
   */
  void setSocketIoHandler(SocketIoHandler socketIoHandler);

  /**
   * Retrieves the Datagram IO handler.
   *
   * @return a {@link DatagramIoHandler} instance
   */
  DatagramIoHandler getDatagramIoHandler();

  /**
   * Sets a Datagram IO handler.
   *
   * @param datagramIoHandler a {@link DatagramIoHandler} instance
   */
  void setDatagramIoHandler(DatagramIoHandler datagramIoHandler);

  /**
   * Retrieves the Session Manager.
   *
   * @return a {@link SessionManager} instance
   */
  SessionManager getSessionManager();

  /**
   * Sets a Session Manager.
   *
   * @param sessionManager a {@link SessionManager} instance
   */
  void setSessionManager(SessionManager sessionManager);

  /**
   * Retrieves the thread pool size.
   *
   * @return the thread pool size ({@code integer} value)
   */
  int getThreadPoolSize();

  /**
   * Sets the thread pool size.
   *
   * @param maxSize the thread pool size ({@code integer} value)
   */
  void setThreadPoolSize(int maxSize);

  /**
   * Retrieves the maximum value of buffer size.
   *
   * @return the maximum value buffer size ({@code integer} value)
   */
  int getMaxBufferSize();

  /**
   * Sets the maximum value of buffer size.
   *
   * @param maxSize the maximum value buffer size ({@code integer} value)
   */
  void setMaxBufferSize(int maxSize);
}
