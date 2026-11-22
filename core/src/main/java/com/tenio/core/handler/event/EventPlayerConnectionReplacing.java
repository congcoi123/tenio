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

import com.tenio.core.entity.Player;
import com.tenio.core.network.entity.outbound.Response;
import com.tenio.core.network.entity.session.Session;

/**
 * Invoked after an old player connection has been made unable to send or receive normal traffic,
 * but before a replacement connection is associated to that player.
 */
@FunctionalInterface
public interface EventPlayerConnectionReplacing<P extends Player> {

  /**
   * Sends any application-specific final message to the replaced connection. Use
   * {@link Response#writeThenClose()} when a message
   * must be delivered before the old connection is closed.
   *
   * @param player the player whose connection is being replaced
   * @param staleSession the prepared old session
   * @param replacementSession the new session that will be associated to the player
   */
  void onPlayerConnectionReplacing(P player, Session staleSession, Session replacementSession);
}
