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
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.schedule.task.AbstractSystemTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * To retrieve the CCU in period time. You can configure this time in your own
 * configurations, see {@link CoreConfiguration}
 */
public final class CcuReportTask extends AbstractSystemTask {

  private PlayerManager playerManager;

  private CcuReportTask(EventManager eventManager) {
    super(eventManager);
  }

  /**
   * Creates a new task instance.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return a new instance of {@link CcuReportTask}
   */
  public static CcuReportTask newInstance(EventManager eventManager) {
    return new CcuReportTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    var threadFactoryTask =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ccu-report-task-%d").build();
    return Executors.newSingleThreadScheduledExecutor(threadFactoryTask).scheduleAtFixedRate(
        () -> eventManager.emit(ServerEvent.FETCHED_CCU_INFO, playerManager.getPlayerCount()),
        initialDelay, interval, TimeUnit.SECONDS);
  }

  /**
   * Sets the player manager instance.
   *
   * @param playerManager an instance of {@link PlayerManager}
   */
  public void setPlayerManager(PlayerManager playerManager) {
    this.playerManager = playerManager;
  }
}
