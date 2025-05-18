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

package com.tenio.core.schedule.task.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.schedule.task.AbstractSystemTask;
import com.tenio.core.server.ServerImpl;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * To remove the empty room (a room without any players) in period time. You can
 * configure this time in your own configurations, see {@link CoreConfiguration}
 */
public final class AutoRemoveRoomTask extends AbstractSystemTask {

  private RoomManager roomManager;

  private AutoRemoveRoomTask(EventManager eventManager) {
    super(eventManager);
  }

  /**
   * Creates a new task instance.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return a new instance of {@link AutoRemoveRoomTask}
   */
  public static AutoRemoveRoomTask newInstance(EventManager eventManager) {
    return new AutoRemoveRoomTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    var threadFactoryTask =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("auto-remove-room-task-%d")
            .build();
    var threadFactoryWorker =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("auto-remove-room-worker-%d").build();
    var executors = Executors.newCachedThreadPool(threadFactoryWorker);
    return Executors.newSingleThreadScheduledExecutor(threadFactoryTask).scheduleAtFixedRate(
        () -> {
          debug("AUTO REMOVE ROOM",
              "Checking empty rooms in ", roomManager.getRoomCount(), " entities");
          executors.execute(() -> {
            Iterator<Room> iterator = roomManager.getReadonlyRoomsList().listIterator();
            while (iterator.hasNext()) {
              Room room = iterator.next();
              if (room.getRoomRemoveMode() == RoomRemoveMode.WHEN_EMPTY && room.isEmpty() &&
                  room.getState().isIdle()) {
                debug("AUTO REMOVE ROOM", "Room ", room.getId(),
                    " is going to be forced to remove by the cleaning task");
                ServerImpl.getInstance().getApi().removeRoom(room, RoomRemoveMode.WHEN_EMPTY);
              }
            }
          });
        }, initialDelay, interval, TimeUnit.SECONDS);
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
