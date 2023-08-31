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

package com.tenio.core.entity.define.result;

/**
 * The results when a player tried to join a room.
 */
public enum PlayerJoinedRoomResult {

  /**
   * Success.
   */
  SUCCESS,
  /**
   * The player fails to join room cause the room is full.
   */
  ROOM_IS_FULL,
  /**
   * The player fails to join room cause it try to occupy an invalid slot in the room.
   */
  SLOT_UNAVAILABLE_IN_ROOM,
  /**
   * The player fails to join room cause there is a same player already in the room. It should
   * aso throw an exception.
   */
  DUPLICATED_PLAYER,
  /**
   * The player fails to join room cause the player or room is unavailable. It should aso throw
   * an exception.
   */
  PLAYER_OR_ROOM_UNAVAILABLE,
  /**
   * The player fails to join room because it is in another room, it must leave its current room
   * before joining a new one. It should aso throw an exception.
   */
  PLAYER_IS_IN_ANOTHER_ROOM,
  /**
   * Invalid password was provided.
   *
   * @since 0.5.0
   */
  INVALID_CREDENTIALS;

  @Override
  public String toString() {
    return this.name();
  }
}
