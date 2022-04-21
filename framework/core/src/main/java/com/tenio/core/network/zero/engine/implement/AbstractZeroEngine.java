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

package com.tenio.core.network.zero.engine.implement;

import com.tenio.common.utility.StringUtility;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ServiceRuntimeException;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.zero.engine.ZeroEngine;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The abstract engine.
 */
public abstract class AbstractZeroEngine extends AbstractManager implements ZeroEngine, Runnable {

  private static final int DEFAULT_NUMBER_WORKERS = 5;
  private static final int DEFAULT_BUFFER_SIZE = 1024;

  private final AtomicInteger id;
  private String name;

  private ExecutorService executorService;
  private int executorSize;
  private int bufferSize;

  private SocketIoHandler socketIoHandler;
  private DatagramIoHandler datagramIoHandler;
  private SessionManager sessionManager;

  private volatile boolean activated;

  /**
   * Initialization.
   *
   * @param eventManager the event manger
   */
  protected AbstractZeroEngine(EventManager eventManager) {
    super(eventManager);

    executorSize = DEFAULT_NUMBER_WORKERS;
    bufferSize = DEFAULT_BUFFER_SIZE;
    activated = false;
    id = new AtomicInteger();
  }

  private void initializeWorkers() {
    executorService = Executors.newFixedThreadPool(executorSize);
    for (int i = 0; i < executorSize; i++) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException e) {
        error(e);
      }
      executorService.execute(this);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (Objects.nonNull(executorService) && !executorService.isShutdown()) {
        try {
          halting();
        } catch (Exception e) {
          error(e);
        }
      }
    }));
  }

  private void halting() throws ServiceRuntimeException {
    activated = false;

    onShutdown();

    executorService.shutdown();

    try {
      if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        destroyEngine();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      destroyEngine();
    }
  }

  private void destroyEngine() {
    info("STOPPED SERVICE", buildgen("zero-", getName(), " (", executorSize, ")"));
    onDestroyed();
    info("DESTROYED SERVICE", buildgen("zero-", getName(), " (", executorSize, ")"));
  }

  @Override
  public void run() {
    setThreadName();
    onRunning();
  }

  private void setThreadName() {
    Thread.currentThread()
        .setName(StringUtility.strgen("zero-", getName(), "-", id.incrementAndGet()));
  }

  @Override
  public SocketIoHandler getSocketIoHandler() {
    return socketIoHandler;
  }

  @Override
  public void setSocketIoHandler(SocketIoHandler socketIoHandler) {
    this.socketIoHandler = socketIoHandler;
  }

  @Override
  public DatagramIoHandler getDatagramIoHandler() {
    return datagramIoHandler;
  }

  @Override
  public void setDatagramIoHandler(DatagramIoHandler datagramIoHandler) {
    this.datagramIoHandler = datagramIoHandler;
  }

  @Override
  public SessionManager getSessionManager() {
    return sessionManager;
  }

  @Override
  public void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
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
  public int getMaxBufferSize() {
    return bufferSize;
  }

  @Override
  public void setMaxBufferSize(int maxSize) {
    bufferSize = maxSize;
  }

  @Override
  public void initialize() {
    initializeWorkers();
    onInitialized();
  }

  @Override
  public void start() {
    onStarted();
    activated = true;
    info("START SERVICE", buildgen("zero-", getName(), " (", executorSize, ")"));
  }

  @Override
  public void shutdown() {
    halting();
  }

  public boolean isActivated() {
    return activated;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}
