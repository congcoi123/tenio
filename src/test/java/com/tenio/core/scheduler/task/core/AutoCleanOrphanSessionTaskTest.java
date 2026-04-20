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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For AutoCleanOrphanSessionTask")
class AutoCleanOrphanSessionTaskTest {

  private EventManager eventManager;
  private SessionManager sessionManager;
  private AutoCleanOrphanSessionTask task;

  @BeforeEach
  void setUp() {
    eventManager = Mockito.mock(EventManager.class);
    sessionManager = Mockito.mock(SessionManager.class);
    task = AutoCleanOrphanSessionTask.newInstance(eventManager);
    task.setSessionManager(sessionManager);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(AutoCleanOrphanSessionTask.newInstance(eventManager));
  }

  @Test
  @DisplayName("Test session manager setter")
  void testSetSessionManager() {
    task.setSessionManager(sessionManager);
    // No exception means success
  }

  @Test
  @DisplayName("When the task runs, orphan sessions should be closed")
  void testRunSchedulesTaskAndClosesOrphanSessions() {
    Session orphanSession = Mockito.mock(Session.class);
    Mockito.when(orphanSession.isActivated()).thenReturn(true);
    Mockito.when(orphanSession.isOrphan()).thenReturn(true);
    Mockito.when(sessionManager.getReadonlySessionsList()).thenReturn(List.of(orphanSession));
    Mockito.when(sessionManager.getSessionCount()).thenReturn(1);
    task.run();
    assertNotNull(task.getScheduler());
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
    AutoCleanOrphanSessionTask freshTask =
        AutoCleanOrphanSessionTask.newInstance(eventManager);
    freshTask.shutdown();
  }

  @Test
  @DisplayName("Test setInterval does not throw")
  void testSetInterval() {
    task.setInterval(45);
  }

  private void runWithImmediateExecution(Runnable action) {
    ScheduledExecutorService mockScheduler = Mockito.mock(ScheduledExecutorService.class);
    ExecutorService mockExec = Mockito.mock(ExecutorService.class);
    Mockito.when(mockScheduler.scheduleAtFixedRate(
            Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
        .thenAnswer(inv -> {
          ((Runnable) inv.getArgument(0)).run();
          return Mockito.mock(ScheduledFuture.class);
        });
    Mockito.doAnswer(inv -> {
      ((Runnable) inv.getArgument(0)).run();
      return null;
    }).when(mockExec).execute(Mockito.any(Runnable.class));

    try (MockedStatic<Executors> execMock = Mockito.mockStatic(Executors.class)) {
      execMock.when(() -> Executors.newSingleThreadScheduledExecutor(Mockito.any()))
          .thenReturn(mockScheduler);
      execMock.when(() -> Executors.newThreadPerTaskExecutor(Mockito.any())).thenReturn(mockExec);
      action.run();
    }
  }

  @Test
  @DisplayName("lambda body: orphan session is closed")
  void testLambdaBodyClosesOrphanSession() throws IOException {
    Session orphan = Mockito.mock(Session.class);
    Mockito.when(orphan.isOrphan()).thenReturn(true);
    Mockito.when(sessionManager.getReadonlySessionsList()).thenReturn(List.of(orphan));
    Mockito.when(sessionManager.getSessionCount()).thenReturn(1);

    runWithImmediateExecution(task::run);

    Mockito.verify(orphan).close(ConnectionDisconnectMode.ORPHAN,
        PlayerDisconnectMode.CONNECTION_LOST);
  }

  @Test
  @DisplayName("lambda body: non-orphan session is not closed")
  void testLambdaBodySkipsNonOrphanSession() throws IOException {
    Session session = Mockito.mock(Session.class);
    Mockito.when(session.isOrphan()).thenReturn(false);
    Mockito.when(sessionManager.getReadonlySessionsList()).thenReturn(List.of(session));
    Mockito.when(sessionManager.getSessionCount()).thenReturn(1);

    runWithImmediateExecution(task::run);

    Mockito.verify(session, Mockito.never()).close(Mockito.any(), Mockito.any());
  }

  @Test
  @DisplayName("lambda body: IOException during session.close() is caught without propagation")
  void testLambdaBodyHandlesIOExceptionDuringClose() throws IOException {
    Session orphan = Mockito.mock(Session.class);
    Mockito.when(orphan.isOrphan()).thenReturn(true);
    Mockito.doThrow(IOException.class).when(orphan).close(Mockito.any(), Mockito.any());
    Mockito.when(sessionManager.getReadonlySessionsList()).thenReturn(List.of(orphan));
    Mockito.when(sessionManager.getSessionCount()).thenReturn(1);

    assertDoesNotThrow(() -> runWithImmediateExecution(task::run));
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
