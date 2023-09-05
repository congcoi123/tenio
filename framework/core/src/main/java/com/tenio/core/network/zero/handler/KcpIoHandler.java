/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero.handler;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.core.network.entity.session.Session;

/**
 * The KCP channel behaviours.
 *
 * @since 0.3.0
 */
public interface KcpIoHandler extends BaseIoHandler {

  /**
   * Retrieves the data serialization type.
   *
   * @return the {@link DataType} value
   */
  DataType getDataType();

  /**
   * Sets the data serialization type.
   *
   * @param dataType the {@link DataType} value
   * @since 0.5.0
   */
  void setDataType(DataType dataType);

  /**
   * When a new KCP channel is established in a session.
   *
   * @param session the target {@link Session} which is using KCP transport
   */
  void channelActiveIn(Session session);

  /**
   * When a new message comes from a session then this method is invoked.
   *
   * @param session the {@link Session} using to communicate to client side
   * @param message an instance of {@link DataCollection} sent from client side, it can be in
   *                different formats base on {@link DataType} value defined in {@code
   *                configuration.xml}
   */
  void sessionRead(Session session, DataCollection message);

  /**
   * When a new KCP channel is removed from a session.
   *
   * @param session the {@link Session} which abandons using KCP transport
   */
  void channelInactiveIn(Session session);
}
