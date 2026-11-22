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

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.common.logger.SystemLogger;
import java.io.IOException;
import java.util.Iterator;

/**
 * For a session which is no longer associated to any player (orphan), this task
 * will scan in period time and force them to disconnect.
 *
 * @since 0.5.0
 */
public final class AutoCleanOrphanSessionTask extends SystemLogger {

  private SessionManager sessionManager;

  /** Runs one cleanup pass. */
  public void scan() {
    Iterator<Session> iterator = sessionManager.getSnapshotSessionsList().listIterator();
    while (iterator.hasNext()) {
      Session session = iterator.next();
      // Since v0.7.0, it doesn't need to check whether the session is activated
      if (session.isOrphan()) {
        try {
          if (isDebugEnabled()) {
            debug("AUTO CLEAN ORPHAN SESSION",
                "Session ", session.getId(), " is going to be forced to remove by the cleaning task");
          }
          session.close(ConnectionDisconnectMode.ORPHAN, PlayerDisconnectMode.CONNECTION_LOST);
        } catch (IOException exception) {
          if (isErrorEnabled()) {
            error(exception, session.toString());
          }
        } finally {
          // A terminated session cannot emit SESSION_WILL_BE_CLOSED again. Remove it
          // directly so it cannot remain in the manager and be selected forever.
          session.remove();
        }
      }
    }
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
