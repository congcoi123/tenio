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

package com.tenio.core.exception;

import com.tenio.core.entity.Player;
import java.io.Serial;

/**
 * When a player is added into a room which is already existed.
 */
public final class AddedDuplicatedPlayerException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1303721781114060707L;

  /**
   * The player instance.
   */
  private final Player player;

  /**
   * Initialization.
   *
   * @param player the {@link Player} tries to join the room
   */
  public AddedDuplicatedPlayerException(Player player) {
    super(String.format("Unable to add player: %s, it already exists", player.getIdentity()));
    this.player = player;
  }

  /**
   * Retrieves the player.
   *
   * @return an instance of {@link Player}
   */
  public Player getPlayer() {
    return player;
  }
}
