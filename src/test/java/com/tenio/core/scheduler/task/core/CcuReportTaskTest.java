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

package com.tenio.core.scheduler.task.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For CcuReportTask")
class CcuReportTaskTest {

  private EventManager eventManager;
  private PlayerManager playerManager;
  private CcuReportTask task;

  @BeforeEach
  void setUp() {
    eventManager = Mockito.mock(EventManager.class);
    playerManager = Mockito.mock(PlayerManager.class);
    task = CcuReportTask.newInstance(eventManager);
    task.setPlayerManager(playerManager);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(CcuReportTask.newInstance(eventManager));
  }

  @Test
  @DisplayName("Test player manager setter")
  void testSetPlayerManager() {
    task.setPlayerManager(playerManager);
    // no exception expected
  }

  @Test
  @DisplayName("Test scheduler is null before run")
  void testGetSchedulerBeforeRunIsNull() {
    CcuReportTask freshTask = CcuReportTask.newInstance(eventManager);
    freshTask.setPlayerManager(playerManager);
    assertNull(freshTask.getScheduler());
  }

  @Test
  @DisplayName("Test run initializes the scheduler")
  void testRunInitializesScheduler() {
    task.run();
    assertNotNull(task.getScheduler());
    task.shutdown();
  }

  @Test
  @DisplayName("Test shutdown after run completes without exception")
  void testShutdownAfterRun() {
    task.run();
    task.shutdown(); // should not throw
  }

  @Test
  @DisplayName("Test shutdown before run completes without exception")
  void testShutdownBeforeRun() {
    CcuReportTask freshTask = CcuReportTask.newInstance(eventManager);
    freshTask.shutdown(); // scheduledService is null, should not throw
  }

  @Test
  @DisplayName("Test setInterval updates interval without exception")
  void testSetInterval() {
    task.setInterval(15);
    // no exception expected
  }

  @Test
  @DisplayName("lambda body emits FETCHED_CCU_INFO event with player count")
  @SuppressWarnings("unchecked")
  void testLambdaBodyEmitsCcuEvent() {
    Mockito.when(playerManager.getPlayerCount()).thenReturn(5);
    ScheduledExecutorService mockService = Mockito.mock(ScheduledExecutorService.class);
    Mockito.when(mockService.scheduleAtFixedRate(
            Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
        .thenAnswer(inv -> {
          ((Runnable) inv.getArgument(0)).run();
          return Mockito.mock(ScheduledFuture.class);
        });

    try (MockedStatic<Executors> execMock = Mockito.mockStatic(Executors.class)) {
      execMock.when(() -> Executors.newSingleThreadScheduledExecutor(Mockito.any()))
          .thenReturn(mockService);
      task.run();
    }

    Mockito.verify(eventManager).emit(ServerEvent.FETCHED_CCU_INFO, 5);
  }

  @Test
  @DisplayName("shutdown handles InterruptedException from awaitTermination")
  void testShutdownHandlesInterruptedException() {
    task.run();
    Thread.currentThread().interrupt();
    try {
      task.shutdown();
    } finally {
      Thread.interrupted();
    }
  }
}
