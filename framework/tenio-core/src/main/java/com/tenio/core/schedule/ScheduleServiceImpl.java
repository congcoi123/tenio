package com.tenio.core.schedule;

import com.tenio.common.task.TaskManager;
import com.tenio.common.task.TaskManagerImpl;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.events.EventManager;
import com.tenio.core.manager.AbstractManager;

public final class ScheduleServiceImpl extends AbstractManager implements ScheduleService {

	private final TaskManager __taskManager;

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
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void halt() {
		__taskManager.clear();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActivated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "schedule-service";
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
	public void setCcuScanInterval(int interval) {
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

}
