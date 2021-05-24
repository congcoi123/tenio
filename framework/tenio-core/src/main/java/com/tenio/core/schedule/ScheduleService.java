package com.tenio.core.schedule;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.schedule.tasks.AutoDisconnectPlayerTask;
import com.tenio.core.schedule.tasks.AutoRemoveRoomTask;
import com.tenio.core.schedule.tasks.CCUScanTask;
import com.tenio.core.schedule.tasks.DeadlockScanTask;
import com.tenio.core.schedule.tasks.SystemMonitoringTask;

public final class ScheduleService {

	
	private void __createAllSchedules(Configuration configuration) {
		__taskManager.create(CoreConstant.KEY_SCHEDULE_TIME_OUT_SCAN,
				(new AutoDisconnectPlayerTask(__eventManager, __playerApi,
						configuration.getInt(CoreConfigurationType.IDLE_READER_TIME),
						configuration.getInt(CoreConfigurationType.IDLE_WRITER_TIME),
						configuration.getInt(CoreConfigurationType.TIMEOUT_SCAN_INTERVAL))).run());

		__taskManager.create(CoreConstant.KEY_SCHEDULE_EMPTY_ROOM_SCAN, (new AutoRemoveRoomTask(__roomApi,
				configuration.getInt(CoreConfigurationType.EMPTY_ROOM_SCAN_INTERVAL))).run());

		__taskManager.create(CoreConstant.KEY_SCHEDULE_CCU_SCAN, (new CCUScanTask(__eventManager, __playerApi,
				configuration.getInt(CoreConfigurationType.CCU_SCAN_INTERVAL))).run());

		__taskManager.create(CoreConstant.KEY_SCHEDULE_SYSTEM_MONITORING, (new SystemMonitoringTask(__eventManager,
				configuration.getInt(CoreConfigurationType.SYSTEM_MONITORING_INTERVAL))).run());

		__taskManager.create(CoreConstant.KEY_SCHEDULE_DEADLOCK_SCAN,
				(new DeadlockScanTask(configuration.getInt(CoreConfigurationType.DEADLOCK_SCAN_INTERVAL))).run());
	}
	
}
