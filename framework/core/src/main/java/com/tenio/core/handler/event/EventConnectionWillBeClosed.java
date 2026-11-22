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

package com.tenio.core.handler.event;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.entity.session.Session;

/**
 * When a connection is about to be closed on the server.
 */
@FunctionalInterface
public interface EventConnectionWillBeClosed {

  /**
   * Called before the session is detached from its player and removed. This is emitted for every
   * associated session close, including retained-player disconnections.
   *
   * @param session the connection session that is about to be closed
   * @param mode the reason that the player's connection is being closed
   * @see ServerEvent#CONNECTION_WILL_BE_CLOSED
   */
  void onConnectionWillBeClosed(Session session, PlayerDisconnectMode mode);
}
