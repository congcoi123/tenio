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

package com.tenio.core.extension.events;

import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.network.entity.session.Session;

/**
 * When a player sends a request to reconnect to the server.
 */
@FunctionalInterface
public interface EventPlayerReconnectRequestHandle {

  /**
   * When a player tried to reconnect to the server. The situation happens if the player gets in
   * an IDLE state for long time enough to be disconnected from the server automatically.
   *
   * @param session a new {@link Session} which the player is using to reconnect to the server
   * @param message a {@link ServerMessage} that the client side tries to send to the server to
   *                judge if the corresponding player could reconnect
   * @see EventPlayerReconnectedResult
   */
  void handle(Session session, ServerMessage message);
}
