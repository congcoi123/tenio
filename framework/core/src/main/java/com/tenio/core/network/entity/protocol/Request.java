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

package com.tenio.core.network.entity.protocol;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.controller.RequestComparator;
import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.entity.session.Session;

/**
 * A request created by the server when it received a message from a client.
 */
public interface Request {

  /**
   * Retrieves the unique ID of request.
   *
   * @return the unique {@code long} ID of request
   */
  long getId();

  /**
   * Retrieves additional information of the request.
   *
   * @param key the {@link String} value key used to fetch information
   * @return the corresponding {@link Object} value if available, otherwise {@code null}
   */
  Object getAttribute(String key);

  /**
   * Puts additional information for the request.
   *
   * @param key   the {@link String} value key used to fetch information
   * @param value the {@link Object} value
   * @return the pointer of request
   */
  Request setAttribute(String key, Object value);

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
   * @return a {@link Session} that plays as a sender of the request
   */
  Session getSender();

  /**
   * Sets the sender of request.
   *
   * @param session a {@link Session} that plays as a sender of the request
   * @return the pointer of request
   */
  Request setSender(Session session);

  /**
   * Retrieves the priority of request.
   *
   * @return {@link RequestPriority} priority of request
   * @see RequestComparator
   */
  RequestPriority getPriority();

  /**
   * Sets priority for the request.
   *
   * @param priority {@link RequestPriority} priority of request
   * @return the pointer of request
   * @see RequestComparator
   */
  Request setPriority(RequestPriority priority);

  /**
   * Retrieves the creation timestamp in milliseconds.
   *
   * @return the creation timestamp in milliseconds ({@code long} value)
   */
  long getCreatedTimestamp();
}
