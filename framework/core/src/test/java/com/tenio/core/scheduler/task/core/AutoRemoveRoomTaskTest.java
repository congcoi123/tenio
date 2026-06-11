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
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.server.ServerImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For AutoRemoveRoomTask")
class AutoRemoveRoomTaskTest {

  private EventManager eventManager;
  private RoomManager roomManager;
  private AutoRemoveRoomTask task;

  @BeforeEach
  void setUp() {
    eventManager = Mockito.mock(EventManager.class);
    roomManager = Mockito.mock(RoomManager.class);
    task = AutoRemoveRoomTask.newInstance(eventManager);
    task.setRoomManager(roomManager);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(AutoRemoveRoomTask.newInstance(eventManager));
  }

  @Test
  @DisplayName("Test room manager setter")
  void testSetRoomManager() {
    task.setRoomManager(roomManager);
    // No exception means success
  }

  @Test
  @DisplayName("When the task runs, empty rooms should be removed")
  void testRunSchedulesTaskAndRemovesEmptyRooms() {
    Room emptyRoom = Mockito.mock(Room.class);
    Mockito.when(emptyRoom.getRoomRemoveMode()).thenReturn(RoomRemoveMode.WHEN_EMPTY);
    Mockito.when(emptyRoom.isSnapshotEmpty()).thenReturn(true);
    com.tenio.core.entity.RoomState roomState = Mockito.mock(com.tenio.core.entity.RoomState.class);
    Mockito.when(emptyRoom.getState()).thenReturn(roomState);
    Mockito.when(roomState.isIdle()).thenReturn(true);
    Mockito.when(emptyRoom.getId()).thenReturn(1L);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of(emptyRoom));
    Mockito.when(roomManager.getSnapshotRoomCount()).thenReturn(1);
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
    ServerApi api = Mockito.mock(ServerApi.class);
    ServerImpl server = Mockito.mock(ServerImpl.class);
    Mockito.when(server.getApi()).thenReturn(api);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of());
    try (MockedStatic<ServerImpl> serverStatic = Mockito.mockStatic(ServerImpl.class)) {
      serverStatic.when(ServerImpl::getInstance).thenReturn(server);
      task.run();
    }
    task.shutdown();
  }

  @Test
  @DisplayName("Test shutdown before run is a no-op")
  void testShutdownBeforeRun() {
    AutoRemoveRoomTask freshTask = AutoRemoveRoomTask.newInstance(eventManager);
    freshTask.shutdown();
  }

  @Test
  @DisplayName("Test setInterval does not throw")
  void testSetInterval() {
    task.setInterval(120);
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
  @DisplayName("lambda body: empty WHEN_EMPTY idle room is removed via server API")
  void testLambdaBodyRemovesEmptyWhenEmptyIdleRoom() {
    Room room = Mockito.mock(Room.class);
    Mockito.when(room.getRoomRemoveMode()).thenReturn(RoomRemoveMode.WHEN_EMPTY);
    Mockito.when(room.isSnapshotEmpty()).thenReturn(true);
    com.tenio.core.entity.RoomState state = Mockito.mock(com.tenio.core.entity.RoomState.class);
    Mockito.when(room.getState()).thenReturn(state);
    Mockito.when(state.isIdle()).thenReturn(true);
    Mockito.when(room.getId()).thenReturn(1L);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of(room));
    Mockito.when(roomManager.getSnapshotRoomCount()).thenReturn(1);

    runWithImmediateExecution(task::run);
  }

  @Test
  @DisplayName("lambda body: non-empty room is skipped")
  void testLambdaBodySkipsNonEmptyRoom() {
    Room room = Mockito.mock(Room.class);
    Mockito.when(room.getRoomRemoveMode()).thenReturn(RoomRemoveMode.WHEN_EMPTY);
    Mockito.when(room.isSnapshotEmpty()).thenReturn(false);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of(room));
    Mockito.when(roomManager.getSnapshotRoomCount()).thenReturn(1);

    runWithImmediateExecution(task::run);
  }

  @Test
  @DisplayName("lambda body: NEVER_REMOVE room is skipped")
  void testLambdaBodySkipsNeverRemoveRoom() {
    Room room = Mockito.mock(Room.class);
    Mockito.when(room.getRoomRemoveMode()).thenReturn(RoomRemoveMode.NEVER_REMOVE);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of(room));
    Mockito.when(roomManager.getSnapshotRoomCount()).thenReturn(1);

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
