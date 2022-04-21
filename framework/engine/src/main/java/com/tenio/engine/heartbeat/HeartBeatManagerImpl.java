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

package com.tenio.engine.heartbeat;

import com.tenio.common.logger.SystemLogger;
import com.tenio.engine.exception.HeartbeatNotFoundException;
import com.tenio.engine.message.ExtraMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The Java ExecutorService is a construct that allows you to pass a task to be
 * executed by a thread asynchronously. The executor service creates and
 * maintains a reusable pool of threads for executing submitted tasks. This
 * class helps you create and manage your HeartBeats. See:
 * {@link AbstractHeartBeat}
 *
 * @see HeartBeatManager
 */
@ThreadSafe
public final class HeartBeatManagerImpl extends SystemLogger implements HeartBeatManager {

  @GuardedBy("this")
  private final Map<String, Future<Void>> threadsManager;
  /**
   * A Set is used as the container for the delayed messages because of the
   * benefit of automatic sorting and avoidance of duplicates. Messages are sorted
   * by their dispatch time. @see {@link HeartbeatMessage}
   */
  @GuardedBy("this")
  private final Map<String, TreeSet<HeartbeatMessage>> messagesManager;
  private ExecutorService executorService;

  public HeartBeatManagerImpl() {
    threadsManager = new HashMap<>();
    messagesManager = new HashMap<>();
  }

  @Override
  public void initialize(final int maxHeartbeat) throws Exception {
    executorService = Executors.newFixedThreadPool(maxHeartbeat);
    info("INITIALIZE HEART BEAT", buildgen(maxHeartbeat));
  }

  @Override
  public synchronized void create(final String id, final AbstractHeartBeat heartbeat) {
    try {
      info("CREATE HEART BEAT", buildgen("id: ", id));
      // Add the listener
      var listener = new TreeSet<HeartbeatMessage>();
      heartbeat.setMessageListener(listener);
      messagesManager.put(id, listener);
      // Start the heart-beat
      var future = executorService.submit(heartbeat);
      threadsManager.put(id, future);
    } catch (Exception e) {
      error(e, "id: ", id);
    }
  }

  @Override
  public synchronized void dispose(final String id) {
    try {
      if (!threadsManager.containsKey(id)) {
        throw new HeartbeatNotFoundException();
      }

      var future = threadsManager.get(id);
      if (Objects.isNull(future)) {
        throw new NullPointerException();
      }

      future.cancel(true);
      threadsManager.remove(id);

      info("DISPOSE HEART BEAT", buildgen(id));

      // Remove the listener
      messagesManager.get(id).clear();
      messagesManager.remove(id);

    } catch (Exception e) {
      error(e, "id: ", id);
    }
  }

  @Override
  public synchronized boolean contains(final String id) {
    return threadsManager.containsKey(id);
  }

  @Override
  public synchronized void clear() {
    if (Objects.nonNull(executorService)) {
      executorService.shutdownNow();
    }
    executorService = null;
    threadsManager.clear();
  }

  @Override
  public void sendMessage(String id, ExtraMessage message, double delayTime) {
    var container = HeartbeatMessage.newInstance(message, delayTime);
    var treeSet = messagesManager.get(id);
    synchronized (treeSet) {
      treeSet.add(container);
    }
  }

  @Override
  public void sendMessage(String id, ExtraMessage message) {
    sendMessage(id, message, 0);
  }
}
