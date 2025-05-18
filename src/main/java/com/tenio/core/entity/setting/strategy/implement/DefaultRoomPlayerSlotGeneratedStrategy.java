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

package com.tenio.core.entity.setting.strategy.implement;

import com.tenio.core.entity.Room;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;

/**
 * The default implementation for the strategy.
 */
public final class DefaultRoomPlayerSlotGeneratedStrategy
    implements RoomPlayerSlotGeneratedStrategy {

  private Room room;

  @Override
  public void initialize() {
  }

  @Override
  public int getFreePlayerSlotInRoom() {
    return 0;
  }

  @Override
  public void freeSlotWhenPlayerLeft(int slot) {
  }

  @Override
  public void tryTakeSlot(int slot) {
  }

  @Override
  public Room getRoom() {
    return room;
  }

  @Override
  public void setRoom(Room room) {
    this.room = room;
  }
}
