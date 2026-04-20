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

package com.tenio.core.entity.manager.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.implement.DefaultRoom;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomCredentialValidatedStrategy;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedRoomException;
import com.tenio.core.exception.CreatedRoomException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For RoomManagerImpl")
class RoomManagerImplTest {

  private RoomManager roomManager;
  private EventManager eventManager;

  @BeforeEach
  void setUp() {
    eventManager = EventManager.newInstance();
    roomManager = RoomManagerImpl.newInstance(eventManager);
  }

  private Room newConfiguredRoom() {
    Room room = DefaultRoom.newInstance();
    room.configureRoomCredentialValidatedStrategy(new DefaultRoomCredentialValidatedStrategy());
    return room;
  }

  @Test
  @DisplayName("Test newInstance creates non-null manager")
  void testNewInstance() {
    assertNotNull(RoomManagerImpl.newInstance(EventManager.newInstance()));
  }

  @Test
  @DisplayName("Test getRoomCount is zero initially")
  void testGetRoomCountInitiallyZero() {
    assertEquals(0, roomManager.getRoomCount());
  }

  @Test
  @DisplayName("Test addRoom increases room count")
  void testAddRoomIncreasesCount() {
    Room room = DefaultRoom.newInstance();
    roomManager.addRoom(room);
    assertEquals(1, roomManager.getRoomCount());
  }

  @Test
  @DisplayName("Test addRoom duplicate throws AddedDuplicatedRoomException")
  void testAddRoomDuplicateThrows() {
    Room room = DefaultRoom.newInstance();
    roomManager.addRoom(room);
    assertThrows(AddedDuplicatedRoomException.class, () -> roomManager.addRoom(room));
  }

  @Test
  @DisplayName("Test containsRoomId returns true when room exists")
  void testContainsRoomIdTrue() {
    Room room = DefaultRoom.newInstance();
    roomManager.addRoom(room);
    assertTrue(roomManager.containsRoomId(room.getId()));
  }

  @Test
  @DisplayName("Test containsRoomId returns false when room does not exist")
  void testContainsRoomIdFalse() {
    assertFalse(roomManager.containsRoomId(99999L));
  }

  @Test
  @DisplayName("Test containsRoomName returns true when room with that name exists")
  void testContainsRoomNameTrue() {
    Room room = newConfiguredRoom();
    room.setName("MyRoom");
    roomManager.addRoom(room);
    assertTrue(roomManager.containsRoomName("MyRoom"));
  }

  @Test
  @DisplayName("Test containsRoomName returns false when no such name exists")
  void testContainsRoomNameFalse() {
    assertFalse(roomManager.containsRoomName("NonExistentRoom"));
  }

  @Test
  @DisplayName("Test getRoomById returns room when it exists")
  void testGetRoomByIdFound() {
    Room room = DefaultRoom.newInstance();
    roomManager.addRoom(room);
    assertEquals(room, roomManager.getRoomById(room.getId()));
  }

  @Test
  @DisplayName("Test getRoomById returns null when room does not exist")
  void testGetRoomByIdNotFound() {
    assertNull(roomManager.getRoomById(99999L));
  }

  @Test
  @DisplayName("Test getReadonlyRoomsList is empty initially")
  void testGetReadonlyRoomsListInitiallyEmpty() {
    assertTrue(roomManager.getReadonlyRoomsList().isEmpty());
  }

  @Test
  @DisplayName("Test getReadonlyRoomsList contains added rooms")
  void testGetReadonlyRoomsListContainsRooms() {
    Room room1 = DefaultRoom.newInstance();
    Room room2 = DefaultRoom.newInstance();
    roomManager.addRoom(room1);
    roomManager.addRoom(room2);
    assertEquals(2, roomManager.getReadonlyRoomsList().size());
  }

  @Test
  @DisplayName("Test getReadonlyRoomsListByName returns matching rooms")
  void testGetReadonlyRoomsListByName() {
    Room room = newConfiguredRoom();
    room.setName("SpecialRoom");
    roomManager.addRoom(room);
    List<Room> result = roomManager.getReadonlyRoomsListByName("SpecialRoom");
    assertEquals(1, result.size());
    assertEquals(room, result.get(0));
  }

  @Test
  @DisplayName("Test getReadonlyRoomsListByName returns empty for unknown name")
  void testGetReadonlyRoomsListByNameEmpty() {
    assertTrue(roomManager.getReadonlyRoomsListByName("Unknown").isEmpty());
  }

  @Test
  @DisplayName("Test computeRooms iterates all rooms")
  void testComputeRooms() {
    Room room = DefaultRoom.newInstance();
    roomManager.addRoom(room);
    AtomicInteger count = new AtomicInteger(0);
    roomManager.computeRooms(iterator -> {
      while (iterator.hasNext()) {
        iterator.next();
        count.incrementAndGet();
      }
    });
    assertEquals(1, count.get());
  }

  @Test
  @DisplayName("Test removeRoomById decreases room count")
  void testRemoveRoomById() {
    Room room = DefaultRoom.newInstance();
    roomManager.addRoom(room);
    assertEquals(1, roomManager.getRoomCount());
    roomManager.removeRoomById(room.getId());
    assertEquals(0, roomManager.getRoomCount());
  }

