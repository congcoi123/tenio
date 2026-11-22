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

package com.tenio.core.scheduler.task;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenio.core.api.ServerApi;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.server.ServerImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For AutoRemoveRoomTask")
class AutoRemoveRoomTaskTest {

  private RoomManager roomManager;
  private AutoRemoveRoomTask task;

  @BeforeEach
  void setUp() {
    roomManager = Mockito.mock(RoomManager.class);
    task = new AutoRemoveRoomTask();
    task.setRoomManager(roomManager);
  }

  @Test
  @DisplayName("Test creating a new instance")
  void testNewInstance() {
    assertNotNull(new AutoRemoveRoomTask());
  }

  @Test
  @DisplayName("Test room manager setter")
  void testSetRoomManager() {
    task.setRoomManager(roomManager);
    // No exception means success
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

    Mockito.verify(scan()).removeRoom(room, RoomRemoveMode.WHEN_EMPTY);
  }

  @Test
  @DisplayName("lambda body: non-empty room is skipped")
  void testLambdaBodySkipsNonEmptyRoom() {
    Room room = Mockito.mock(Room.class);
    Mockito.when(room.getRoomRemoveMode()).thenReturn(RoomRemoveMode.WHEN_EMPTY);
    Mockito.when(room.isSnapshotEmpty()).thenReturn(false);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of(room));
    Mockito.when(roomManager.getSnapshotRoomCount()).thenReturn(1);

    Mockito.verifyNoInteractions(scan());
  }

  @Test
  @DisplayName("lambda body: NEVER_REMOVE room is skipped")
  void testLambdaBodySkipsNeverRemoveRoom() {
    Room room = Mockito.mock(Room.class);
    Mockito.when(room.getRoomRemoveMode()).thenReturn(RoomRemoveMode.NEVER_REMOVE);
    Mockito.when(roomManager.getSnapshotRoomsList()).thenReturn(List.of(room));
    Mockito.when(roomManager.getSnapshotRoomCount()).thenReturn(1);

    Mockito.verifyNoInteractions(scan());
  }

  private ServerApi scan() {
    var server = Mockito.mock(ServerImpl.class);
    var api = Mockito.mock(ServerApi.class);
    Mockito.when(server.getApi()).thenReturn(api);
    try (var servers = Mockito.mockStatic(ServerImpl.class)) {
      servers.when(ServerImpl::getInstance).thenReturn(server);
      task.scan();
      return api;
    }
  }
}
