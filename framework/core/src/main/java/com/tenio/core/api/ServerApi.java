/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.mode.PlayerBanMode;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.implement.RoomImpl;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.entity.setting.InitialRoomSetting;
import com.tenio.core.handler.event.EventPlayerLoggedinResult;
import com.tenio.core.network.entity.session.Session;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * This class provides all supported APIs working with the server.
 */
public interface ServerApi {

  /**
   * Allows creating an instance of a player in the server. The player does not contain
   * session, it should act like a server's bot.
   *
   * @param playerName a unique player's name ({@link String} value) on the server
   * @see Session
   * @see EventPlayerLoggedinResult
   */
  void login(String playerName);

  /**
   * Allows creating an instance of a player in the server.
   *
   * @param playerName a unique player's name ({@link String} value) on the server
   * @param session    a {@link Session} associated to the player
   * @see EventPlayerLoggedinResult
   */
  void login(String playerName, Session session);

  /**
   * Removes a player from the management list and from the server as well.
   *
   * @param player the current {@link Player} in the management list, on the server
   */
  void logout(Player player);

  /**
   * Removes a player manually from the server.
   *
   * @param player         the {@link Player} needs to be removed
   * @param message        a {@link String} text delivered to the player before it leaves
   * @param delayInSeconds a countdown time in seconds ({@code integer} value) for removing the
   *                       player. In case the player has to be out immediately, this value
   *                       should be set to {@code 0}
   * @throws UnsupportedOperationException this method is not supported at the moment
   */
  default void kickPlayer(Player player, String message, int delayInSeconds) {
    throw new UnsupportedOperationException("Unsupported at the moment");
  }

  /**
   * Prevents a player from logging on the server.
   *
   * @param player            the {@link Player} needs to be banned
   * @param message           a text ({@link String} value) delivered to the player before it is
   *                          banned
   * @param banMode           a rule ({@link PlayerBanMode}) applied for the player
   * @param durationInMinutes how long the player takes punishment calculated in
   *                          minutes ({@code integer} value)
   * @param delayInSeconds    a countdown time in seconds ({@code integer} value) for banning
   *                          the player. In case the player has to be banned immediately, this
   *                          value should be set to {@code 0}
   * @throws UnsupportedOperationException this method is not supported at the moment
   */
  default void banPlayer(Player player, String message, PlayerBanMode banMode,
                         int durationInMinutes,
                         int delayInSeconds) {
    throw new UnsupportedOperationException("Unsupported at the moment");
  }

  /**
   * Creates a new room on the server without an owner.
   *
   * @param setting all room {@link InitialRoomSetting} at the time its created
   * @return a new instance of {@link Room} if available, otherwise {@code null}
   */
  default Room createRoom(InitialRoomSetting setting) {
    return createRoom(setting, null);
  }

  /**
   * Creates a new room on the server.
   *
   * @param setting all room {@link InitialRoomSetting} at the time its created
   * @param owner   a {@link Player} owner of this room, owner can also be declared by
   *                {@code null} value
   * @return a new instance of {@link Room} if available, otherwise {@code null}
   */
  Room createRoom(InitialRoomSetting setting, Player owner);

  /**
   * Retrieves a player on the server by using its name.
   *
   * @param playerName a unique {@link String} value for player's name on the server
   * @return a corresponding instance of optional {@link Player}
   * @see Optional
   */
  Optional<Player> getPlayerByName(String playerName);

  /**
   * Retrieves a player on the server by using its session (if present).
   *
   * @param session a {@link Session} associating to the player on the server
   * @return a corresponding instance of optional {@link Player}
   * @see Optional
   */
  Optional<Player> getPlayerBySession(Session session);

  /**
   * Fetches the current number of players activating on the server.
   *
   * @return the current number of players ({@code integer} value)
   */
  int getPlayerCount();

  /**
   * Retrieves an iterator for the global player management list on the server. This method should
   * be used to prevent the "escape references" issue.
   *
   * @return an iterator of {@link Player} management list
   * @see PlayerManager
   * @see Iterator
   */
  Iterator<Player> getPlayerIterator();

  /**
   * Retrieves a read-only global player management list on the server. This method should be used
   * to prevent the "escape references" issue.
   *
   * @return a list of all {@link Player}s in the management list on the server
   * @see PlayerManager
   * @see List
   */
  List<Player> getReadonlyPlayersList();

  /**
   * Retrieves a room instance by using its ID.
   *
   * @param roomId a unique room ID ({@code long} value)
   * @return an optional {@link Room} instance
   * @see Optional
   */
  Optional<Room> getRoomById(long roomId);

