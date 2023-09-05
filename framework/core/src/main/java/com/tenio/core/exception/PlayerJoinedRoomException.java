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

package com.tenio.core.exception;

import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import java.io.Serial;

/**
 * Something went wrong when a player tries to join a room.
 */
public final class PlayerJoinedRoomException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 3173662815856707842L;

  /**
   * The player-join-room result.
   */
  private final PlayerJoinedRoomResult result;

  /**
   * Creates a new exception.
   *
   * @param message a warning {@link String} message
   * @param result  a {@link PlayerJoinedRoomResult} singleton value indicates the result when
   *                player tries to get in a room
   */
  public PlayerJoinedRoomException(String message, PlayerJoinedRoomResult result) {
    super(message);
    this.result = result;
  }

  /**
   * Retrieves a result when a player tried to get in a room.
   *
   * @return a {@link PlayerJoinedRoomResult} singleton value indicates the result when
   * player tries to get in a room
   */
  public PlayerJoinedRoomResult getResult() {
    return result;
  }
}
