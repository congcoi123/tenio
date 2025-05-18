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

package com.tenio.core.entity;

import com.tenio.core.entity.manager.ChannelManager;
import java.util.List;
import java.util.Map;

/**
 * Represents an abstract channel entity used for player communication and notifications.
 * This class provides a foundation for implementing different types of communication channels
 * in the game server.
 *
 * <p>Key features:
 * <ul>
 *   <li>Player notification management</li>
 *   <li>Channel membership tracking</li>
 *   <li>Message broadcasting capabilities</li>
 *   <li>Channel state management</li>
 * </ul>
 *
 * <p>Thread safety: Implementations of this class should be thread-safe
 * as they may be accessed from multiple threads concurrently.
 *
 * @see Player
 * @see ChannelManager
 * @since 0.6.3
 */
public interface Channel {

  /**
   * Retrieves the channel's id.
   *
   * @return the channel's id
   */
  String getId();

  /**
   * Retrieves the channel's description.
   *
   * @return the channel's description
   */
  String getDescription();

  /**
   * Sets description to the channel.
   *
   * @param description the description
   */
  void setDescription(String description);

  /**
   * Retrieves the players who are subscribing the channel.
   *
   * @return a {@link Map} of {@link Player} instances
   */
  Map<String, Player> getPlayers();

  /**
   * Retrieves the snapshot of subscribing players.
   *
   * @return a {@link List} of {@link Player} instance
   */
  List<Player> getReadonlyPlayers();

  /**
   * Retrieves the current number of subscribing players.
   *
   * @return the current number of subscribing players
   */
  int countPlayer();

  /**
   * Determines whether a player is subscribing the channel.
   *
   * @param playerIdentity the player's identity value
   * @return {@code true} if the player is subscribing the channel, otherwise returns {@code false}
   */
  boolean containsPlayer(String playerIdentity);

  /**
   * Adds a player into the subscribing management.
   *
   * @param player an instance of {@link Player}
   */
  void addPlayer(Player player);

  /**
   * Removes a player from the subscribing management.
   *
   * @param player an instance of {@link Player}
   */
  void removePlayer(Player player);

  /**
   * Removes all players from the subscribing management.
   */
  void removePlayers();
}
