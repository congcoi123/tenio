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
package com.tenio.core.api;

import java.util.Collection;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.entities.defines.modes.PlayerBanMode;
import com.tenio.core.entities.defines.modes.PlayerLeaveRoomMode;
import com.tenio.core.entities.defines.modes.RoomRemoveMode;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.network.entities.session.Session;

public interface ServerApi {

	void login(String playerName);

	void login(String playerName, Session session);

	void logout(Player player);

	void kickPlayer(Player player, String message, int delayInSeconds);

	void banPlayer(Player player, String message, PlayerBanMode banMode, int durationInMinutes, int delayInSeconds);

	Room createRoom(InitialRoomSetting setting);

	Room createRoom(InitialRoomSetting setting, Player owner);

	Player getPlayerByName(String playerName);

	Player getPlayerBySession(Session session);
	
	int getPlayerCount();
	
	Collection<Player> getAllPlayers();

	Room getRoomById(long roomId);

	void joinRoom(Player player, Room room, String roomPassword, int slotInRoom, boolean asSpectator);

	void joinRoom(Player player, Room room);

	void switchPlayerToSpectator(Player player, Room room);

	void switchSpectatorToPlayer(Player player, Room room, int targetSlot);

	void leaveRoom(Player player, PlayerLeaveRoomMode leaveRoomMode);

	void removeRoom(Room room, RoomRemoveMode removeRoomMode);

	void sendPublicMessage(Player sender, Room room, ServerMessage message);

	void sendPrivateMessage(Player sender, Player recipient, ServerMessage message);

}
