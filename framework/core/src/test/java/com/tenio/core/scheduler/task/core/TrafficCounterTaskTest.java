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

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For TrafficCounterTask")
class TrafficCounterTaskTest {

  private EventManager eventManager;
  private NetworkReaderStatistic readerStatistic;
  private NetworkWriterStatistic writerStatistic;
  private TrafficCounterTask task;

  @BeforeEach
  void setUp() {
    eventManager = Mockito.mock(EventManager.class);
    readerStatistic = Mockito.mock(NetworkReaderStatistic.class);
    writerStatistic = Mockito.mock(NetworkWriterStatistic.class);
    task = TrafficCounterTask.newInstance(eventManager);
    task.setNetworkReaderStatistic(readerStatistic);
    task.setNetworkWriterStatistic(writerStatistic);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(TrafficCounterTask.newInstance(eventManager));
  }

  @Test
  @DisplayName("Test network reader statistic setter")
  void testSetNetworkReaderStatistic() {
    task.setNetworkReaderStatistic(readerStatistic);
    // No exception means success
  }

  @Test
  @DisplayName("Test network writer statistic setter")
  void testSetNetworkWriterStatistic() {
    task.setNetworkWriterStatistic(writerStatistic);
    // No exception means success
  }

  @Test
  @DisplayName("When the task runs, bandwidth info events should be emitted")
  void testRunSchedulesTaskAndEmitsBandwidthInfoEvent() {
    task.run();
    assertNotNull(task.getScheduler());
    // The actual event emission is handled by the scheduled lambda, which can be verified in integration tests
  }

  @Test
  @DisplayName("Test shutdown after run completes without exception")
  void testShutdownAfterRun() {
    task.run();
    task.shutdown();
  }

  @Test
  @DisplayName("Test shutdown before run is a no-op")
  void testShutdownBeforeRun() {
    TrafficCounterTask freshTask = TrafficCounterTask.newInstance(eventManager);
    freshTask.shutdown();
  }

  @Test
  @DisplayName("Test setInterval does not throw")
  void testSetInterval() {
    task.setInterval(20);
  }

  @Test
  @DisplayName("lambda body: emits FETCHED_BANDWIDTH_INFO with statistic values")
  void testLambdaBodyEmitsBandwidthInfo() {
    java.util.concurrent.ScheduledExecutorService mockScheduler =
        Mockito.mock(java.util.concurrent.ScheduledExecutorService.class);
    Mockito.when(mockScheduler.scheduleAtFixedRate(
            Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
        .thenAnswer(inv -> {
          ((Runnable) inv.getArgument(0)).run();
          return Mockito.mock(java.util.concurrent.ScheduledFuture.class);
        });

    try (org.mockito.MockedStatic<java.util.concurrent.Executors> execMock =
        Mockito.mockStatic(java.util.concurrent.Executors.class)) {
      execMock.when(() -> java.util.concurrent.Executors.newSingleThreadScheduledExecutor(
          Mockito.any())).thenReturn(mockScheduler);
      task.run();
    }

    Mockito.verify(eventManager).emit(
        Mockito.eq(com.tenio.core.configuration.define.ServerEvent.FETCHED_BANDWIDTH_INFO),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @org.junit.jupiter.api.Test
  @org.junit.jupiter.api.DisplayName("shutdown handles InterruptedException from awaitTermination")
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
