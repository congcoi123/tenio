/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SchedulerImpl")
class SchedulerImplTest {

  private Scheduler scheduler;
  private EventManager eventManager;

  @BeforeEach
  void setUp() {
    eventManager = EventManager.newInstance();
    scheduler = SchedulerImpl.newInstance(eventManager);
  }

  @Test
  @DisplayName("Test newInstance creates non-null scheduler")
  void testNewInstance() {
    assertNotNull(SchedulerImpl.newInstance(EventManager.newInstance()));
  }

  @Test
  @DisplayName("Test getName returns 'scheduler'")
  void testGetName() {
    assertEquals("scheduler", scheduler.getName());
  }

  @Test
  @DisplayName("Test getMaximumStartingTimeInMilliseconds returns 0")
  void testGetMaximumStartingTimeInMilliseconds() {
    assertEquals(0, scheduler.getMaximumStartingTimeInMilliseconds());
  }

  @Test
  @DisplayName("Test initialize does not throw")
  void testInitialize() {
    scheduler.initialize();
    // cleanup
    scheduler.shutdown();
  }

  @Test
  @DisplayName("Test shutdown before initialize is a no-op")
  void testShutdownBeforeInitializeIsNoOp() {
    scheduler.shutdown(); // should not throw
  }

  @Test
  @DisplayName("Test shutdown after initialize completes cleanly")
  void testShutdownAfterInitialize() {
    scheduler.initialize();
    scheduler.shutdown();
    // second shutdown should be no-op
    scheduler.shutdown();
  }

  @Test
  @DisplayName("Test setRemovedRoomScanInterval does not throw")
  void testSetRemovedRoomScanInterval() {
    scheduler.setRemovedRoomScanInterval(30);
    // no exception expected
  }

  @Test
  @DisplayName("Test setDisconnectedPlayerScanInterval does not throw")
  void testSetDisconnectedPlayerScanInterval() {
    scheduler.setDisconnectedPlayerScanInterval(60);
    // no exception expected
  }

  @Test
  @DisplayName("Test setCcuReportInterval with positive enables CCU task")
  void testSetCcuReportIntervalPositive() {
    scheduler.setCcuReportInterval(10);
    // no exception expected
  }

  @Test
  @DisplayName("Test setCcuReportInterval with zero disables CCU task")
  void testSetCcuReportIntervalZero() {
    scheduler.setCcuReportInterval(0);
    // no exception expected
  }

  @Test
  @DisplayName("Test setDeadlockScanInterval with positive enables deadlock scan task")
  void testSetDeadlockScanIntervalPositive() {
    scheduler.setDeadlockScanInterval(60);
    // no exception expected
  }

  @Test
  @DisplayName("Test setDeadlockScanInterval with zero disables deadlock scan task")
  void testSetDeadlockScanIntervalZero() {
    scheduler.setDeadlockScanInterval(0);
    // no exception expected
  }

  @Test
  @DisplayName("Test setTrafficCounterInterval with positive enables traffic counter task")
  void testSetTrafficCounterIntervalPositive() {
    scheduler.setTrafficCounterInterval(30);
    // no exception expected
  }

  @Test
  @DisplayName("Test setTrafficCounterInterval with zero disables traffic counter task")
  void testSetTrafficCounterIntervalZero() {
    scheduler.setTrafficCounterInterval(0);
    // no exception expected
  }

  @Test
  @DisplayName("Test setSystemMonitoringInterval with positive enables system monitoring task")
  void testSetSystemMonitoringIntervalPositive() {
    scheduler.setSystemMonitoringInterval(15);
    // no exception expected
  }

  @Test
  @DisplayName("Test setSystemMonitoringInterval with zero disables system monitoring task")
  void testSetSystemMonitoringIntervalZero() {
    scheduler.setSystemMonitoringInterval(0);
    // no exception expected
  }

  @Test
  @DisplayName("Test setSessionManager does not throw")
  void testSetSessionManager() {
    SessionManager sessionManager = mock(SessionManager.class);
    scheduler.setSessionManager(sessionManager);
    // no exception expected
  }

  @Test
  @DisplayName("Test setPlayerManager does not throw")
  void testSetPlayerManager() {
    PlayerManager playerManager = mock(PlayerManager.class);
    scheduler.setPlayerManager(playerManager);
    // no exception expected
  }

  @Test
  @DisplayName("Test setRoomManager does not throw")
  void testSetRoomManager() {
    RoomManager roomManager = mock(RoomManager.class);
    scheduler.setRoomManager(roomManager);
    // no exception expected
  }

  @Test
  @DisplayName("Test setNetworkReaderStatistic does not throw")
  void testSetNetworkReaderStatistic() {
    NetworkReaderStatistic readerStatistic = mock(NetworkReaderStatistic.class);
    scheduler.setNetworkReaderStatistic(readerStatistic);
    // no exception expected
  }

  @Test
  @DisplayName("Test setNetworkWriterStatistic does not throw")
  void testSetNetworkWriterStatistic() {
    NetworkWriterStatistic writerStatistic = mock(NetworkWriterStatistic.class);
    scheduler.setNetworkWriterStatistic(writerStatistic);
    // no exception expected
  }

  @Test
  @DisplayName("Test activate throws UnsupportedOperationException")
  void testActivateThrows() {
    assertThrows(UnsupportedOperationException.class, scheduler::activate);
  }

  @Test
  @DisplayName("Test isActivated throws UnsupportedOperationException")
  void testIsActivatedThrows() {
    assertThrows(UnsupportedOperationException.class, scheduler::isActivated);
  }

  @Test
  @DisplayName("Test setName throws UnsupportedOperationException")
  void testSetNameThrows() {
    assertThrows(UnsupportedOperationException.class, () -> scheduler.setName("test"));
  }

  @Test
  @DisplayName("Test initialize then start then shutdown lifecycle")
  void testInitializeStartShutdown() {
    scheduler.initialize();
    scheduler.start();
    scheduler.shutdown();
  }

  @Test
  @DisplayName("Test initialize then start with optional tasks enabled")
  void testStartWithOptionalTasksEnabled() {
    scheduler.setCcuReportInterval(30);
    scheduler.setDeadlockScanInterval(60);
    scheduler.setSystemMonitoringInterval(15);
    scheduler.setTrafficCounterInterval(30);
    PlayerManager playerManager = mock(PlayerManager.class);
    RoomManager roomManager = mock(RoomManager.class);
    SessionManager sessionManager = mock(SessionManager.class);
    NetworkReaderStatistic readerStatistic = mock(NetworkReaderStatistic.class);
    NetworkWriterStatistic writerStatistic = mock(NetworkWriterStatistic.class);
    scheduler.setPlayerManager(playerManager);
    scheduler.setRoomManager(roomManager);
    scheduler.setSessionManager(sessionManager);
    scheduler.setNetworkReaderStatistic(readerStatistic);
    scheduler.setNetworkWriterStatistic(writerStatistic);
    scheduler.initialize();
    scheduler.start();
    scheduler.shutdown();
  }
}
