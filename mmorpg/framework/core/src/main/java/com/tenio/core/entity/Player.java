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

package com.tenio.core.entity;

import com.tenio.core.network.entity.session.Session;

/**
 * The abstract player object used in the server.
 */
public interface Player {

  /**
   * Retrieve the player's name. This value must be unique.
   *
   * @return the player's name in (@link String}
   */
  String getName();

  boolean containsSession();

  boolean isState(PlayerState state);

  PlayerState getState();

  void setState(PlayerState state);

  boolean isActivated();

  void setActivated(boolean activated);

  boolean isLoggedIn();

  void setLoggedIn(boolean loggedIn);

  long getLastLoggedInTime();

  Session getSession();

  void setSession(Session session);

  boolean isInRoom();

  boolean isSpectator();

  void setSpectator(boolean isSpectator);

  Room getCurrentRoom();

  void setCurrentRoom(Room room);

  long getLastJoinedRoomTime();

  int getPlayerSlotInCurrentRoom();

  void setPlayerSlotInCurrentRoom(int slot);

  Object getProperty(String key);

  void setProperty(String key, Object value);

  boolean containsProperty(String key);

  void removeProperty(String key);

  void clearProperties();

  void clean();
}
