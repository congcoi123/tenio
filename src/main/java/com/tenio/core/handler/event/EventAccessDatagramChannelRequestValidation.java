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

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import java.util.Optional;

/**
 * When a player attempts to connect to UDP channel to send and receive messages.
 */
@FunctionalInterface
public interface EventAccessDatagramChannelRequestValidation<D extends DataCollection> {

  /**
   * When a player attempts to connect to UDP channel to send and receive messages.
   *
   * @param message an instance of {@link D} sent by client to identify its corresponding player
   *                on the server
   * @return an optional of {@link Player} is present if it qualifies identified conditions,
   * otherwise is empty
   * @see ServerEvent#ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION
   * @see EventAccessDatagramChannelRequestValidationResult
   * @see Optional
   */
  Optional<Player> handle(D message);
}
