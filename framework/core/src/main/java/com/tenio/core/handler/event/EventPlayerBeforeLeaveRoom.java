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

package com.tenio.core.handler.event;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import java.util.Optional;

/**
 * The event occurs before a player leave its current room.
 */
@FunctionalInterface
public interface EventPlayerBeforeLeaveRoom<P extends Player, R extends Room> {

  /**
   * When a player is going to leave its current room.
   *
   * @param player       the leaving {@link Player}
   * @param optionalRoom the {@link Room} which the player is going to leave out, this object can be
   *                     {@code null} due to auto removing processes
   * @param mode         the leaving {@link PlayerLeaveRoomMode} applied for the player when it leaves
   *                     the room
   * @see EventPlayerAfterLeftRoom
   * @see Optional
   */
  void handle(P player, Optional<R> optionalRoom, PlayerLeaveRoomMode mode);
}
