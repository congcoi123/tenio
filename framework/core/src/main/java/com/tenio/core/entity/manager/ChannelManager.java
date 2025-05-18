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

package com.tenio.core.entity.manager;

import com.tenio.common.data.DataCollection;
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.exception.CreatedDuplicatedChannelException;
import java.util.Map;

/**
 * Manages the lifecycle and operations of communication channels in the game server.
 * This interface provides comprehensive channel management capabilities including
 * creation, deletion, and player management within channels.
 *
 * <p>Key features:
 * <ul>
 *   <li>Channel lifecycle management (creation, deletion, cleanup)</li>
 *   <li>Player membership management within channels</li>
 *   <li>Message broadcasting across channels</li>
 *   <li>Channel state tracking and validation</li>
 *   <li>Thread-safe channel operations</li>
 *   <li>Channel event handling</li>
 * </ul>
 *
 * <p>Thread safety: Implementations of this interface should be thread-safe
 * as they handle concurrent channel operations. The interface provides atomic
 * operations for channel management and player membership changes.
 *
 * <p>Note: This interface is designed to work in conjunction with the
 * {@link Channel} interface for managing communication channels and their
 * associated players.
 *
 * @see Channel
 * @see Player
 * @see CreatedDuplicatedChannelException
 * @since 0.6.3
 */
public interface ChannelManager {

  /**
   * Creates a new channel.
   *
   * @param id          a unique value and should not be duplicated
   * @param description the description of channel
   * @throws CreatedDuplicatedChannelException when a channel which has its id to be already
   *                                           registered is added onto server.
   */
  void createChannel(String id, String description) throws CreatedDuplicatedChannelException;

  /**
   * Creates a new channel.
   *
   * @param id a unique value and should not be duplicated
   * @throws CreatedDuplicatedChannelException when a channel which has its id to be already
   *                                           registered is added onto server.
   */
  default void createChannel(String id) throws CreatedDuplicatedChannelException {
    createChannel(id, null);
  }

  /**
   * Removes a channel from management.
   *
   * @param id the channel's id
   */
  void removeChannel(String id);

  /**
   * A player subscribes to a channel.
   *
   * @param channel an instance of {@link Channel}
   * @param player an instance of {@link Player}
   */
  void subscribe(Channel channel, Player player);

  /**
   * A player unsubscribes from a channel.
   *
   * @param channel an instance of {@link Channel}
   * @param player an instance of {@link Player}
   */
  void unsubscribe(Channel channel, Player player);

  /**
   * A player unsubscribes from all channels.
   *
   * @param player an instance of {@link Player}
   */
  void unsubscribe(Player player);

  /**
   * Broadcasts a message to a channel.
   *
   * @param channel an instance of {@link Channel}
   * @param message an instance of {@link DataCollection}
   */
  void broadcast(Channel channel, DataCollection message);

  /**
   * Retrieves all channels that a player is subscribing to.
   *
   * @param player an instance of {@link Player}
   * @return a map of subscribing {@link Channel}
   */
  Map<String, Channel> getSubscribedChannelsForPlayer(Player player);
}
