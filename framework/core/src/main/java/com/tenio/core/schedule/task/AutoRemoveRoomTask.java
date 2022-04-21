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

package com.tenio.core.schedule.task;

import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import java.util.concurrent.ScheduledFuture;

/**
 * To remove the empty room (a room without any players) in period time. You can
 * configure this time in your own configurations, see {@link CoreConfiguration}
 */
public final class AutoRemoveRoomTask extends AbstractTask {

  private AutoRemoveRoomTask(EventManager eventManager) {
    super(eventManager);
  }

  public static AutoRemoveRoomTask newInstance(EventManager eventManager) {
    return new AutoRemoveRoomTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    return null;
  }

  /**
   * Set the room manager.
   *
   * @param roomManager the room manager
   */
  public void setRoomManager(RoomManager roomManager) {
  }
}
