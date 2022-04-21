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

import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.setting.InitialRoomSetting;

/**
 * Returns the result when the server attempts to create a new room.
 */
@FunctionalInterface
public interface EventRoomCreatedResult {

  /**
   * Retrieves the result when the server attempts to create a new room.
   *
   * @param room    a new creating {@link Room}
   * @param setting all settings in {@link InitialRoomSetting} needs for the room creation
   * @param result  the creation result presented by {@link RoomCreatedResult}. A
   *                new room is considered as it is created and is added to the management list
   *                when the result equals to success
   * @see RoomCreatedResult#SUCCESS
   */
  void handle(Room room, InitialRoomSetting setting, RoomCreatedResult result);
}
