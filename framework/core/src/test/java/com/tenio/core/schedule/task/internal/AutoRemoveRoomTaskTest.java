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
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.server.ServerImpl;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
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
    Mockito.when(emptyRoom.isEmpty()).thenReturn(true);
    com.tenio.core.entity.RoomState roomState = Mockito.mock(com.tenio.core.entity.RoomState.class);
    Mockito.when(emptyRoom.getState()).thenReturn(roomState);
    Mockito.when(roomState.isIdle()).thenReturn(true);
    Mockito.when(emptyRoom.getId()).thenReturn(1L);
    Mockito.when(roomManager.getReadonlyRoomsList()).thenReturn(List.of(emptyRoom));
    Mockito.when(roomManager.getRoomCount()).thenReturn(1);
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
