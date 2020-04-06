/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.tenio.api.PlayerApi;
import com.tenio.api.RoomApi;
import com.tenio.configuration.constant.ErrorMsg;
import com.tenio.entities.manager.IPlayerManager;
import com.tenio.entities.manager.IRoomManager;
import com.tenio.entities.manager.PlayerManager;
import com.tenio.entities.manager.RoomManager;
import com.tenio.exception.DuplicatedPlayerException;
import com.tenio.exception.DuplicatedRoomException;
import com.tenio.exception.NullPlayerNameException;
import com.tenio.models.PlayerModel;
import com.tenio.models.RoomModel;

/**
 * 
 * @author kong
 * 
 */
@TestMethodOrder(OrderAnnotation.class)
public final class PlayerRoomTest {

	private static IPlayerManager __playerManager;
	private static IRoomManager __roomManager;
	private static PlayerApi __playerApi;
	private static RoomApi __roomApi;

	private static String __testPlayerName;
	private static String __testRoomId;

	@BeforeAll
	public static void initializeAll() {
		__playerManager = new PlayerManager();
		__roomManager = new RoomManager();
		__playerApi = new PlayerApi(__playerManager, __roomManager);
		__roomApi = new RoomApi(__roomManager);
		__testPlayerName = "kong";
		__testRoomId = UUID.randomUUID().toString();
	}

	@AfterAll
	public static void tearDownAll() {
		__playerManager.clear();
		__roomManager.clear();
	}

	@Test
	@Order(1)
	public void addNewPlayerShouldReturnSuccess() {
		var player = new PlayerModel(__testPlayerName);
		__playerApi.login(player);
		var result = __playerApi.get(__testPlayerName);
		assertEquals(player, result);
	}

	@Test
	@Order(2)
	public void addDupplicatedPlayerShouldCauseException() {
		assertThrows(DuplicatedPlayerException.class, () -> {
			var player = new PlayerModel(__testPlayerName);
			__playerApi.login(player);
		});
	}

	@Test
	@Order(3)
	public void addNullPlayerNameShouldCauseException() {
		assertThrows(NullPlayerNameException.class, () -> {
			var player = new PlayerModel(null);
			__playerApi.login(player, null);
		});
	}

	@Test
	@Order(4)
	public void checkContainPlayerShouldReturnSuccess() {
		assertTrue(__playerApi.contain(__testPlayerName));
	}

	@Test
	@Order(5)
	public void countPlayersShouldReturnTrueValue() {
		for (int i = 0; i < 10; i++) {
			var player = new PlayerModel(UUID.randomUUID().toString());
			__playerApi.login(player);
		}
		assertEquals(11, __playerApi.count());
	}

	@Test
	@Order(6)
	public void countRealPlayersShouldReturnTrueValue() {
		assertEquals(0, __playerApi.countPlayers());
	}

	@Test
	@Order(7)
	public void removePlayerShouldReturnSuccess() {
		__playerApi.logOut(__testPlayerName);
		assertEquals(10, __playerApi.count());
	}

	@Test
	@Order(8)
	public void createNewRoomShouldReturnSuccess() {
		var room = new RoomModel(__testRoomId, "Test Room", 3);
		__roomApi.add(room);
		assertTrue(__roomApi.contain(__testRoomId));
	}

	@Test
	@Order(9)
	public void createDuplicatedRoomShouldCauseException() {
		assertThrows(DuplicatedRoomException.class, () -> {
			var room = new RoomModel(__testRoomId, "Test Room Fake", 3);
			__roomApi.add(room);
		});
	}

	@Test
	@Order(10)
	public void playerJoinRoomShouldReturnSuccess() {
		var player = new PlayerModel(__testPlayerName);
		__playerApi.login(player);
		assertEquals(null, __playerApi.playerJoinRoom(__roomApi.get(__testRoomId), __playerApi.get(__testPlayerName)));
	}

	@Test
	@Order(11)
	public void addDuplicatedPlayerToRoomShouldReturnErrorMessage() {
		assertEquals(ErrorMsg.PLAYER_WAS_IN_ROOM,
				__playerApi.playerJoinRoom(__roomApi.get(__testRoomId), __playerApi.get(__testPlayerName)));
	}

	@Test
	@Order(12)
	public void playerLeaveRoomShouldReturnSuccess() {
		__playerApi.playerLeaveRoom(__playerApi.get(__testPlayerName), true);
		assertAll("playerLeaveRoom", () -> assertFalse(__roomApi.get(__testRoomId).contain(__testPlayerName)),
				() -> assertEquals(null, __playerApi.get(__testPlayerName).getRoom()));
	}

	@Test
	@Order(13)
	public void addNumberPlayersExceedsRoomCapacityShouldReturnErrorMessage() {
		int capacity = __roomApi.get(__testRoomId).getCapacity();
		PlayerModel[] players = new PlayerModel[capacity + 1];
		int counter = 0;
		for (var player : __playerApi.gets().values()) {
			if (counter > capacity) {
				break;
			}
			players[counter] = (PlayerModel) player;
			counter++;
		}
		assertAll("playerJoinRoom", () -> assertEquals(3, capacity),
				() -> assertEquals(null, __playerApi.playerJoinRoom(__roomApi.get(__testRoomId), players[0])),
				() -> assertEquals(null, __playerApi.playerJoinRoom(__roomApi.get(__testRoomId), players[1])),
				() -> assertEquals(null, __playerApi.playerJoinRoom(__roomApi.get(__testRoomId), players[2])),
				() -> assertEquals(ErrorMsg.ROOM_IS_FULL,
						__playerApi.playerJoinRoom(__roomApi.get(__testRoomId), players[3])));
	}

	@Test
	@Order(14)
	public void removeRoomShouldReturnSuccess() {
		__roomApi.remove(__roomApi.get(__testRoomId));
		boolean allRemoved = true;
		for (var player : __playerApi.gets().values()) {
			if (player.getRoom() != null) {
				allRemoved = false;
				break;
			}
		}
		final boolean removedResult = allRemoved;
		assertAll("removeRoom", () -> assertFalse(__roomApi.contain(__testRoomId)),
				() -> assertTrue(removedResult));
	}

}
