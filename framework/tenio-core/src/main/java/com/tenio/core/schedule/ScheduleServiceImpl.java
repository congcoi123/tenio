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
package com.tenio.core.schedule;

import com.tenio.common.task.TaskManager;
import com.tenio.common.task.TaskManagerImpl;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.schedule.tasks.AutoDisconnectPlayerTask;
import com.tenio.core.schedule.tasks.AutoRemoveRoomTask;
import com.tenio.core.schedule.tasks.CcuReportTask;
import com.tenio.core.schedule.tasks.DeadlockScanTask;
import com.tenio.core.schedule.tasks.SystemMonitoringTask;
import com.tenio.core.schedule.tasks.TrafficCounterTask;

public final class ScheduleServiceImpl extends AbstractManager implements ScheduleService {

	private TaskManager __taskManager;

	private AutoDisconnectPlayerTask __autoDisconnectPlayerTask;
	private AutoRemoveRoomTask __autoRemoveRoomTask;
	private CcuReportTask __ccuReportTask;
	private DeadlockScanTask __deadlockScanTask;
	private SystemMonitoringTask __systemMonitoringTask;
	private TrafficCounterTask __trafficCounterTask;

	private boolean __initialized;

	public static ScheduleService newInstance(EventManager eventManager) {
		return new ScheduleServiceImpl(eventManager);
	}

	private ScheduleServiceImpl(EventManager eventManager) {
		super(eventManager);

		__autoDisconnectPlayerTask = AutoDisconnectPlayerTask.newInstance(__eventManager);
		__autoRemoveRoomTask = AutoRemoveRoomTask.newInstance(__eventManager);
		__ccuReportTask = CcuReportTask.newInstance(__eventManager);
		__deadlockScanTask = DeadlockScanTask.newInstance(__eventManager);
		__systemMonitoringTask = SystemMonitoringTask.newInstance(__eventManager);
		__trafficCounterTask = TrafficCounterTask.newInstance(__eventManager);

		__initialized = false;
	}

	@Override
	public void initialize() {
		__initializeTasks();
		__initialized = true;
	}

	private void __initializeTasks() {
		__taskManager = TaskManagerImpl.newInstance();
	}

	@Override
	public void start() {
		info("START SERVICE", buildgen(getName(), " (", 1, ")"));

		// __taskManager.create("auto-disconnect-player",
		// __autoDisconnectPlayerTask.run());
		// __taskManager.create("auto-remove-room", __autoRemoveRoomTask.run());
		__taskManager.create("ccu-report", __ccuReportTask.run());
		__taskManager.create("dead-lock", __deadlockScanTask.run());
		__taskManager.create("system-monitoring", __systemMonitoringTask.run());
		__taskManager.create("traffic-counter", __trafficCounterTask.run());
	}

	@Override
	public void shutdown() {
		if (!__initialized) {
			return;
		}
		__shutdown();
	}

	private void __shutdown() {
		__taskManager.clear();

		info("STOPPED SERVICE", buildgen(getName(), " (", 1, ")"));
		__cleanup();
		info("DESTROYED SERVICE", buildgen(getName(), " (", 1, ")"));
	}

	private void __cleanup() {
		__autoDisconnectPlayerTask = null;
		__autoRemoveRoomTask = null;
		__ccuReportTask = null;
		__deadlockScanTask = null;
		__systemMonitoringTask = null;
		__trafficCounterTask = null;
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
		__autoRemoveRoomTask.setInterval(interval);
	}

	@Override
	public void setDisconnectedPlayerScanInterval(int interval) {
		__autoDisconnectPlayerTask.setInterval(interval);
	}

	@Override
	public void setCcuReportInterval(int interval) {
		__ccuReportTask.setInterval(interval);
	}

	@Override
	public void setDeadlockScanInterval(int interval) {
		__deadlockScanTask.setInterval(interval);
	}

	@Override
	public void setTrafficCounterInterval(int interval) {
		__trafficCounterTask.setInterval(interval);
	}

	@Override
	public void setSystemMonitoringInterval(int interval) {
		__systemMonitoringTask.setInterval(interval);
	}

	@Override
	public void setPlayerManager(PlayerManager playerManager) {
		__autoDisconnectPlayerTask.setPlayerManager(playerManager);
		__ccuReportTask.setPlayerManager(playerManager);
	}

	@Override
	public void setRoomManager(RoomManager roomManager) {
		__autoRemoveRoomTask.setRoomManager(roomManager);
	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
		__trafficCounterTask.setNetworkReaderStatistic(networkReaderStatistic);
	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
		__trafficCounterTask.setNetworkWriterStatistic(networkWriterStatistic);
	}

}
