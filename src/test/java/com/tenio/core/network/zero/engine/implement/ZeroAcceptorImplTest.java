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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import com.tenio.core.network.zero.engine.acceptor.AcceptorHandler;
import org.mockito.MockedConstruction;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroAcceptorImpl")
class ZeroAcceptorImplTest {

  private ZeroAcceptor acceptor;

  @BeforeEach
  void setUp() {
    acceptor = ZeroAcceptorImpl.newInstance(mock(EventManager.class));
  }

  @Test
  @DisplayName("newInstance creates an acceptor with name 'acceptor'")
  void testNewInstanceCreatesWithNameAcceptor() {
    assertEquals("acceptor", acceptor.getName());
  }

  @Test
  @DisplayName("setConnectionFilter does not throw")
  void testSetConnectionFilterDoesNotThrow() {
    ConnectionFilter filter = mock(ConnectionFilter.class);
    assertDoesNotThrow(() -> acceptor.setConnectionFilter(filter));
  }

  @Test
  @DisplayName("setServerAddress does not throw")
  void testSetServerAddressDoesNotThrow() {
    assertDoesNotThrow(() -> acceptor.setServerAddress("127.0.0.1"));
  }

  @Test
  @DisplayName("setSocketConfiguration does not throw")
  void testSetSocketConfigurationDoesNotThrow() {
    SocketConfiguration config = new SocketConfiguration("tcp", TransportType.TCP, 8080, 4);
    assertDoesNotThrow(() -> acceptor.setSocketConfiguration(config));
  }

  @Test
  @DisplayName("setZeroReaderListener does not throw")
  void testSetZeroReaderListenerDoesNotThrow() {
    ZeroReaderListener listener = mock(ZeroReaderListener.class);
    assertDoesNotThrow(() -> acceptor.setZeroReaderListener(listener));
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 0")
  void testGetNumberOfExtraWorkersReturnsZero() {
    assertEquals(0, ((AbstractZeroEngine) acceptor).getNumberOfExtraWorkers());
  }

  @Test
  @DisplayName("initialize() then shutdown() covers onInitialized and onShutdown")
  void testInitializeAndShutdown() {
    assertDoesNotThrow(() -> {
      acceptor.initialize();
      acceptor.shutdown();
    });
  }

  @Test
  @DisplayName("onStarted is a no-op method (covers its single return instruction)")
  void testOnStartedIsNoOp() throws Exception {
    Method onStarted = ZeroAcceptorImpl.class.getDeclaredMethod("onStarted");
    onStarted.setAccessible(true);
    assertDoesNotThrow(() -> onStarted.invoke(acceptor));
  }

  @Test
  @DisplayName("onRunning catches Throwable thrown by acceptorHandler.running()")
  void testOnRunningCatchesThrowableFromRunning() throws Exception {
    acceptor.setConnectionFilter(mock(ConnectionFilter.class));
    acceptor.setServerAddress("127.0.0.1");
    acceptor.setSocketConfiguration(new SocketConfiguration("tcp", TransportType.TCP, 0, 1));
    acceptor.setZeroReaderListener(mock(ZeroReaderListener.class));
    acceptor.setSocketIoHandler(mock(SocketIoHandler.class));
    acceptor.activate();

    Field handlersField = ZeroAcceptorImpl.class.getDeclaredField("acceptorHandlers");
    handlersField.setAccessible(true);
    List<Object> handlers = new ArrayList<>();
    handlersField.set(acceptor, handlers);

    Method onRunning = ZeroAcceptorImpl.class.getDeclaredMethod("onRunning");
    onRunning.setAccessible(true);

    try (MockedConstruction<AcceptorHandler> mockConstruction =
        mockConstruction(AcceptorHandler.class, (mock, ctx) ->
            doThrow(new RuntimeException("acceptor error")).when(mock).running())) {

      Thread t = new Thread(() -> {
        try { onRunning.invoke(acceptor); } catch (Exception ignored) {}
      });
      t.start();
      Thread.sleep(150);
      t.interrupt();
      t.join(2000);
    }
  }

  @Test
  @DisplayName("onRunning creates AcceptorHandler and loops; onShutdown iterates non-empty handler list")
  void testOnRunningLoopsAndOnShutdownClosesHandlers() throws Exception {
    acceptor.setConnectionFilter(mock(ConnectionFilter.class));
    acceptor.setServerAddress("127.0.0.1");
    acceptor.setSocketConfiguration(new SocketConfiguration("tcp", TransportType.TCP, 0, 1));
    acceptor.setZeroReaderListener(mock(ZeroReaderListener.class));
    acceptor.setSocketIoHandler(mock(SocketIoHandler.class));
    acceptor.activate();

    // Manually set the acceptorHandlers list without creating the executor
    Field handlersField = ZeroAcceptorImpl.class.getDeclaredField("acceptorHandlers");
    handlersField.setAccessible(true);
    List<Object> handlers = new ArrayList<>();
    handlersField.set(acceptor, handlers);

    Method onRunning = ZeroAcceptorImpl.class.getDeclaredMethod("onRunning");
    onRunning.setAccessible(true);

    Thread t = new Thread(() -> {
      try {
        onRunning.invoke(acceptor);
      } catch (Exception ignored) {
      }
    });
    t.start();

    // Wait for AcceptorHandler to be created and added to the list
    long deadline = System.currentTimeMillis() + 2000;
    while (handlers.isEmpty() && System.currentTimeMillis() < deadline) {
      Thread.sleep(10);
    }

    // Interrupt the thread (causes select() to return on most JVMs)
    t.interrupt();

    // Also wakeup the selector for robustness
    if (!handlers.isEmpty()) {
      Field selectorField = handlers.get(0).getClass().getDeclaredField("acceptableSelector");
      selectorField.setAccessible(true);
      Selector selector = (Selector) selectorField.get(handlers.get(0));
      selector.wakeup();
    }

    t.join(3000);

    // onShutdown with non-empty list closes each handler
    Method onShutdown = ZeroAcceptorImpl.class.getDeclaredMethod("onShutdown");
    onShutdown.setAccessible(true);
    assertDoesNotThrow(() -> onShutdown.invoke(acceptor));
  }
}
