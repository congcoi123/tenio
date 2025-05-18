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

package com.tenio.core.schedule.task.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.schedule.task.AbstractSystemTask;
import com.tenio.core.server.ServerImpl;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * For a player which is in IDLE mode, that means for a long time without
 * receiving or sending any data from the server or from a client. This task
 * will scan those IDLE players in period time and force them to log out. Those
 * players got a "timeout" error.
 */
public final class AutoDisconnectPlayerTask extends AbstractSystemTask {

  private PlayerManager playerManager;

  private AutoDisconnectPlayerTask(EventManager eventManager) {
    super(eventManager);
  }

  /**
   * Creates a new task instance.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return a new instance of {@link AutoDisconnectPlayerTask}
   */
  public static AutoDisconnectPlayerTask newInstance(EventManager eventManager) {
    return new AutoDisconnectPlayerTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    var threadFactoryTask =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("auto-disconnect-player-task-%d")
            .build();
    var threadFactoryWorker =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("auto-disconnect-player-worker" +
            "-%d").build();
    var executors = Executors.newCachedThreadPool(threadFactoryWorker);
    return Executors.newSingleThreadScheduledExecutor(threadFactoryTask).scheduleAtFixedRate(
        () -> {
          debug("AUTO DISCONNECT PLAYER",
              "Checking IDLE players in ", playerManager.getPlayerCount(), " entities");
          executors.execute(() -> {
            Iterator<Player> iterator = playerManager.getReadonlyPlayersList().listIterator();
            while (iterator.hasNext()) {
              Player player = iterator.next();
              if (player.isNeverDeported()) {
                if (player.isIdleNeverDeported()) {
                  debug("AUTO DISCONNECT PLAYER",
                      player.getIdentity(),
                      " (never deported) is going to be forced to remove by the cleaning task");
                  ServerImpl.getInstance().getApi().logout(player, ConnectionDisconnectMode.IDLE,
                      PlayerDisconnectMode.IDLE);
                }
              } else {
                if (player.isIdle()) {
                  debug("AUTO DISCONNECT PLAYER",
                      player.getIdentity(), " is going to be forced to remove by the cleaning task");
                  ServerImpl.getInstance().getApi().logout(player, ConnectionDisconnectMode.IDLE,
                      PlayerDisconnectMode.IDLE);
                }
              }
            }
          });
        }, initialDelay, interval, TimeUnit.SECONDS);
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
