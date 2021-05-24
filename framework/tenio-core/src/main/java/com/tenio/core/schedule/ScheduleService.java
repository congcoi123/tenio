package com.tenio.core.schedule;

import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.service.Service;

public interface ScheduleService extends Service {

	void setRemovedRoomScanInterval(int interval);

	void setDisconnectedPlayerScanInterval(int interval);

	void setCcuScanInterval(int interval);

	void setDeadlockScanInterval(int interval);

	void setTrafficCounterInterval(int interval);

	void setSystemMonitoringInterval(int interval);

	void setPlayerManager(PlayerManager playerManager);

	void setRoomManager(RoomManager roomManager);

}
