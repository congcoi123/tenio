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
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.monitoring.system.SystemMonitoring;
import com.tenio.core.schedule.task.AbstractSystemTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * To retrieve the current system information in period time. You can configure
 * this time in your own configurations, see {@link CoreConfiguration}
 */
public final class SystemMonitoringTask extends AbstractSystemTask {

  private final SystemMonitoring systemMonitoring;

  private SystemMonitoringTask(EventManager eventManager) {
    super(eventManager);

    systemMonitoring = SystemMonitoring.newInstance();
  }

  /**
   * Creates a new task instance.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return a new instance of {@link SystemMonitoringTask}
   */
  public static SystemMonitoringTask newInstance(EventManager eventManager) {
    return new SystemMonitoringTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    var threadFactoryTask =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("system-monitoring-task-%d")
            .build();
    return Executors.newSingleThreadScheduledExecutor(threadFactoryTask).scheduleAtFixedRate(
        () -> eventManager.emit(ServerEvent.SYSTEM_MONITORING, systemMonitoring.getCpuUsage(),
            systemMonitoring.getTotalMemory(), systemMonitoring.getUsedMemory(),
            systemMonitoring.getFreeMemory(),
            systemMonitoring.countRunningThreads()),
        initialDelay, interval, TimeUnit.SECONDS);
  }
}
