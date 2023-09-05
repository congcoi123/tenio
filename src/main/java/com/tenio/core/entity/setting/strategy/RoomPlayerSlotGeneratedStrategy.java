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

package com.tenio.core.entity.setting.strategy;

import com.tenio.core.entity.Room;

/**
 * The strategy is for accomplish a room's slot of a participant.
 */
public interface RoomPlayerSlotGeneratedStrategy {

  /**
   * Initializes default data if needed.
   */
  void initialize();

  /**
   * Retrieves a free slot available for a participant in the room.
   *
   * @return a position ({@code integer} value) of slot if available, otherwise
   * {@code -1} should be used
   */
  int getFreePlayerSlotInRoom();

  /**
   * Frees a participant's slot when the player left the room.
   *
   * @param slot the player's slot position ({@code integer} value) before it leave the room
   */
  void freeSlotWhenPlayerLeft(int slot);

  /**
   * When a new player wants to join the room, and it tries to acquire a slot.
   *
   * @param slot an {@code integer} value of slot position
   * @throws IllegalArgumentException when the required slot value is invalid
   */
  void tryTakeSlot(int slot) throws IllegalArgumentException;

  /**
   * Retrieves the room which is applying this strategy.
   *
   * @return an instance of {@link Room}
   */
  Room getRoom();

  /**
   * Sets a room that applies the strategy.
   *
   * @param room an instance of {@link Room}
   */
  void setRoom(Room room);
}
