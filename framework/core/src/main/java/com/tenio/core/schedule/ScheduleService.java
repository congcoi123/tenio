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

package com.tenio.core.schedule;

import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.service.Service;

/**
 * All APIs designed to schedule tasks.
 */
public interface ScheduleService extends Service {

  /**
   * Sets interval time to ask the server frequently checks all rooms for finding out removable
   * ones and remove them from the management list.
   *
   * @param interval {@code integer} value, the interval time (seconds) to ask the sever
   *                 frequently checks all rooms
   * @see RoomRemoveMode
   */
  void setRemovedRoomScanInterval(int interval);

  /**
   * Sets interval time to ask the server frequently checks all players for finding out removable
   * ones and remove them from the management list.
   *
   * @param interval {@code integer} value, the interval time (seconds) to ask the sever
   *                 frequently checks all players
   * @see PlayerDisconnectMode
   */
  void setDisconnectedPlayerScanInterval(int interval);

  /**
   * Sets interval time to ask the server frequently provides CCU information.
   *
   * @param interval {@code integer} value, the interval time (seconds) to ask the sever
   *                 frequently provides CCU information
   */
  void setCcuReportInterval(int interval);

  /**
   * Sets interval time to ask the server frequently checks all deadlock occurring on it.
   *
   * @param interval {@code integer} value, the interval time (seconds) to ask the sever
   *                 frequently checks all deadlock occurring on it
   */
  void setDeadlockScanInterval(int interval);

  /**
   * Sets interval time to ask the server frequently provides traffic information.
   *
   * @param interval {@code integer} value, the interval time (seconds) to ask the sever
   *                 frequently provides traffic information
   */
  void setTrafficCounterInterval(int interval);

  /**
   * Sets interval time to ask the server frequently provides system information.
   *
   * @param interval {@code integer} value, the interval time (seconds) to ask the sever
   *                 frequently provides system information
   */
  void setSystemMonitoringInterval(int interval);

  /**
   * Sets an instance of session manager to the service.
   *
   * @param sessionManager an instance of {@link SessionManager}
   */
  void setSessionManager(SessionManager sessionManager);

  /**
   * Sets an instance of player manager to the service.
   *
   * @param playerManager an instance of {@link PlayerManager}
   */
  void setPlayerManager(PlayerManager playerManager);

  /**
   * Sets an instance of room manager to the service.
   *
   * @param roomManager an instance of {@link RoomManager}
   */
  void setRoomManager(RoomManager roomManager);

  /**
   * Sets an object for recording all activities regarding receiving data from client sides.
   *
   * @param networkReaderStatistic an instance of {@link NetworkReaderStatistic}
   */
  void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic);

  /**
   * Sets an object for recording all activities regarding sending data to client sides.
   *
   * @param networkWriterStatistic an instance of {@link NetworkWriterStatistic}
   */
  void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic);
}