  /**
   * Retrieves an iterator for the room management list on the server. This method should be used
   * to prevent the "escape references" issue.
   *
   * @return a list of all rooms {@link Room} in the management list on the server
   * @see RoomManager
   * @see Iterator
   */
  Iterator<Room> getRoomIterator();

  /**
   * Retrieves a read-only global room management list on the server. This method should be used
   * to prevent the "escape references" issue.
   *
   * @return a list of all rooms {@link Room} in the management list on the server
   * @see RoomManager
   * @see List
   */
  List<Room> getReadonlyRoomsList();

  /**
   * Allows a player to join a particular room.
   *
   * @param player       the joining {@link Player}
   * @param room         the current {@link Room}
   * @param roomPassword a {@link String} credential using for a player to join room.
   *                     In case of free join, this value would be set to {@code null}
   * @param slotInRoom   the position of player located in the room ({@code integer} value)
   * @param asSpectator  sets by {@code true} if the player operating in the room as a
   *                     spectator, otherwise returns {@code false}
   */
  void joinRoom(Player player, Room room, String roomPassword, int slotInRoom, boolean asSpectator);

  /**
   * Allows a player to join a particular room as the role of "player" with the room's password is
   * not present, and the player position in room is not considered.
   *
   * @param player the joining {@link Player}
   * @param room   the current {@link Room}
   * @see RoomImpl#DEFAULT_SLOT
   */
  default void joinRoom(Player player, Room room) {
    joinRoom(player, room, null, RoomImpl.DEFAULT_SLOT, false);
  }

  /**
   * Changes the role of a player in its room, from "participant" to "spectator".
   *
   * @param player the checking {@link Player}
   * @param room   the current player's {@link Room}
   * @throws UnsupportedOperationException this method is not supported at the moment
   */
  default void switchParticipantToSpectator(Player player, Room room) {
    throw new UnsupportedOperationException("Unsupported at the moment");
  }

  /**
   * Changes the role of a player in its room, from "spectator" to "participant".
   *
   * @param player     the checking spectator ({@link Player} instance)
   * @param room       the current spectator's {@link Room}
   * @param targetSlot a new position ({@code integer} value) of transformed "participant" in
   *                   its room
   * @throws UnsupportedOperationException this method is not supported at the moment
   */
  default void switchSpectatorToParticipant(Player player, Room room, int targetSlot) {
    throw new UnsupportedOperationException("Unsupported at the moment");
  }

  /**
   * Makes a player to leave its current room.
   *
   * @param player        the leaving {@link Player}
   * @param leaveRoomMode a rule ({@link PlayerLeaveRoomMode}) applied for the leaving player
   */
  void leaveRoom(Player player, PlayerLeaveRoomMode leaveRoomMode);

  /**
   * Removes a room from the management list, server.
   *
   * @param room           the removing {@link Room}
   * @param removeRoomMode a rule ({@link RoomRemoveMode}) applied for the removing room
   */
  void removeRoom(Room room, RoomRemoveMode removeRoomMode);

  /**
   * Sends a message from a player to all recipients in a room.
   *
   * @param sender  the sender ({@link Player} instance)
   * @param room    all recipients in the same {@link Room}
   * @param message the sending {@link ServerMessage}
   * @throws UnsupportedOperationException the method is not supported at the moment
   */
  default void sendPublicMessage(Player sender, Room room, ServerMessage message) {
    throw new UnsupportedOperationException("Unsupported at the moment");
  }

  /**
   * Sends a message from a player to another recipient.
   *
   * @param sender    the sender ({@link Player} instance)
   * @param recipient the receiver ({@link Player} instance)
   * @param message   the sending {@link ServerMessage}
   * @throws UnsupportedOperationException the method is not supported at the moment
   */
  default void sendPrivateMessage(Player sender, Player recipient, ServerMessage message) {
    throw new UnsupportedOperationException("Unsupported at the moment");
  }

  /**
   * Retrieves the current available UDP port.
   *
   * @return an {@code integer} value of UDP port
   * @since 0.3.0
   */
  int getCurrentAvailableUdpPort();

  /**
   * Retrieves the time when server starts in milliseconds.
   *
   * @return started time in milliseconds
   * @since 0.3.1
   */
  long getStartedTime();

  /**
   * Retrieves the current uptime of server in milliseconds.
   *
   * @return the current server's uptime in milliseconds
   * @since 0.3.1
   */
  long getUptime();
}
