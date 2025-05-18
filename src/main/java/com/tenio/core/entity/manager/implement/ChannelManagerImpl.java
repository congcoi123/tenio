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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ChannelManagerImpl implements ChannelManager {

  private final EventManager eventManager;
  private final Map<String, Channel> channels;

  private ChannelManagerImpl(EventManager eventManager) {
    this.eventManager = eventManager;
    channels = new ConcurrentHashMap<>();
  }

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
    if (Objects.isNull(player)) {
      return;
    }
    if (Objects.isNull(channel)) {
      throw new ChannelNotExistException();
    }
    channel.addPlayer(player);
  }

  @Override
  public void unsubscribe(Channel channel, Player player) {
    if (Objects.isNull(channel) || Objects.isNull(player)) {
      return;
    }
    channel.removePlayer(player);
  }

  @Override
  public void unsubscribe(Player player) {
    if (Objects.isNull(player)) {
      return;
    }
    channels.values().forEach(channel -> channel.removePlayer(player));
  }

  @Override
  public void broadcast(Channel channel, DataCollection message) {
    if (Objects.isNull(channel)) {
      throw new ChannelNotExistException();
    }
    channel.getReadonlyPlayers()
        .forEach(player -> eventManager.emit(ServerEvent.BROADCAST_TO_CHANNEL, channel,
            player, message));
  }

  @Override
  public Map<String, Channel> getSubscribedChannelsForPlayer(Player player) {
    return channels.values().stream()
        .filter(channel -> channel.containsPlayer(player.getIdentity()))
        .collect(Collectors.toMap(Channel::getId, channel -> channel));
  }
}
