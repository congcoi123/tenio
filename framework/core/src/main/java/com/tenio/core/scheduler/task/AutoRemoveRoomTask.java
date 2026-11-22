/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.scheduler.task;

import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.server.ServerImpl;
import java.util.Iterator;

/**
 * To remove the empty room (a room without any players) in period time. You can
 * configure this time in your own configurations, see {@link CoreConfiguration}
 */
public final class AutoRemoveRoomTask extends SystemLogger {

  private RoomManager roomManager;

  /** Runs one cleanup pass. */
  public void scan() {
    Iterator<Room> iterator = roomManager.getSnapshotRoomsList().listIterator();
    while (iterator.hasNext()) {
      Room room = iterator.next();
      if (room.getRoomRemoveMode() == RoomRemoveMode.WHEN_EMPTY && room.isSnapshotEmpty()
              && room.getState() != null && room.getState().isIdle()) {
        if (isDebugEnabled()) {
          debug("AUTO REMOVE ROOM", "Room ", room.getId(),
              " is going to be forced to remove by the cleaning task");
        }
        ServerImpl.getInstance().getApi().removeRoom(room, RoomRemoveMode.WHEN_EMPTY);
      }
    }
  }

  /**
   * Set the room manager.
   *
   * @param roomManager the room manager
   */
  public void setRoomManager(RoomManager roomManager) {
    this.roomManager = roomManager;
  }
}
