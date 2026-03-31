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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
  @DisplayName("getMaximumStartingTimeInMilliseconds equals threadPoolSize multiplied by delay constant")
  void testGetMaximumStartingTimeInMilliseconds() {
    int threadPoolSize = engine.getThreadPoolSize();
    // Delay constant is 100 ms per worker
    assertEquals(threadPoolSize * 100, engine.getMaximumStartingTimeInMilliseconds());
  }
}
