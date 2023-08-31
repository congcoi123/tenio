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

package com.tenio.core.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.RequestQueueFullException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entity.protocol.Request;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The abstracted class for a controller.
 */
public abstract class AbstractController extends AbstractManager implements Controller, Runnable {

  private static final int DEFAULT_MAX_QUEUE_SIZE = 50;
  private static final int DEFAULT_NUMBER_WORKERS = 5;

  private final AtomicInteger id;
  private String name;

  private ExecutorService executorService;
  private int executorSize;

  private BlockingQueue<Request> requestQueue;

  private int maxQueueSize;

  private boolean initialized;
  private boolean stopping;
  private volatile boolean activated;

  /**
   * Initialization.
   *
   * @param eventManager the {@link EventManager}
   */
  protected AbstractController(EventManager eventManager) {
    super(eventManager);

    maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
    executorSize = DEFAULT_NUMBER_WORKERS;
    activated = false;
    stopping = false;
    initialized = false;
    id = new AtomicInteger();
  }

  private void initializeWorkers() {
    var requestComparator = RequestComparator.newInstance();
    requestQueue = new PriorityBlockingQueue<>(maxQueueSize, requestComparator);

    var threadFactory = new ThreadFactoryBuilder().setDaemon(true).build();
    executorService = Executors.newFixedThreadPool(executorSize, threadFactory);
    for (int i = 0; i < executorSize; i++) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException exception) {
        if (isErrorEnabled()) {
          error(exception);
        }
      }
      executorService.execute(this);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (Objects.nonNull(executorService) && !executorService.isShutdown()) {
        try {
          attemptToShutdown();
        } catch (Exception exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }
    }));
  }

  private void attemptToShutdown() {
    if (stopping) {
      return;
    }

    stopping = true;
    activated = false;

    if (isInfoEnabled()) {
      info("STOPPING SERVICE", buildgen("controller-", getName(), " (", executorSize, ")"));
    }
    executorService.shutdown();

    try {
      if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        destroyController();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      destroyController();
    }
  }

  @Override
  public void run() {
    setThreadName();
    processing();
  }

  private void processing() {
    while (true) {
      if (activated) {
        try {
          var request = requestQueue.take();
          processRequest(request);
        } catch (Throwable cause) {
          if (isErrorEnabled()) {
            error(cause);
          }
        }
      }
    }
  }

  private void destroy() {
    requestQueue.clear();
    onDestroyed();
  }

  private void setThreadName() {
    Thread currentThread = Thread.currentThread();
    currentThread.setName(StringUtility.strgen("controller-", getName(), "-", id.incrementAndGet()));
    currentThread.setUncaughtExceptionHandler((thread, cause) -> {
      if (isErrorEnabled()) {
        error(cause, thread.getName());
      }
    });
  }

  private void destroyController() {
    if (isInfoEnabled()) {
      info("STOPPING SERVICE", buildgen("controller-", getName(), " (", executorSize, ")"));
    }
    destroy();
    if (isInfoEnabled()) {
      info("DESTROYED SERVICE", buildgen("controller-", getName(), " (", executorSize, ")"));
    }
  }

  @Override
  public void initialize() {
    initializeWorkers();
    initialized = true;
  }

  @Override
  public void start() {
    activated = true;
    if (isInfoEnabled()) {
      info("START SERVICE", buildgen("controller-", getName(), " (", executorSize, ")"));
    }
  }

  @Override
  public void shutdown() {
    if (!initialized) {
      return;
    }
    attemptToShutdown();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean isActivated() {
    return activated;
  }

  @Override
  public void enqueueRequest(Request request) {
    if (requestQueue.size() >= maxQueueSize) {
      var exception = new RequestQueueFullException(requestQueue.size());
      if (isErrorEnabled()) {
        error(exception, exception.getMessage());
      }
      throw exception;
    }
    requestQueue.add(request);
  }

  @Override
  public int getMaxRequestQueueSize() {
    return maxQueueSize;
  }

  @Override
  public void setMaxRequestQueueSize(int maxSize) {
    maxQueueSize = maxSize;
  }

  @Override
  public int getThreadPoolSize() {
    return executorSize;
  }

  @Override
  public void setThreadPoolSize(int maxSize) {
    executorSize = maxSize;
  }

  @Override
  public float getPercentageUsedRequestQueue() {
    return maxQueueSize == 0 ? 0.0f :
        (float) (requestQueue.size() * 100) / (float) maxQueueSize;
  }

  /**
   * Subscribe all events for handling.
   */
  public abstract void subscribe();

  /**
   * Processes a request.
   *
   * @param request the processing {@link Request}
   */
  public abstract void processRequest(Request request);
}
