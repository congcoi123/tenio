/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.common.task;

import com.tenio.common.exception.RunningScheduledTaskException;
import com.tenio.common.logger.SystemLogger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class uses Java scheduler ({@link ScheduledFuture}) to manage your
 * tasks. The scheduler is used to schedule a thread or task that executes at a
 * certain period of time or periodically at a fixed interval. It's useful when
 * you want to create a time counter before starting a match or send messages
 * periodically for one player.
 *
 * @see TaskManager
 */
@ThreadSafe
public final class TaskManagerImpl extends SystemLogger implements TaskManager {

  /**
   * A list of tasks in the server.
   */
  private final Map<String, ScheduledFuture<?>> tasks;

  private TaskManagerImpl() {
    tasks = new ConcurrentHashMap<String, ScheduledFuture<?>>();
  }

  public static TaskManager newInstance() {
    return new TaskManagerImpl();
  }

  @Override
  public void create(String id, ScheduledFuture<?> task) {
    if (tasks.containsKey(id)) {
      try {
        if (!tasks.get(id).isDone() || !tasks.get(id).isCancelled()) {
          throw new RunningScheduledTaskException();
        }
      } catch (RunningScheduledTaskException e) {
        error(e, "task id: ", id);
        return;
      }
    }

    tasks.put(id, task);
    info("RUN TASK", buildgen(id, " >Time left> ", task.getDelay(TimeUnit.SECONDS), " seconds"));
  }

  @Override
  public void kill(String id) {
    if (tasks.containsKey(id)) {
      info("KILLED TASK", id);
      tasks.remove(id);
      var task = tasks.get(id);
      if (task != null && (!task.isDone() || !task.isCancelled())) {
        task.cancel(true);
      }
    }
  }

  @Override
  public void clear() {
    tasks.forEach((id, task) -> {
      info("KILLED TASK", id);
      if (task != null && (!task.isDone() || !task.isCancelled())) {
        task.cancel(true);
      }
    });
    tasks.clear();
  }

  @Override
  public int getRemainTime(String id) {
    var task = tasks.get(id);
    if (task != null) {
      return (int) task.getDelay(TimeUnit.SECONDS);
    }
    return -1;
  }
}
