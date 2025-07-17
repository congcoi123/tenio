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

package com.tenio.core.entity.define.mode;

/**
 * All modes associated with the player leaving room phase.
 */
public enum PlayerLeaveRoomMode {

  /**
   * When the player's session is closed.
   *
   * @since 0.6.6
   */
  SESSION_CLOSED,
  /**
   * When the reconnection happens.
   *
   * @since 0.6.6
   */
  RECONNECTION,
  /**
   * When a player changes its current room.
   */
  CHANGE_ROOM,
  /**
   * When a player falls in IDLE state then is removed from the server.
   */
  IDLE,
  /**
   * When a player is forced to remove from the server.
   */
  KICK,
  /**
   * When a player logged out from the server.
   */
  LOG_OUT,
  /**
   * When the player's room is removed.
   */
  ROOM_REMOVED,
  /**
   * For other reasons.
   */
  UNKNOWN;

  @Override
  public String toString() {
    return this.name();
  }
}
