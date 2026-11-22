/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.scheduler.task;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.server.ServerImpl;
import java.util.Iterator;

/**
 * For a player which is in IDLE mode, that means for a long time without
 * receiving or sending any data from the server or from a client. This task
 * will scan those IDLE players in period time and force them to log out. Those
 * players got a "timeout" error.
 */
public final class AutoDisconnectPlayerTask extends SystemLogger {

  private PlayerManager playerManager;

  /** Runs one cleanup pass. */
  public void scan() {
    Iterator<Player> iterator = playerManager.getSnapshotPlayersList().listIterator();
    while (iterator.hasNext()) {
      Player player = iterator.next();
      if (!player.containsSession()) {
        if (player.isReconnectGracePeriodExpired()) {
          if (isDebugEnabled()) {
            debug("AUTO DISCONNECT PLAYER",
                player.getIdentity(),
                " exceeded the reconnect grace period and is going to be removed > ", player);
          }
          ServerImpl.getInstance().getApi().logout(player, ConnectionDisconnectMode.IDLE,
              PlayerDisconnectMode.IDLE);
        }
      }
    }
  }

  /**
   * Set the player manager.
   *
   * @param playerManager the player manager
   */
  public void setPlayerManager(PlayerManager playerManager) {
    this.playerManager = playerManager;
  }
}
