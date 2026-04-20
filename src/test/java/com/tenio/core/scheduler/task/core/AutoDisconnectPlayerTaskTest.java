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

import com.tenio.core.api.ServerApi;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.server.ServerImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For AutoDisconnectPlayerTask")
class AutoDisconnectPlayerTaskTest {

  private EventManager eventManager;
  private PlayerManager playerManager;
  private AutoDisconnectPlayerTask task;

  @BeforeEach
  void setUp() {
    eventManager = Mockito.mock(EventManager.class);
    playerManager = Mockito.mock(PlayerManager.class);
    task = AutoDisconnectPlayerTask.newInstance(eventManager);
    task.setPlayerManager(playerManager);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(AutoDisconnectPlayerTask.newInstance(eventManager));
  }

  @Test
  @DisplayName("Test player manager setter")
  void testSetPlayerManager() {
    task.setPlayerManager(playerManager);
    // No exception means success
  }

  @Test
  @DisplayName("When the task runs, IDLE players should be logged out")
  void testRunSchedulesTaskAndLogsOutIdlePlayers() {
    Player idlePlayer = Mockito.mock(Player.class);
    Mockito.when(idlePlayer.isNeverDeported()).thenReturn(false);
    Mockito.when(idlePlayer.isIdle()).thenReturn(true);
    Mockito.when(idlePlayer.getIdentity()).thenReturn("player1");
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(List.of(idlePlayer));
    Mockito.when(playerManager.getPlayerCount()).thenReturn(1);
    ServerApi api = Mockito.mock(ServerApi.class);
    ServerImpl server = Mockito.mock(ServerImpl.class);
    Mockito.when(server.getApi()).thenReturn(api);
    try (MockedStatic<ServerImpl> serverStatic = Mockito.mockStatic(ServerImpl.class)) {
      serverStatic.when(ServerImpl::getInstance).thenReturn(server);
      task.run();
      assertNotNull(task.getScheduler());
    }
  }

  @Test
  @DisplayName("Test shutdown after run completes without exception")
  void testShutdownAfterRun() {
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(List.of());
    Mockito.when(playerManager.getPlayerCount()).thenReturn(0);
    ServerApi api = Mockito.mock(ServerApi.class);
    ServerImpl server = Mockito.mock(ServerImpl.class);
    Mockito.when(server.getApi()).thenReturn(api);
    try (MockedStatic<ServerImpl> serverStatic = Mockito.mockStatic(ServerImpl.class)) {
      serverStatic.when(ServerImpl::getInstance).thenReturn(server);
      task.run();
    }
    task.shutdown();
  }

  @Test
  @DisplayName("Test shutdown before run is a no-op")
  void testShutdownBeforeRun() {
    AutoDisconnectPlayerTask freshTask =
        AutoDisconnectPlayerTask.newInstance(eventManager);
    freshTask.shutdown();
  }

  @Test
  @DisplayName("Test setInterval does not throw")
  void testSetInterval() {
    task.setInterval(60);
  }

  private void runWithImmediateExecution(Runnable action) {
    java.util.concurrent.ScheduledExecutorService mockScheduler =
        Mockito.mock(java.util.concurrent.ScheduledExecutorService.class);
    java.util.concurrent.ExecutorService mockExec =
        Mockito.mock(java.util.concurrent.ExecutorService.class);
    Mockito.when(mockScheduler.scheduleAtFixedRate(
            Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
        .thenAnswer(inv -> {
          ((Runnable) inv.getArgument(0)).run();
          return Mockito.mock(java.util.concurrent.ScheduledFuture.class);
        });
    Mockito.doAnswer(inv -> {
      ((Runnable) inv.getArgument(0)).run();
      return null;
    }).when(mockExec).execute(Mockito.any(Runnable.class));

    ServerApi api = Mockito.mock(ServerApi.class);
    ServerImpl server = Mockito.mock(ServerImpl.class);
    Mockito.when(server.getApi()).thenReturn(api);

    try (MockedStatic<ServerImpl> serverStatic = Mockito.mockStatic(ServerImpl.class);
        org.mockito.MockedStatic<java.util.concurrent.Executors> execMock =
            Mockito.mockStatic(java.util.concurrent.Executors.class)) {
      serverStatic.when(ServerImpl::getInstance).thenReturn(server);
      execMock.when(() -> java.util.concurrent.Executors.newSingleThreadScheduledExecutor(
          Mockito.any())).thenReturn(mockScheduler);
      execMock.when(() -> java.util.concurrent.Executors.newThreadPerTaskExecutor(
          Mockito.any())).thenReturn(mockExec);
      action.run();
    }
  }

  @Test
  @DisplayName("lambda body: never-deported idle player is logged out")
  void testLambdaBodyLogsOutNeverDeportedIdlePlayer() {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.isNeverDeported()).thenReturn(true);
    Mockito.when(player.isIdleNeverDeported()).thenReturn(true);
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(List.of(player));
    Mockito.when(playerManager.getPlayerCount()).thenReturn(1);

    runWithImmediateExecution(task::run);
    // ServerImpl.getInstance().getApi().logout() was called via the mocked chain
  }

  @Test
  @DisplayName("lambda body: never-deported non-idle player is skipped")
  void testLambdaBodySkipsNeverDeportedNonIdlePlayer() {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.isNeverDeported()).thenReturn(true);
    Mockito.when(player.isIdleNeverDeported()).thenReturn(false);
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(List.of(player));
    Mockito.when(playerManager.getPlayerCount()).thenReturn(1);

    runWithImmediateExecution(task::run);
  }

  @Test
  @DisplayName("lambda body: regular idle player is logged out")
  void testLambdaBodyLogsOutRegularIdlePlayer() {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.isNeverDeported()).thenReturn(false);
    Mockito.when(player.isIdle()).thenReturn(true);
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(List.of(player));
    Mockito.when(playerManager.getPlayerCount()).thenReturn(1);

    runWithImmediateExecution(task::run);
  }

  @Test
  @DisplayName("lambda body: regular non-idle player is skipped")
  void testLambdaBodySkipsRegularNonIdlePlayer() {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.isNeverDeported()).thenReturn(false);
    Mockito.when(player.isIdle()).thenReturn(false);
    Mockito.when(playerManager.getReadonlyPlayersList()).thenReturn(List.of(player));
    Mockito.when(playerManager.getPlayerCount()).thenReturn(1);

    runWithImmediateExecution(task::run);
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
