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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An implemented class is for a player using on the server.
 */
public class DefaultPlayer implements Player {

  private final String identity;
  private final Map<String, Object> properties;
  private final AtomicReference<PlayerState> state;
  private final AtomicReference<PlayerRoleInRoom> roleInRoom;
  private Consumer<Field> updateConsumer;

  private volatile Room currentRoom;
  private volatile Session session;

  private volatile long lastLoginTime;
  private volatile long lastJoinedRoomTime;
  private volatile long lastReadTime;
  private volatile long lastWriteTime;
  private volatile long lastActivityTime;

  private volatile int playerSlotInCurrentRoom;

  private volatile boolean loggedIn;
  private volatile boolean activated;
  private volatile boolean deportedFlag;

  private int maxIdleTimeInSecond;
  private int maxIdleTimeNeverDeportedInSecond;

  /**
   * Constructor.
   *
   * @param identity the player unique name
   */
  public DefaultPlayer(String identity) {
    this(identity, null);
  }

  /**
   * Constructor.
   *
   * @param identity the player unique name
   * @param session  a session which associates to the player
   */
  public DefaultPlayer(String identity, Session session) {
    this.identity = identity;
    properties = new ConcurrentHashMap<>();
    state = new AtomicReference<>();
    roleInRoom = new AtomicReference<>();
    setState(null);
    setRoleInRoom(PlayerRoleInRoom.SPECTATOR);
    playerSlotInCurrentRoom = Room.NIL_SLOT;
    long currentTime = now();
    setSession(session);
    setLastReadTime(currentTime);
    setLastWriteTime(currentTime);
  }

  /**
   * Create a new instance without session.
   *
   * @param name a unique name for player on the server
   * @return a new instance
   */
  public static Player newInstance(String name) {
    return new DefaultPlayer(name);
  }

