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

import com.tenio.common.data.ZeroObject;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.modes.PlayerBanMode;
import com.tenio.core.entities.defines.results.PlayerDisconnectedResult;
import com.tenio.core.entities.defines.results.PlayerLeftRoomResult;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.network.entities.session.Session;

/**
 * @author kong
 */
// TODO: Add description
public interface ServerApi {

	void login(Player player);

	void login(Player player, Session session);

	void logout(Player player);

	void kickPlayer(Player player, String message, int delayInSeconds);

	void banPlayer(Player player, String message, PlayerBanMode banMode, int durationInMinutes, int delayInSeconds);

	void disconnectPlayer(Player player, PlayerDisconnectedResult disconnectedReason);

	void disconnectSession(Session session);

	Room createRoom(InitialRoomSetting setting, Player owner);

	Room createRoom(InitialRoomSetting setting, Player ownder, Room roomToLeave);

	Player getPlayerByName(String playerName);

	Player getPlayerBySession(Session session);

	void joinRoom(Player player, Room room, int slotInRoom);

	void joinRoom(Player player, Room room);

	void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave);

	void joinRoom(Player player, Room room, String roomPassword, boolean asSpectator, Room roomToLeave, int slotInRoom);

	void switchPlayerToSpectator(Player player, Room room);

	void switchSpectatorToPlayer(Player player, Room room, int targetSlot);

	void switchSpectatorToPlayer(Player player, Room room);

	void leaveRoom(Player player, PlayerLeftRoomResult leftRoomReason);

	void removeRoom(Room room);

	void sendPublicMessage(Room room, Player sender, ZeroObject message);

	void sendPrivateMessage(Player sender, Player recipient, ZeroObject message);

}
