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

package com.tenio.core.entity.setting;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.setting.strategy.RoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomCredentialValidatedStrategy;
import com.tenio.core.entity.setting.strategy.implement.DefaultRoomPlayerSlotGeneratedStrategy;
import com.tenio.core.schedule.task.internal.AutoRemoveRoomTask;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * The initialized information is for creating a new room.
 */
public final class InitialRoomSetting {

  private final String name;
  private final String password;
  private final int maxParticipants;
  private final int maxSpectators;
  private final boolean activated;
  private final RoomRemoveMode roomRemoveMode;
  private final RoomCredentialValidatedStrategy roomCredentialValidatedStrategy;
  private final RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy;

  private InitialRoomSetting(Builder builder) {
    name = builder.name;
    password = builder.password;
    maxParticipants = builder.maxParticipants;
    maxSpectators = builder.maxSpectators;
    activated = builder.activated;
    roomRemoveMode = builder.removeMode;
    roomCredentialValidatedStrategy = builder.credentialValidatedStrategy;
    roomPlayerSlotGeneratedStrategy = builder.playerSlotGeneratedStrategy;

  }

  /**
   * Retrieves the room's name.
   *
   * @return the {@link String} value of room's name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves the room's password.
   *
   * @return the {@link String} value of room's password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Retrieves the maximum number of participants allowing in the room.
   *
   * @return the maximum number of participants allowing in the room
   */
  public int getMaxParticipants() {
    return maxParticipants;
  }

  /**
   * Retrieves the maximum number of spectators allowing in the room.
   *
   * @return the maximum number of spectators allowing in the room
   */
  public int getMaxSpectators() {
    return maxSpectators;
  }

  /**
   * Determines whether the room is active.
   *
   * @return {@code true} if the room is activated, otherwise returns {@code false}
   */
  public boolean isActivated() {
    return activated;
  }

  /**
   * Retrieves the room removing mode.
   *
   * @return the {@link RoomRemoveMode}
   */
  public RoomRemoveMode getRoomRemoveMode() {
    return roomRemoveMode;
  }

  /**
   * Retrieves the room credential validation strategy.
   *
   * @return an instance of {@link RoomCredentialValidatedStrategy}
   */
  public RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy() {
    return roomCredentialValidatedStrategy;
  }

  /**
   * Retrieves the participant slot generation strategy in the room.
   *
   * @return an instance of {@link RoomPlayerSlotGeneratedStrategy}
   */
  public RoomPlayerSlotGeneratedStrategy getRoomPlayerSlotGeneratedStrategy() {
    return roomPlayerSlotGeneratedStrategy;
  }

  @Override
  public String toString() {
    return "InitialRoomSetting{" +
        "name='" + name + '\'' +
        ", password='" + password + '\'' +
        ", maxParticipants=" + maxParticipants +
        ", maxSpectators=" + maxSpectators +
        ", activated=" + activated +
        ", roomRemoveMode=" + roomRemoveMode +
        ", roomCredentialValidatedStrategy=" + roomCredentialValidatedStrategy +
        ", roomPlayerSlotGeneratedStrategy=" + roomPlayerSlotGeneratedStrategy +
        '}';
  }

  /**
   * The builder class for collecting setups information.
   */
  public static class Builder extends SystemLogger {

    private String name;
    private String password;
    private int maxParticipants;
    private int maxSpectators;
    private boolean activated;
    private RoomRemoveMode removeMode;
    private RoomCredentialValidatedStrategy credentialValidatedStrategy;
    private RoomPlayerSlotGeneratedStrategy playerSlotGeneratedStrategy;

    private Builder() {
      name = null;
      password = null;
      maxParticipants = 0;
      maxSpectators = 0;
      activated = false;
      removeMode = RoomRemoveMode.DEFAULT;
      credentialValidatedStrategy = null;
      playerSlotGeneratedStrategy = null;
    }

    /**
     * Creates a new instance.
     *
     * @return a new instance of {@link Builder}
     */
    public static Builder newInstance() {
      return new Builder();
    }

    /**
     * Sets the room's name.
     *
     * @param name the {@link String} room's name
     * @return the pointer of builder
     */
    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the room's password.
     *
     * @param password the {@link String} room's password
     * @return the pointer of builder
     */
    public Builder setPassword(String password) {
      this.password = password;
      return this;
    }

    /**
     * Sets the room's maximum number of participants.
     *
     * @param maxParticipants the maximum number of participants allowed be in the room
     * @return the pointer of builder
     */
    public Builder setMaxParticipants(int maxParticipants) {
      this.maxParticipants = maxParticipants;
      return this;
    }

    /**
     * Sets the room's maximum number of spectators.
     *
     * @param maxSpectators the maximum number of spectators allowed be in the room
     * @return the pointer of builder
     */
    public Builder setMaxSpectators(int maxSpectators) {
      this.maxSpectators = maxSpectators;
      return this;
    }

    /**
     * Allows a room to be activated or not.
     *
     * @param activated set the flag's value to be {@code true} when the room is active,
     *                  otherwise returns {@code false}
     * @return the pointer of builder
     */
    public Builder setActivated(boolean activated) {
      this.activated = activated;
      return this;
    }

    /**
     * Sets removed mode for the room.
     *
     * @param roomRemoveMode the {@link RoomRemoveMode} decides rules applied to remove the room
     * @return the pointer of builder
     * @see AutoRemoveRoomTask
     */
    public Builder setRoomRemoveMode(RoomRemoveMode roomRemoveMode) {
      removeMode = roomRemoveMode;
      return this;
    }

    /**
     * Sets a strategy for validating credentials using for get in the room.
     *
     * @param clazz a class which is an implementation of {@link RoomCredentialValidatedStrategy}
     * @return the pointer of builder
     * @see DefaultRoomCredentialValidatedStrategy
     */
    public Builder setRoomCredentialValidatedStrategy(
        Class<? extends RoomCredentialValidatedStrategy> clazz) {
      credentialValidatedStrategy = (RoomCredentialValidatedStrategy) createNewInstance(clazz);
      return this;
    }

    /**
     * Sets a strategy for generating participant's slots in the room.
     *
     * @param clazz a class which is an implementation of {@link RoomPlayerSlotGeneratedStrategy}
     * @return the pointer of builder
     * @see DefaultRoomPlayerSlotGeneratedStrategy
     */
    public Builder setRoomPlayerSlotGeneratedStrategy(
        Class<? extends RoomPlayerSlotGeneratedStrategy> clazz) {
      playerSlotGeneratedStrategy = (RoomPlayerSlotGeneratedStrategy) createNewInstance(clazz);
      return this;
    }

    /**
     * Initialization.
     *
     * @return a new building instance
     */
    public InitialRoomSetting build() {
      if (Objects.isNull(credentialValidatedStrategy)) {
        credentialValidatedStrategy = (RoomCredentialValidatedStrategy) createNewInstance(
            DefaultRoomCredentialValidatedStrategy.class);
      }
      if (Objects.isNull(playerSlotGeneratedStrategy)) {
        playerSlotGeneratedStrategy = (RoomPlayerSlotGeneratedStrategy) createNewInstance(
            DefaultRoomPlayerSlotGeneratedStrategy.class);
      }
      return new InitialRoomSetting(this);
    }

    private Object createNewInstance(Class<?> clazz) {
      Object object = null;
      try {
        object = clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException
          | InvocationTargetException | NoSuchMethodException
          | SecurityException e) {
        error(e);
      }
      return object;
    }
  }
}
