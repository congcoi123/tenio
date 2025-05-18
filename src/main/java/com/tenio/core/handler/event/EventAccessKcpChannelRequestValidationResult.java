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

package com.tenio.core.handler.event;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import javax.annotation.Nullable;

/**
 * When the server responds to the request from client side which requires using the KCP channel.
 */
@FunctionalInterface
public interface EventAccessKcpChannelRequestValidationResult<P extends Player> {

  /**
   * When the server responds to the request from client side which requires using the KCP channel.
   *
   * @param player the instance of {@link Player} (nullable) which requires using UDP channel
   * @param result the responded {@link AccessDatagramChannelResult} from the server, if the result
   *               equals to {@link AccessDatagramChannelResult#PLAYER_NOT_FOUND} then the returned
   *               player is empty, otherwise it is present
   * @see ServerEvent#ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT
   * @see EventAccessKcpChannelRequestValidation
   */
  void handle(@Nullable P player, AccessDatagramChannelResult result);
}