  @Test
  @DisplayName("Test configureMaxRooms limits room creation")
  void testConfigureMaxRooms() {
    roomManager.configureMaxRooms(1);
    Player owner = mock(Player.class);
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("Room1")
        .setMaxParticipants(10)
        .setMaxSpectators(5)
        .setActivated(true)
        .build();
    roomManager.createRoomWithOwner(setting, owner);
    assertThrows(CreatedRoomException.class, () -> roomManager.createRoomWithOwner(setting, owner));
  }

  @Test
  @DisplayName("Test createRoomWithOwner creates and adds a room")
  void testCreateRoomWithOwner() {
    Player owner = mock(Player.class);
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("TestRoom")
        .setPassword("pass")
        .setMaxParticipants(10)
        .setMaxSpectators(5)
        .setActivated(true)
        .build();
    Room room = roomManager.createRoomWithOwner(setting, owner);
    assertNotNull(room);
    assertEquals(1, roomManager.getRoomCount());
  }

  @Test
  @DisplayName("Test changeRoomName updates room name")
  void testChangeRoomName() {
    Room room = newConfiguredRoom();
    room.setName("OldName");
    roomManager.addRoom(room);
    roomManager.changeRoomName(room, "NewName");
    assertEquals("NewName", room.getName());
  }

  @Test
  @DisplayName("Test changeRoomPassword updates room password")
  void testChangeRoomPassword() {
    Room room = newConfiguredRoom();
    roomManager.addRoom(room);
    roomManager.changeRoomPassword(room, "newpass");
    assertEquals("newpass", room.getPassword());
  }

  @Test
  @DisplayName("Test changeRoomCapacity with valid capacity updates room")
  void testChangeRoomCapacityValid() {
    Room room = DefaultRoom.newInstance();
    room.setCapacity(5, 5);
    roomManager.addRoom(room);
    roomManager.changeRoomCapacity(room, 10, 10);
    assertEquals(10, room.getMaxParticipants());
    assertEquals(10, room.getMaxSpectators());
  }

  @Test
  @DisplayName("Test changeRoomCapacity with too-small participants throws")
  void testChangeRoomCapacityTooSmallParticipantsThrows() {
    Room room = DefaultRoom.newInstance();
    room.setCapacity(5, 5);
    roomManager.addRoom(room);
    assertThrows(IllegalArgumentException.class, () -> roomManager.changeRoomCapacity(room, 0, 10));
  }

  @Test
  @DisplayName("Test changeRoomCapacity with too-small spectators throws")
  void testChangeRoomCapacityTooSmallSpectatorsThrows() {
    Room room = DefaultRoom.newInstance();
    room.setCapacity(5, 5);
    roomManager.addRoom(room);
    assertThrows(IllegalArgumentException.class, () -> roomManager.changeRoomCapacity(room, 10, 0));
  }

  @Test
  @DisplayName("Test createRoom default method creates room without owner")
  void testCreateRoomDefaultMethodCreatesRoomWithoutOwner() {
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("DefaultRoom")
        .setMaxParticipants(4)
        .setMaxSpectators(2)
        .setActivated(true)
        .build();
    Room room = roomManager.createRoom(setting);
    assertNotNull(room);
    assertTrue(room.getOwner().isEmpty());
    assertEquals(1, roomManager.getRoomCount());
  }

  @Test
  @DisplayName("Test addRoomWithOwner throws when max rooms exceeded")
  void testAddRoomWithOwnerThrowsWhenMaxRoomsExceeded() {
    roomManager.configureMaxRooms(1);
    Player owner = mock(Player.class);
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("Room")
        .setMaxParticipants(4)
        .setMaxSpectators(2)
        .setActivated(true)
        .build();
    Room room1 = DefaultRoom.newInstance();
    roomManager.addRoomWithOwner(room1, setting, owner);
    Room room2 = DefaultRoom.newInstance();
    assertThrows(CreatedRoomException.class, () -> roomManager.addRoomWithOwner(room2, setting, owner));
  }

  @Test
  @DisplayName("Test addRoomWithOwner with non-null properties applies them to room")
  void testAddRoomWithOwnerWithProperties() {
    Player owner = mock(Player.class);
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("PropRoom")
        .setMaxParticipants(4)
        .setMaxSpectators(2)
        .setActivated(true)
        .setProperties(Map.of("key1", "val1", "key2", 42))
        .build();
    Room room = DefaultRoom.newInstance();
    roomManager.addRoomWithOwner(room, setting, owner);
    assertEquals("val1", room.getProperty("key1"));
    assertEquals(42, room.getProperty("key2"));
  }

  @Test
  @DisplayName("Test createRoomWithOwner with non-null properties applies them to room")
  void testCreateRoomWithOwnerWithProperties() {
    Player owner = mock(Player.class);
    InitialRoomSetting setting = InitialRoomSetting.Builder.newInstance()
        .setName("PropRoom2")
        .setMaxParticipants(4)
        .setMaxSpectators(2)
        .setActivated(true)
        .setProperties(Map.of("propKey", "propVal"))
        .build();
    Room room = roomManager.createRoomWithOwner(setting, owner);
    assertNotNull(room);
    assertEquals("propVal", room.getProperty("propKey"));
  }

  @Test
  @DisplayName("clear() default method throws UnsupportedOperationException")
  void testClearDefaultMethodThrowsUnsupportedOperation() {
    assertThrows(UnsupportedOperationException.class, roomManager::clear);
  }
}
