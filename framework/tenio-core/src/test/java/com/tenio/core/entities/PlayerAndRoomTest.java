/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.core.entities;

public final class PlayerAndRoomTest {

//	private EventManager __eventManager;
//
//	private PlayerManager __playerManager;
//	private RoomManager __roomManager;
//
//	private String __testPlayerName;
//	private String __testRoomId;
//
//	@BeforeEach
//	public void initialize() {
//		var configuration = new Configuration("TenIOConfig.example.xml");
//
//		__eventManager = new EventManagerImpl();
//		__playerManager = new PlayerManagerImpl(__eventManager);
//		__playerManager.initialize(configuration);
//		__roomManager = new RoomManagerImpl(__eventManager);
//		__roomManager.initialize(configuration);
//		__playerApi = new PlayerApi(__playerManager, __roomManager);
//		__roomApi = new RoomApi(__roomManager);
//		__testPlayerName = "kong";
//		__testRoomId = UUID.randomUUID().toString();
//	}
//
//	@AfterEach
//	public void tearDown() {
//		__playerManager.clear();
//		__roomManager.clear();
//		__eventManager.clear();
//	}
//
//	@Test
//	public void addNewPlayerShouldReturnSuccess() {
//		var player = new PlayerModel(__testPlayerName);
//		__playerApi.login(player);
//		var result = __playerApi.get(__testPlayerName);
//
//		assertEquals(player, result);
//	}
//
//	@Test
//	public void addDupplicatedPlayerShouldCauseException() {
//		assertThrows(AddedDuplicatedPlayerException.class, () -> {
//			var player = new PlayerModel(__testPlayerName);
//			__playerManager.add(player);
//			__playerManager.add(player);
//		});
//	}
//
//	@Test
//	public void addNullPlayerNameShouldCauseException() {
//		assertThrows(NullPlayerNameException.class, () -> {
//			var player = new PlayerModel(null);
//			__playerManager.add(player, null);
//		});
//	}
//
//	@Test
//	public void checkContainPlayerShouldReturnSuccess() {
//		var player = new PlayerModel(__testPlayerName);
//		__playerApi.login(player);
//
//		assertTrue(__playerApi.contain(__testPlayerName));
//	}
//
//	@Test
//	public void countPlayersShouldReturnTrueValue() {
//		for (int i = 0; i < 10; i++) {
//			var player = new PlayerModel(UUID.randomUUID().toString());
//			__playerApi.login(player);
//		}
//
//		assertEquals(10, __playerApi.count());
//	}
//
//	@Test
//	public void countRealPlayersShouldReturnTrueValue() {
//		for (int i = 0; i < 10; i++) {
//			var player = new PlayerModel(UUID.randomUUID().toString());
//			__playerApi.login(player);
//		}
//
//		assertEquals(0, __playerApi.countPlayers());
//	}
//
//	@Test
//	public void removePlayerShouldReturnSuccess() {
//		var player = new PlayerModel(__testPlayerName);
//		__playerApi.login(player);
//		__playerApi.logOut(__testPlayerName);
//
//		assertEquals(0, __playerApi.count());
//	}
//
//	@Test
//	public void createNewRoomShouldReturnSuccess() {
//		var room = new RoomModel(__testRoomId, "Test Room", 3);
//		__roomApi.add(room);
//
//		assertTrue(__roomApi.contain(__testRoomId));
//	}
//
//	@Test
//	public void createDuplicatedRoomShouldCauseException() {
//		assertThrows(DuplicatedRoomIdException.class, () -> {
//			var room = new RoomModel(__testRoomId, "Test Room", 3);
//			__roomManager.add(room);
//			__roomManager.add(room);
//		});
//	}
//
//	@Test
//	public void playerJoinRoomShouldReturnSuccess() {
//		var player = new PlayerModel(__testPlayerName);
//		__playerApi.login(player);
//		var room = new RoomModel(__testRoomId, "Test Room", 3);
//		__roomApi.add(room);
//
//		assertEquals(null,
//				__playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), __playerApi.get(__testPlayerName)));
//	}
//
//	@Test
//	public void addDuplicatedPlayerToRoomShouldReturnErrorMessage() {
//		var player = new PlayerModel(__testPlayerName);
//		__playerApi.login(player);
//		var room = new RoomModel(__testRoomId, "Test Room", 3);
//		__roomApi.add(room);
//
//		__playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), __playerApi.get(__testPlayerName));
//
//		assertEquals(CoreMessageCode.PLAYER_WAS_IN_ROOM,
//				__playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), __playerApi.get(__testPlayerName)));
//	}
//
//	@Test
//	public void playerLeaveRoomShouldReturnSuccess() {
//		var player = new PlayerModel(__testPlayerName);
//		__playerApi.login(player);
//		var room = new RoomModel(__testRoomId, "Test Room", 3);
//		__roomApi.add(room);
//
//		__playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), __playerApi.get(__testPlayerName));
//		__playerApi.makePlayerLeaveRoom(__playerApi.get(__testPlayerName), true);
//
//		assertAll("playerLeaveRoom", () -> assertFalse(__roomApi.get(__testRoomId).containPlayerName(__testPlayerName)),
//				() -> assertEquals(null, __playerApi.get(__testPlayerName).getCurrentRoom()));
//	}
//
//	@Test
//	public void addNumberPlayersExceedsRoomCapacityShouldReturnErrorMessage() {
//		for (int i = 0; i < 10; i++) {
//			var player = new PlayerModel(UUID.randomUUID().toString());
//			__playerApi.login(player);
//		}
//
//		var room = new RoomModel(__testRoomId, "Test Room", 3);
//		__roomApi.add(room);
//
//		int capacity = __roomApi.get(__testRoomId).getCapacity();
//		PlayerModel[] players = new PlayerModel[capacity + 1];
//		int counter = 0;
//		for (var player : __playerApi.gets().values()) {
//			if (counter > capacity) {
//				break;
//			}
//			players[counter] = (PlayerModel) player;
//			counter++;
//		}
//
//		assertAll("playerJoinRoom", () -> assertEquals(3, capacity),
//				() -> assertEquals(null, __playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), players[0])),
//				() -> assertEquals(null, __playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), players[1])),
//				() -> assertEquals(null, __playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), players[2])),
//				() -> assertEquals(CoreMessageCode.ROOM_IS_FULL,
//						__playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), players[3])));
//	}
//
//	@Test
//	public void removeRoomShouldReturnSuccess() {
//		var room = new RoomModel(__testRoomId, "Test Room", 3);
//		__roomApi.add(room);
//
//		for (int i = 0; i < 3; i++) {
//			var player = new PlayerModel(UUID.randomUUID().toString());
//			__playerApi.login(player);
//			__playerApi.makePlayerJoinRoom(__roomApi.get(__testRoomId), player);
//		}
//
//		__roomApi.remove(__roomApi.get(__testRoomId));
//		boolean allRemoved = true;
//		for (var player : __playerApi.gets().values()) {
//			if (player.getCurrentRoom() != null) {
//				allRemoved = false;
//				break;
//			}
//		}
//		final boolean removedResult = allRemoved;
//
//		assertAll("removeRoom", () -> assertFalse(__roomApi.contain(__testRoomId)), () -> assertTrue(removedResult));
//	}

}
