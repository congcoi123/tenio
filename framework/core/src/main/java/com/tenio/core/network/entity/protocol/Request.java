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

package com.tenio.core.network.entity.protocol;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.controller.RequestComparator;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A request created by the server when it received a message from a client.
 */
public interface Request {

  /**
   * The request counter to provide unique id for every single request.
   */
  AtomicLong ID_COUNTER = new AtomicLong(1L);

  /**
   * Retrieves the unique ID of request.
   *
   * @return the unique {@code long} ID of request
   */
  long getId();

  /**
   * Retrieves the server event associating to the request.
   *
   * @return a {@link ServerEvent} associating to the request
   */
  ServerEvent getEvent();

  /**
   * Sets a server event associating to the request.
   *
   * @param event a {@link ServerEvent} associating to the request
   * @return the pointer of request
   */
  Request setEvent(ServerEvent event);

  /**
   * Retrieves the sender of request.
   *
   * @return a {@link Object} that plays as a sender of the request
   */
  Object getSender();

  /**
   * Sets the sender of request.
   *
   * @param sender a {@link Object} that plays as a sender of the request
   * @return the pointer of request
   */
  Request setSender(Object sender);

  /**
   * Retrieves the remote address associating to the client side whenever the server receives
   * message from him.
   *
   * @return the remote address associating to the client side
   */
  SocketAddress getRemoteAddress();

  /**
   * Sets the remote address associating to the client side whenever the server receives
   * message from him.
   *
   * @param remoteAddress remote address associating to the client side
   * @return the request instance
   */
  Request setRemoteAddress(SocketAddress remoteAddress);

  /**
   * Retrieves the request message.
   *
   * @return an instance of {@link DataCollection}
   */
  DataCollection getMessage();

  /**
   * Sets the request message.
   *
   * @param message an instance of {@link DataCollection}
   * @return the {@link Request} itself
   */
  Request setMessage(DataCollection message);

  /**
   * Retrieves the priority of request.
   *
   * @return {@code integer number} priority of request
   * @see RequestComparator
   */
  int getPriority();

  /**
   * Sets priority for the request.
   *
   * @param priority {@code integer number} priority of request
   * @return the pointer of request
   * @see RequestComparator
   */
  Request setPriority(int priority);

  /**
   * Retrieves the creation timestamp in milliseconds.
   *
   * @return the creation timestamp in milliseconds ({@code long} value)
   */
  long getCreatedTimestamp();
}
