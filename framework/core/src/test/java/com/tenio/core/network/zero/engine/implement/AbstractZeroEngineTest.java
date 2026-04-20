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

package com.tenio.core.network.zero.engine.implement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For AbstractZeroEngine")
class AbstractZeroEngineTest {

  private AbstractZeroEngine engine;

  @BeforeEach
  void setUp() {
    // ZeroAcceptorImpl is the simplest concrete subclass with no extra workers
    engine = (AbstractZeroEngine) ZeroAcceptorImpl.newInstance(mock(EventManager.class));
  }

  @Test
  @DisplayName("Default thread pool size is 5")
  void testDefaultThreadPoolSizeIsFive() {
    assertEquals(5, engine.getThreadPoolSize());
  }

  @Test
  @DisplayName("Default buffer size is 1024")
  void testDefaultBufferSizeIs1024() {
    assertEquals(1024, engine.getMaxBufferSize());
  }

  @Test
  @DisplayName("setThreadPoolSize and getThreadPoolSize work correctly")
  void testSetAndGetThreadPoolSize() {
    engine.setThreadPoolSize(10);
    assertEquals(10, engine.getThreadPoolSize());
  }

  @Test
  @DisplayName("setMaxBufferSize and getMaxBufferSize work correctly")
  void testSetAndGetMaxBufferSize() {
    engine.setMaxBufferSize(4096);
    assertEquals(4096, engine.getMaxBufferSize());
  }

  @Test
  @DisplayName("getName returns 'acceptor' for ZeroAcceptorImpl")
  void testGetNameReturnsAcceptor() {
    assertEquals("acceptor", engine.getName());
  }

  @Test
  @DisplayName("setName and getName work correctly")
  void testSetAndGetName() {
    engine.setName("custom-engine");
    assertEquals("custom-engine", engine.getName());
  }

  @Test
  @DisplayName("isActivated is false before activate() is called")
  void testIsActivatedIsFalseInitially() {
    assertFalse(engine.isActivated());
  }

  @Test
  @DisplayName("activate() sets activated to true")
  void testActivateSetsActivatedToTrue() {
    engine.activate();
    assertTrue(engine.isActivated());
  }

  @Test
  @DisplayName("setSocketIoHandler and getSocketIoHandler work correctly")
  void testSetAndGetSocketIoHandler() {
    SocketIoHandler handler = mock(SocketIoHandler.class);
    engine.setSocketIoHandler(handler);
    assertEquals(handler, engine.getSocketIoHandler());
  }

  @Test
  @DisplayName("setDatagramIoHandler and getDatagramIoHandler work correctly")
  void testSetAndGetDatagramIoHandler() {
    DatagramIoHandler handler = mock(DatagramIoHandler.class);
    engine.setDatagramIoHandler(handler);
    assertEquals(handler, engine.getDatagramIoHandler());
  }

  @Test
  @DisplayName("setSessionManager and getSessionManager work correctly")
  void testSetAndGetSessionManager() {
    SessionManager sessionManager = mock(SessionManager.class);
    engine.setSessionManager(sessionManager);
    assertEquals(sessionManager, engine.getSessionManager());
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 0 for ZeroAcceptorImpl")
  void testGetNumberOfExtraWorkersReturnsZero() {
    assertEquals(0, engine.getNumberOfExtraWorkers());
  }

  @Test
  @DisplayName("initialize() then shutdown() completes without exception")
  void testInitializeThenShutdown() {
    assertDoesNotThrow(() -> {
      engine.initialize();
      engine.shutdown();
    });
  }

  @Test
  @DisplayName("double shutdown() after initialize() — second call is a no-op")
  void testDoubleShutdownAfterInitialize() {
    assertDoesNotThrow(() -> {
      engine.initialize();
      engine.shutdown();
      engine.shutdown(); // second call: stopping flag already set, should return immediately
    });
  }

  @Test
  @DisplayName("start() then shutdown() with empty onRunning covers run/configureThread/halting")
  void testStartThenShutdownWithMinimalEngine() throws InterruptedException {
    AbstractZeroEngine minimalEngine = new AbstractZeroEngine(mock(EventManager.class)) {
      @Override
      public void onInitialized() {}

      @Override
      public void onStarted() {}

      @Override
      public void onRunning() {} // returns immediately so the virtual thread finishes quickly

      @Override
      public void onShutdown() {}

      @Override
      public void onDestroyed() {}
    };
    minimalEngine.setName("test-engine");
    minimalEngine.setThreadPoolSize(1);
    minimalEngine.initialize();
    minimalEngine.start();
    Thread.sleep(50); // give the virtual thread time to finish onRunning()
    minimalEngine.shutdown();
  }

  @Test
  @DisplayName("run(Runnable, String) executes extra worker task when within limit")
  void testRunWithExtraWorkerExecutesTask() throws InterruptedException {
    AbstractZeroEngine engineWithExtra = new AbstractZeroEngine(mock(EventManager.class)) {
      @Override
      public void onInitialized() {}

      @Override
      public void onStarted() {}

      @Override
      public void onRunning() {}

      @Override
      public void onShutdown() {}

      @Override
      public void onDestroyed() {}

      @Override
      public int getNumberOfExtraWorkers() {
        return 1;
      }
    };
    engineWithExtra.setName("extra-engine");
    engineWithExtra.setThreadPoolSize(2); // must be > extra workers (1)
    engineWithExtra.initialize();

    java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
    assertDoesNotThrow(() -> engineWithExtra.run(latch::countDown, "extra"));

    latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
    engineWithExtra.shutdown();
  }

  @Test
  @DisplayName("run(Runnable, String) throws IllegalArgumentException when extra worker limit exceeded")
  void testRunWithExtraWorkerThrowsWhenLimitExceeded() {
    AbstractZeroEngine engineNoExtra = new AbstractZeroEngine(mock(EventManager.class)) {
      @Override
      public void onInitialized() {}

      @Override
      public void onStarted() {}

      @Override
      public void onRunning() {}

      @Override
      public void onShutdown() {}

      @Override
      public void onDestroyed() {}
    }; // getNumberOfExtraWorkers() returns 0 by default
    engineNoExtra.setName("no-extra-engine");
    engineNoExtra.initialize();

    assertThrows(IllegalArgumentException.class,
        () -> engineNoExtra.run(() -> {}, "test-postfix"));
    engineNoExtra.shutdown();
  }
}
