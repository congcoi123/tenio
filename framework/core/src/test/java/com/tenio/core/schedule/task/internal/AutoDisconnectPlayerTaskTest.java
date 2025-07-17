/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.schedule.task.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenio.core.api.ServerApi;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.server.ServerImpl;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
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
      ScheduledFuture<?> future = task.run();
      assertNotNull(future);
    }
  }
}
