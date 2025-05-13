/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.configuration.Configuration;
import com.tenio.core.entity.implement.DefaultPlayer;
import com.tenio.core.entity.implement.DefaultRoom;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.manager.implement.PlayerManagerImpl;
import com.tenio.core.entity.manager.implement.RoomManagerImpl;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import com.tenio.core.exception.AddedDuplicatedRoomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PlayerAndRoomTest {

  private EventManager eventManager;

  private PlayerManager playerManager;
  private RoomManager roomManager;

  private String testPlayerName;

  @BeforeEach
  public void initialize() throws Exception {
    var configuration = new Configuration();
    configuration.load("configuration.example.xml");

    eventManager = EventManager.newInstance();
    playerManager = PlayerManagerImpl.newInstance(eventManager);
    roomManager = RoomManagerImpl.newInstance(eventManager);
    testPlayerName = "kong";
  }

  @AfterEach
  public void tearDown() {
    eventManager.clear();
    playerManager.clear();
  }

  @Test
  public void addNewPlayerShouldReturnSuccess() {
    var player = DefaultPlayer.newInstance(testPlayerName);
    playerManager.addPlayer(player);
    var result = playerManager.getPlayerByIdentity(testPlayerName);

    assertEquals(player, result);
  }

  @Test
  public void addDuplicatedPlayerShouldCauseException() {
    assertThrows(AddedDuplicatedPlayerException.class, () -> {
      var player = DefaultPlayer.newInstance(testPlayerName);
      playerManager.addPlayer(player);
      playerManager.addPlayer(player);
    });
  }

  @Test
  public void checkContainPlayerShouldReturnSuccess() {
    var player = DefaultPlayer.newInstance(testPlayerName);
    playerManager.addPlayer(player);

    assertTrue(playerManager.containsPlayerIdentity(testPlayerName));
  }

  @Test
  public void countPlayersShouldReturnTrueValue() {
    for (int i = 0; i < 10; i++) {
      var player = DefaultPlayer.newInstance(testPlayerName + i);
      playerManager.addPlayer(player);
    }

    assertEquals(10, playerManager.getPlayerCount());
  }

  @Test
  public void removePlayerShouldReturnSuccess() {
    var player = DefaultPlayer.newInstance(testPlayerName);
    playerManager.addPlayer(player);
    playerManager.removePlayerByIdentity(testPlayerName);

    assertEquals(0, playerManager.getPlayerCount());
  }

  @Test
  @Disabled
  public void createNewRoomShouldReturnSuccess() {
    var room = DefaultRoom.newInstance();
    roomManager.addRoom(room);

    assertTrue(roomManager.containsRoomId(0));
  }

  @Test
  public void createDuplicatedRoomShouldCauseException() {
    assertThrows(AddedDuplicatedRoomException.class, () -> {
      var room = DefaultRoom.newInstance();
      room.configurePlayerManager(Mockito.mock(PlayerManager.class));
      roomManager.addRoom(room);
      roomManager.addRoom(room);
    });
  }
}
