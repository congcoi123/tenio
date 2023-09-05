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

package com.tenio.common.task;

import java.util.concurrent.ScheduledFuture;

/**
 * This class uses Java scheduler ({@link ScheduledFuture}) to manage your
 * tasks. The scheduler is used to schedule a thread or task that executes at a
 * certain period of time or periodically at a fixed interval. It's useful when
 * you want to create a time counter before starting a match or send messages
 * periodically for one player.
 */
public interface TaskManager {

  /**
   * Creates a new task.
   *
   * @param id   the unique {@link String} id for management
   * @param task the running task, see {@link ScheduledFuture}
   */
  void create(String id, ScheduledFuture<?> task);

  /**
   * Kills or stops a running task.
   *
   * @param id the unique {@link String} id
   */
  void kill(String id);

  /**
   * Kills or stops all running tasks.
   */
  void clear();

  /**
   * Retrieves the remaining time of one task.
   *
   * @param id the unique {@link String} value for retrieving the desired task
   * @return the left time
   */
  int getRemainTime(String id);
}
