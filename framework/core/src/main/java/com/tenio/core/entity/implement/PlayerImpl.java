/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

import com.tenio.common.utility.TimeUtility;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.PlayerState;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.room.PlayerRoleInRoom;
import com.tenio.core.network.entity.session.Session;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * An implemented class is for a player using on the server.
 */
public class PlayerImpl implements Player {

  private final String name;
  private final Map<String, Object> properties;
  private Consumer<Field> updateConsumer;
  private volatile Session session;
  private volatile Room currentRoom;
  private volatile PlayerState state;
  private volatile PlayerRoleInRoom roleInRoom;

  private volatile long lastLoginTime;
  private volatile long lastJoinedRoomTime;
  private volatile long lastReadTime;
  private volatile long lastWriteTime;
  private volatile long lastActivityTime;

  private int maxIdleTimeInSecond;
  private int maxIdleTimeNeverDeportedInSecond;
  private volatile int playerSlotInCurrentRoom;

  private volatile boolean loggedIn;
  private volatile boolean activated;
  private volatile boolean deportedFlag;

  /**
   * Constructor.
   *
   * @param name the player unique name
   */
  public PlayerImpl(String name) {
    this(name, null);
  }

  /**
   * Constructor.
   *
   * @param name    the player unique name
   * @param session a session which associates to the player
   */
  public PlayerImpl(String name, Session session) {
    this.name = name;
    properties = new ConcurrentHashMap<>();
    lastLoginTime = 0L;
    lastJoinedRoomTime = 0L;
    setCurrentRoom(null);
    setRoleInRoom(PlayerRoleInRoom.SPECTATOR);
    setSession(session);
    setLoggedIn(false);
    setActivated(false);
    setLastReadTime(now());
    setLastWriteTime(now());
    setLastActivityTime(now());
    setNeverDeported(false);
  }

  /**
   * Create a new instance without session.
   *
   * @param name a unique name for player on the server
   * @return a new instance
   */
  public static Player newInstance(String name) {
    return new PlayerImpl(name);
  }

