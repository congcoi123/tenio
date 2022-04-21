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

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.AttachedConnectionResult;
import java.util.Optional;

/**
 * When the server responds to the request from client side which requires using the UDP channel.
 */
@FunctionalInterface
public interface EventAttachedConnectionResult {

  /**
   * When the server responds to the request from client side which requires using the UDP channel.
   *
   * @param player the optional of {@link Player} which requires using UDP channel
   * @param result the responded {@link AttachedConnectionResult} from the server, if the result
   *               equals to {@link AttachedConnectionResult#PLAYER_NOT_FOUND} then the returned
   *               player is empty, otherwise it is present
   * @see ServerEvent#ATTACHED_CONNECTION_RESULT
   * @see EventAttachConnectionRequestValidation
   * @see Optional
   */
  void handle(Optional<Player> player, AttachedConnectionResult result);
}
