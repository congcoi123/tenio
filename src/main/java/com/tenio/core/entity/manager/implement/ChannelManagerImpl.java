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

package com.tenio.core.entity.manager.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.implement.ChannelImpl;
import com.tenio.core.entity.manager.ChannelManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ChannelNotExistException;
import com.tenio.core.exception.CreatedDuplicatedChannelException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ChannelManager}.
 */
public final class ChannelManagerImpl implements ChannelManager {

  private final EventManager eventManager;
  private final Map<String, Channel> channels;

  private ChannelManagerImpl(EventManager eventManager) {
    this.eventManager = eventManager;
    channels = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new instance.
   *
   * @param eventManager instance of {@link EventManager}
   * @return a new instance of {@link ChannelManager}
   */
  public static ChannelManager newInstance(EventManager eventManager) {
    return new ChannelManagerImpl(eventManager);
  }

  @Override
  public void createChannel(String id, String description) {
    if (channels.containsKey(id)) {
      throw new CreatedDuplicatedChannelException(id);
    }
    Channel channel = ChannelImpl.newInstance(id, eventManager, description);
    channels.put(channel.getId(), channel);
    eventManager.emit(ServerEvent.CHANNEL_CREATED, channel);
  }

  @Override
  public void removeChannel(String id) {
    if (channels.containsKey(id)) {
      Channel channel = channels.get(id);
      eventManager.emit(ServerEvent.CHANNEL_WILL_BE_REMOVED, channel);
      // Unsubscribe all players from this channel
      channel.removePlayers();
      channels.remove(id);
    }
  }

  @Override
  public void subscribe(Channel channel, Player player) {
    if (player == null) {
      return;
    }
    if (channel == null) {
      throw new ChannelNotExistException();
    }
    channel.addPlayer(player);
  }

  @Override
  public void unsubscribe(Channel channel, Player player) {
    if (channel == null || player == null) {
      return;
    }
    channel.removePlayer(player);
  }

  @Override
  public void unsubscribe(Player player) {
    if (player == null) {
      return;
    }
    channels.values().forEach(channel -> channel.removePlayer(player));
  }

  @Override
  public void broadcast(Channel channel, DataCollection message) {
    if (channel == null) {
      throw new ChannelNotExistException();
    }
    channel.getReadonlyPlayers()
        .forEach(player -> eventManager.emit(ServerEvent.BROADCAST_TO_CHANNEL, channel, player, message));
  }

  @Override
  public Map<String, Channel> getSubscribedChannelsForPlayer(Player player) {
    return channels.values().stream()
        .filter(channel -> channel.containsPlayer(player.getIdentity()))
        .collect(Collectors.toMap(Channel::getId, channel -> channel));
  }
}
