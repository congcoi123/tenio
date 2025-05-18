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

package com.tenio.core.entity.implement;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.event.implement.EventManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ChannelImpl implements Channel {

  private final String id;
  private final Map<String, Player> players;
  private final EventManager eventManager;
  private String description;

  private ChannelImpl(String id, EventManager eventManager, String description) {
    this.id = id;
    this.eventManager = eventManager;
    this.description = description;
    players = new ConcurrentHashMap<>();
  }

  public static Channel newInstance(String id, EventManager eventManager, String description) {
    return new ChannelImpl(id, eventManager, description);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Map<String, Player> getPlayers() {
    return players;
  }

  @Override
  public List<Player> getReadonlyPlayers() {
    return players.values().stream().toList();
  }

  @Override
  public int countPlayer() {
    return players.size();
  }

  @Override
  public boolean containsPlayer(String playerIdentity) {
    return players.containsKey(playerIdentity);
  }

  @Override
  public void addPlayer(Player player) {
    players.put(player.getIdentity(), player);
    eventManager.emit(ServerEvent.PLAYER_SUBSCRIBED_CHANNEL, this, player);
  }

  @Override
  public void removePlayer(Player player) {
    players.remove(player.getIdentity());
    eventManager.emit(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, this, player);
  }

  @Override
  public void removePlayers() {
    var iterator = players.values().iterator();
    while (iterator.hasNext()) {
      var player = iterator.next();
      iterator.remove();
      eventManager.emit(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, this, player);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChannelImpl channel = (ChannelImpl) o;
    return Objects.equals(id, channel.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Channel{" +
        "id='" + id + '\'' +
        ", description='" + description + '\'' +
        ", playerCount=" + players.size() +
        '}';
  }
}
