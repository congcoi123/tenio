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

package com.tenio.core.schedule;

import com.tenio.common.task.TaskManager;
import com.tenio.common.task.implement.TaskManagerImpl;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.schedule.task.AutoDisconnectPlayerTask;
import com.tenio.core.schedule.task.AutoRemoveRoomTask;
import com.tenio.core.schedule.task.CcuReportTask;
import com.tenio.core.schedule.task.DeadlockScanTask;
import com.tenio.core.schedule.task.SystemMonitoringTask;
import com.tenio.core.schedule.task.TrafficCounterTask;

/**
 * The implementation for the schedule service.
 *
 * @see ScheduleService
 */
public final class ScheduleServiceImpl extends AbstractManager implements ScheduleService {

  private TaskManager taskManager;

  private AutoDisconnectPlayerTask autoDisconnectPlayerTask;
  private AutoRemoveRoomTask autoRemoveRoomTask;
  private CcuReportTask ccuReportTask;
  private DeadlockScanTask deadlockScanTask;
  private SystemMonitoringTask systemMonitoringTask;
  private TrafficCounterTask trafficCounterTask;

  private boolean initialized;

  private ScheduleServiceImpl(EventManager eventManager) {
    super(eventManager);

    autoDisconnectPlayerTask = AutoDisconnectPlayerTask.newInstance(this.eventManager);
    autoRemoveRoomTask = AutoRemoveRoomTask.newInstance(this.eventManager);
    ccuReportTask = CcuReportTask.newInstance(this.eventManager);
    deadlockScanTask = DeadlockScanTask.newInstance(this.eventManager);
    systemMonitoringTask = SystemMonitoringTask.newInstance(this.eventManager);
    trafficCounterTask = TrafficCounterTask.newInstance(this.eventManager);

    initialized = false;
  }

  public static ScheduleService newInstance(EventManager eventManager) {
    return new ScheduleServiceImpl(eventManager);
  }

  @Override
  public void initialize() {
    initializeTasks();
    initialized = true;
  }

  private void initializeTasks() {
    taskManager = TaskManagerImpl.newInstance();
  }

  @Override
  public void start() {
    info("START SERVICE", buildgen(getName(), " (", 1, ")"));

    // taskManager.create("auto-disconnect-player", autoDisconnectPlayerTask.run());
    // taskManager.create("auto-remove-room", autoRemoveRoomTask.run());
    taskManager.create("ccu-report", ccuReportTask.run());
    taskManager.create("dead-lock", deadlockScanTask.run());
    taskManager.create("system-monitoring", systemMonitoringTask.run());
    taskManager.create("traffic-counter", trafficCounterTask.run());
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }
    attemptToShutdown();
  }

  private void attemptToShutdown() {
    taskManager.clear();

    info("STOPPED SERVICE", buildgen(getName(), " (", 1, ")"));
    cleanup();
    info("DESTROYED SERVICE", buildgen(getName(), " (", 1, ")"));
  }

  private void cleanup() {
    autoDisconnectPlayerTask = null;
    autoRemoveRoomTask = null;
    ccuReportTask = null;
    deadlockScanTask = null;
    systemMonitoringTask = null;
    trafficCounterTask = null;
  }

  @Override
  public boolean isActivated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "schedule-tasks";
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRemovedRoomScanInterval(int interval) {
    autoRemoveRoomTask.setInterval(interval);
  }

  @Override
  public void setDisconnectedPlayerScanInterval(int interval) {
    autoDisconnectPlayerTask.setInterval(interval);
  }

  @Override
  public void setCcuReportInterval(int interval) {
    ccuReportTask.setInterval(interval);
  }

  @Override
  public void setDeadlockScanInterval(int interval) {
    deadlockScanTask.setInterval(interval);
  }

  @Override
  public void setTrafficCounterInterval(int interval) {
    trafficCounterTask.setInterval(interval);
  }

  @Override
  public void setSystemMonitoringInterval(int interval) {
    systemMonitoringTask.setInterval(interval);
  }

  @Override
  public void setPlayerManager(PlayerManager playerManager) {
    autoDisconnectPlayerTask.setPlayerManager(playerManager);
    ccuReportTask.setPlayerManager(playerManager);
  }

  @Override
  public void setRoomManager(RoomManager roomManager) {
    autoRemoveRoomTask.setRoomManager(roomManager);
  }

  @Override
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    trafficCounterTask.setNetworkReaderStatistic(networkReaderStatistic);
  }

  @Override
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    trafficCounterTask.setNetworkWriterStatistic(networkWriterStatistic);
  }
}
