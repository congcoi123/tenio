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

public final class ScheduleServiceImpl extends AbstractManager implements ScheduleService {

	private final TaskManager __taskManager;
	
	private AutoDisconnectPlayerTask __autoDisconnectPlayerTask;
	private AutoRemoveRoomTask __autoRemoveRoomTask;
	private CcuReportTask __ccuReportTask;
	private DeadlockScanTask __deadlockScanTask;

	public static ScheduleService newInstance(EventManager eventManager) {
		return new ScheduleServiceImpl(eventManager);
	}

	private ScheduleServiceImpl(EventManager eventManager) {
		super(eventManager);

		__taskManager = TaskManagerImpl.newInstance();
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		info("START SERVICE", getName());

//		__taskManager.create(CoreConstant.KEY_SCHEDULE_TIME_OUT_SCAN,
//		(new AutoDisconnectPlayerTask(__eventManager, __playerApi,
//				configuration.getInt(CoreConfigurationType.IDLE_READER_TIME),
//				configuration.getInt(CoreConfigurationType.IDLE_WRITER_TIME),
//				configuration.getInt(CoreConfigurationType.TIMEOUT_SCAN_INTERVAL))).run());
//
//__taskManager.create(CoreConstant.KEY_SCHEDULE_EMPTY_ROOM_SCAN, (new AutoRemoveRoomTask(__roomApi,
//		configuration.getInt(CoreConfigurationType.EMPTY_ROOM_SCAN_INTERVAL))).run());
//
//__taskManager.create(CoreConstant.KEY_SCHEDULE_CCU_SCAN, (new CCUScanTask(__eventManager, __playerApi,
//		configuration.getInt(CoreConfigurationType.CCU_SCAN_INTERVAL))).run());
//
//__taskManager.create(CoreConstant.KEY_SCHEDULE_SYSTEM_MONITORING, (new SystemMonitoringTask(__eventManager,
//		configuration.getInt(CoreConfigurationType.SYSTEM_MONITORING_INTERVAL))).run());
//
//__taskManager.create(CoreConstant.KEY_SCHEDULE_DEADLOCK_SCAN,
//		(new DeadlockScanTask(configuration.getInt(CoreConfigurationType.DEADLOCK_SCAN_INTERVAL))).run());
	}

	@Override
	public void shutdown() {
		__taskManager.clear();

		info("STOPPED SERVICE", getName());
		__cleanup();
		info("DESTROYED SERVICE", getName());
	}

	private void __cleanup() {

	}

	@Override
	public boolean isActivated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "schedule-tasks";
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRemovedRoomScanInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisconnectedPlayerScanInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCcuReportInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDeadlockScanInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTrafficCounterInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSystemMonitoringInterval(int interval) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlayerManager(PlayerManager playerManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRoomManager(RoomManager roomManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {

	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {

	}

}
