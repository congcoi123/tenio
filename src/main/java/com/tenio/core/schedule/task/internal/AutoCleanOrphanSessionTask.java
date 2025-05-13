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

package com.tenio.core.schedule.task.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.schedule.task.AbstractSystemTask;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * For a session which is no longer associated to any player (orphan), this task
 * will scan in period time and force them to disconnect.
 *
 * @since 0.5.0
 */
public final class AutoCleanOrphanSessionTask extends AbstractSystemTask {

  private SessionManager sessionManager;

  private AutoCleanOrphanSessionTask(EventManager eventManager) {
    super(eventManager);
  }

  /**
   * Creates a new task instance.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return a new instance of {@link AutoCleanOrphanSessionTask}
   */
  public static AutoCleanOrphanSessionTask newInstance(EventManager eventManager) {
    return new AutoCleanOrphanSessionTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    var threadFactoryTask =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("auto-clean-orphan-session-task" +
            "-%d").build();
    var threadFactoryWorker =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("auto-clean-orphan-worker-%d").build();
    var executors = Executors.newCachedThreadPool(threadFactoryWorker);
    return Executors.newSingleThreadScheduledExecutor(threadFactoryTask).scheduleAtFixedRate(
        () -> {
          debug("AUTO CLEAN ORPHAN SESSION",
              "Checking orphan sessions in ", sessionManager.getSessionCount(), " entities");
          executors.execute(() -> {
            Iterator<Session> iterator = sessionManager.getReadonlySessionsList().listIterator();
            while (iterator.hasNext()) {
              Session session = iterator.next();
              if (session.isOrphan()) {
                try {
                  debug("AUTO CLEAN ORPHAN SESSION",
                      "Session ", session.getId(), " is going to be forced to remove by the cleaning task");
                  session.close(ConnectionDisconnectMode.ORPHAN, PlayerDisconnectMode.DEFAULT);
                } catch (IOException exception) {
                  error(exception, session.toString());
                }
              }
            }
          });
        }, initialDelay, interval, TimeUnit.SECONDS);
  }

  /**
   * Set the session manager.
   *
   * @param sessionManager the session manager
   */
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }
}