  /**
   * Create a new instance.
   *
   * @param name    a unique name for player on the server
   * @param session a session associated to the player
   * @return a new instance
   */
  public static Player newInstance(String name, Session session) {
    return new DefaultPlayer(name, session);
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public boolean containsSession() {
    return Objects.nonNull(session);
  }

  @Override
  public boolean isState(PlayerState state) {
    return getState() == state;
  }

  @Override
  public PlayerState getState() {
    return state.get();
  }

  @Override
  public void setState(PlayerState state) {
    this.state.set(state);
    notifyUpdate(Field.STATE);
  }

  @Override
  public boolean transitionState(PlayerState expectedState, PlayerState newState) {
    if (state.compareAndSet(expectedState, newState)) {
      notifyUpdate(Field.STATE);
      return true;
    }
    return false;
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public void setActivated(boolean activated) {
    this.activated = activated;
    notifyUpdate(Field.ACTIVATION);
  }

  @Override
  public boolean isLoggedIn() {
    return loggedIn;
  }

  @Override
  public void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
    if (loggedIn) {
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
  public long getInactiveTimeInSeconds() {
    return (now() - getLastActivityTime()) / 1000L;
  }

  private void setLastActivityTime(long timestamp) {
    lastActivityTime = timestamp;
  }

  @Override
  public long getLastReadTime() {
    return lastReadTime;
  }

  @Override
  public void setLastReadTime(long timestamp) {
    lastReadTime = timestamp;
    setLastActivityTime(timestamp);
  }

  @Override
  public long getLastWriteTime() {
    return lastWriteTime;
  }

  @Override
  public void setLastWriteTime(long timestamp) {
    lastWriteTime = timestamp;
    setLastActivityTime(timestamp);
  }

  @Override
  public boolean isIdle() {
    return isConnectionIdle(maxIdleTimeInSecond);
  }

  @Override
  public boolean isNeverDeported() {
    return deportedFlag;
  }

  @Override
  public void setNeverDeported(boolean flag) {
    deportedFlag = flag;
    notifyUpdate(Field.DEPORTATION);
  }

  @Override
  public boolean isIdleNeverDeported() {
    return isNeverDeported() && isConnectionIdle(maxIdleTimeNeverDeportedInSecond);
  }

  @Override
  public Optional<Session> getSession() {
    return Optional.ofNullable(session);
  }

  @Override
  public void setSession(Session session) {
    if (Objects.nonNull(session)) {
      session.setName(identity);
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
    return roleInRoom.get();
  }

  @Override
  public void setRoleInRoom(PlayerRoleInRoom roleInRoom) {
    this.roleInRoom.set(roleInRoom);
    notifyUpdate(Field.ROLE_IN_ROOM);
  }

  @Override
  public boolean transitionRole(PlayerRoleInRoom expectedRole, PlayerRoleInRoom newRole) {
    if (roleInRoom.compareAndSet(expectedRole, newRole)) {
      notifyUpdate(Field.ROLE_IN_ROOM);
      return true;
    }
    return false;
  }

  @Override
  public Optional<Room> getCurrentRoom() {
    return Optional.ofNullable(currentRoom);
  }

  @Override
  public void setCurrentRoom(Room room) {
    currentRoom = room;
    setPlayerSlotInCurrentRoom(Objects.isNull(room) ? Room.NIL_SLOT : Room.DEFAULT_SLOT);
    setLastJoinedRoomTime();
  }

  @Override
  public long getLastJoinedRoomTime() {
    return lastJoinedRoomTime;
  }

  @Override
  public int getPlayerSlotInCurrentRoom() {
    return playerSlotInCurrentRoom;
  }

  @Override
  public void setPlayerSlotInCurrentRoom(int slot) {
    playerSlotInCurrentRoom = slot;
    notifyUpdate(Field.SLOT_IN_ROOM);
  }

  @Override
  public Object getProperty(String key) {
    return properties.get(key);
  }

  @Override
  public void setProperty(String key, Object value) {
    properties.put(key, value);
    notifyUpdate(Field.PROPERTY);
  }

  @Override
  public boolean containsProperty(String key) {
    return properties.containsKey(key);
  }

  @Override
  public void removeProperty(String key) {
    properties.remove(key);
    notifyUpdate(Field.PROPERTY);
  }

  @Override
  public void clearProperties() {
    properties.clear();
    notifyUpdate(Field.PROPERTY);
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

  @Override
  public void configureMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSecond = seconds;
  }

  @Override
  public void configureMaxIdleTimeNeverDeportedInSeconds(int seconds) {
    maxIdleTimeNeverDeportedInSecond = seconds;
  }

  /**
   * Retrieves current time in milliseconds.
   *
   * @return current time in milliseconds
   * @see TimeUtility#currentTimeMillis()
   */
  protected long now() {
    return TimeUtility.currentTimeMillis();
  }

  private boolean isConnectionIdle(int maxIdleTimeInSecond) {
    return (maxIdleTimeInSecond > 0) && (getInactiveTimeInSeconds() > maxIdleTimeInSecond);
  }

  private void setLastLoggedInTime() {
    lastLoginTime = now();
  }

  private void setLastJoinedRoomTime() {
    lastJoinedRoomTime = now();
  }

  // This is not thread-safe
  private void notifyUpdate(Field field) {
    if (Objects.nonNull(updateConsumer)) {
      updateConsumer.accept(field);
    }
  }

  @Override
  public boolean equals(Object object) {
    return (object instanceof Player player) && identity.equals(player.getIdentity());
  }

  @Override
  public int hashCode() {
    return Objects.isNull(identity) ? 0 : identity.hashCode();
  }

  @Override
  public String toString() {
    return "DefaultPlayer{" +
        "identity='" + identity + '\'' +
        ", properties=" + properties +
        ", session=" + session +
        ", currentRoom=" + currentRoom +
        ", state=" + state.get() +
        ", roleInRoom=" + roleInRoom.get() +
        ", lastLoginTime=" + lastLoginTime +
        ", lastJoinedRoomTime=" + lastJoinedRoomTime +
        ", lastReadTime=" + lastReadTime +
        ", lastWriteTime=" + lastWriteTime +
        ", lastActivityTime=" + lastActivityTime +
        ", playerSlotInCurrentRoom=" + playerSlotInCurrentRoom +
        ", loggedIn=" + loggedIn +
        ", activated=" + activated +
        ", deportedFlag=" + deportedFlag +
        ", maxIdleTimeInSecond=" + maxIdleTimeInSecond +
        ", maxIdleTimeNeverDeportedInSecond=" + maxIdleTimeNeverDeportedInSecond +
        '}';
  }
}
