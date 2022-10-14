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

package com.tenio.core.schedule.task.kcp;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.schedule.task.AbstractTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This task takes responsibility to update every KCP channel frequently.
 * @since 0.3.0
 */
public final class KcpUpdateTask extends AbstractTask {

  private SessionManager sessionManager;

  private KcpUpdateTask(EventManager eventManager) {
    super(eventManager);
  }

  public static KcpUpdateTask newInstance(EventManager eventManager) {
    return new KcpUpdateTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    ExecutorService workers = Executors.newFixedThreadPool(Runtime.getRuntime()
        .availableProcessors() * 2);
    return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        () -> {
          var iterator = sessionManager.getSessionIterator();
          while (iterator.hasNext()) {
            var session = iterator.next();
            if (session.containsKcp()) {
              workers.execute(() -> {
                var ukcp = session.getUkcp();
                ukcp.update();
                ukcp.receive();
              });
            }
          }
        }, 0, 10, TimeUnit.MILLISECONDS);
  }

  /**
   * Sets a session manager to be able to update all managed sessions.
   *
   * @param sessionManager an instance of {@link SessionManager}
   */
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }
}