  /**
   * Create a new instance.
   *
   * @param name    a unique name for player on the server
   * @param session a session associated to the player
   * @return a new instance
   */
  public static Player newInstance(String name, Session session) {
    return new PlayerImpl(name, session);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean containsSession() {
    return Objects.nonNull(session);
  }

  @Override
  public boolean isState(PlayerState state) {
    return this.state == state;
  }

  @Override
  public PlayerState getState() {
    return state;
  }

  @Override
  public void setState(PlayerState state) {
    this.state = state;
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.STATE);
    }
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public void setActivated(boolean activated) {
    this.activated = activated;
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.ACTIVATION);
    }
  }

  @Override
  public boolean isLoggedIn() {
    return loggedIn;
  }

  @Override
  public void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
    if (this.loggedIn) {
      setLastLoggedInTime();
    }
  }

  @Override
  public long getLastLoggedInTime() {
    return lastLoginTime;
  }

  @Override
  public long getLastActivityTime() {
    return lastActivityTime;
  }

  @Override
  public void setLastActivityTime(long timestamp) {
    lastActivityTime = timestamp;
  }

  @Override
  public long getLastReadTime() {
    return lastReadTime;
  }

  @Override
  public void setLastReadTime(long timestamp) {
    lastReadTime = timestamp;
    setLastActivityTime(lastReadTime);
  }

  @Override
  public long getLastWriteTime() {
    return lastWriteTime;
  }

  @Override
  public void setLastWriteTime(long timestamp) {
    lastWriteTime = timestamp;
    setLastActivityTime(lastWriteTime);
  }

  @Override
  public int getMaxIdleTimeInSeconds() {
    return maxIdleTimeInSecond;
  }

  @Override
  public void setMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public boolean isIdle() {
    return isConnectionIdle(getMaxIdleTimeInSeconds());
  }

  @Override
  public boolean isNeverDeported() {
    return deportedFlag;
  }

  @Override
  public void setNeverDeported(boolean flag) {
    deportedFlag = flag;
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.DEPORTATION);
    }
  }

  @Override
  public int getMaxIdleTimeNeverDeportedInSeconds() {
    return maxIdleTimeNeverDeportedInSecond;
  }

  @Override
  public void setMaxIdleTimeNeverDeportedInSeconds(int seconds) {
    maxIdleTimeNeverDeportedInSecond = seconds;
  }

  @Override
  public boolean isIdleNeverDeported() {
    return isNeverDeported() && isConnectionIdle(getMaxIdleTimeNeverDeportedInSeconds());
  }

  private boolean isConnectionIdle(int maxIdleTimeInSecond) {
    if (maxIdleTimeInSecond > 0) {
      long elapsedSinceLastActivity = now() - getLastActivityTime();
      return elapsedSinceLastActivity / 1000L > (long) maxIdleTimeInSecond;
    }
    return false;
  }

  private void setLastLoggedInTime() {
    lastLoginTime = now();
  }

  @Override
  public Optional<Session> getSession() {
    return Optional.ofNullable(session);
  }

  @Override
  public void setSession(Session session) {
    if (Objects.nonNull(session)) {
      session.setName(name);
      session.setAssociatedToPlayer(Session.AssociatedState.DONE);
    }
    this.session = session;
  }

  @Override
  public boolean isInRoom() {
    return Objects.nonNull(currentRoom);
  }

  @Override
  public PlayerRoleInRoom getRoleInRoom() {
    return roleInRoom;
  }

  @Override
  public void setRoleInRoom(PlayerRoleInRoom roleInRoom) {
    this.roleInRoom = roleInRoom;
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.ROLE_IN_ROOM);
    }
  }

  @Override
  public Optional<Room> getCurrentRoom() {
    return Optional.ofNullable(currentRoom);
  }

  @Override
  public void setCurrentRoom(Room room) {
    currentRoom = room;
    if (Objects.isNull(currentRoom)) {
      playerSlotInCurrentRoom = Room.NIL_SLOT;
    } else {
      playerSlotInCurrentRoom = Room.DEFAULT_SLOT;
    }
    setLastJoinedRoomTime();
  }

  @Override
  public long getLastJoinedRoomTime() {
    return lastJoinedRoomTime;
  }

  private void setLastJoinedRoomTime() {
    lastJoinedRoomTime = now();
  }

  @Override
  public int getPlayerSlotInCurrentRoom() {
    return playerSlotInCurrentRoom;
  }

  @Override
  public void setPlayerSlotInCurrentRoom(int slot) {
    playerSlotInCurrentRoom = slot;
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.SLOT_IN_ROOM);
    }
  }

  @Override
  public Object getProperty(String key) {
    return properties.get(key);
  }

  @Override
  public void setProperty(String key, Object value) {
    properties.put(key, value);
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.PROPERTY);
    }
  }

  @Override
  public boolean containsProperty(String key) {
    return properties.containsKey(key);
  }

  @Override
  public void removeProperty(String key) {
    properties.remove(key);
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.PROPERTY);
    }
  }

  @Override
  public void clearProperties() {
    properties.clear();
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(Field.PROPERTY);
    }
  }

  @Override
  public void onUpdateListener(Consumer<Field> updateConsumer) {
    this.updateConsumer = updateConsumer;
  }

  @Override
  public void clean() {
    setActivated(false);
    setCurrentRoom(null);
    setSession(null);
    clearProperties();
  }

  private long now() {
    return TimeUtility.currentTimeMillis();
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Player player)) {
      return false;
    } else {
      return getName().equals(player.getName());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (Objects.isNull(name) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Player{" +
        "name='" + name + '\'' +
        ", properties=" + properties +
        ", currentRoom=" + (Objects.nonNull(currentRoom) ? currentRoom.getId() : "") +
        ", state=" + state +
        ", roleInRoom=" + roleInRoom +
        ", lastLoginTime=" + lastLoginTime +
        ", lastJoinedRoomTime=" + lastJoinedRoomTime +
        ", lastReadTime=" + lastReadTime +
        ", lastWriteTime=" + lastWriteTime +
        ", lastActivityTime=" + lastActivityTime +
        ", maxIdleTimeInSecond=" + maxIdleTimeInSecond +
        ", maxIdleTimeNeverDeportedInSecond=" + maxIdleTimeNeverDeportedInSecond +
        ", playerSlotInCurrentRoom=" + playerSlotInCurrentRoom +
        ", loggedIn=" + loggedIn +
        ", activated=" + activated +
        ", deportedFlag=" + deportedFlag +
        ", session=" + session +
        '}';
  }
}
