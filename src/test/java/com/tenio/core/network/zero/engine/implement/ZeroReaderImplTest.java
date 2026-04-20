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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import java.io.IOException;
import org.mockito.MockedConstruction;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.engine.reader.DatagramReaderHandler;
import com.tenio.core.network.zero.engine.reader.SocketReaderHandler;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroReaderImpl")
class ZeroReaderImplTest {

  private ZeroReader reader;

  @BeforeEach
  void setUp() {
    reader = ZeroReaderImpl.newInstance(mock(EventManager.class));
  }

  @Test
  @DisplayName("newInstance creates a reader with name 'reader'")
  void testNewInstanceCreatesWithNameReader() {
    assertEquals("reader", reader.getName());
  }

  @Test
  @DisplayName("getNetworkReaderStatistic is null before being set")
  void testGetNetworkReaderStatisticIsNullBeforeSet() {
    assertNull(reader.getNetworkReaderStatistic());
  }

  @Test
  @DisplayName("setNetworkReaderStatistic and getNetworkReaderStatistic work correctly")
  void testSetAndGetNetworkReaderStatistic() {
    NetworkReaderStatistic statistic = mock(NetworkReaderStatistic.class);
    reader.setNetworkReaderStatistic(statistic);
    assertEquals(statistic, reader.getNetworkReaderStatistic());
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 0 when no UDP config is set")
  void testGetNumberOfExtraWorkersReturnsZeroWithoutUdpConfig() {
    assertEquals(0, ((AbstractZeroEngine) reader).getNumberOfExtraWorkers());
  }

  @Test
  @DisplayName("getNumberOfExtraWorkers returns 1 after UDP config is set")
  void testGetNumberOfExtraWorkersReturnsOneWithUdpConfig() {
    reader.setUdpChannelConfiguration(
        new SocketConfiguration("udp", TransportType.UDP, 8081, 4));
    assertEquals(1, ((AbstractZeroEngine) reader).getNumberOfExtraWorkers());
  }

  @Test
  @DisplayName("setServerAddress does not throw")
  void testSetServerAddressDoesNotThrow() {
    assertDoesNotThrow(() -> reader.setServerAddress("0.0.0.0"));
  }

  @Test
  @DisplayName("setUdpChannelConfiguration does not throw")
  void testSetUdpChannelConfigurationDoesNotThrow() {
    SocketConfiguration config = new SocketConfiguration("udp", TransportType.UDP, 9000, 2);
    assertDoesNotThrow(() -> reader.setUdpChannelConfiguration(config));
  }

  @Test
  @DisplayName("setDatagramPacketPolicy does not throw")
  void testSetDatagramPacketPolicyDoesNotThrow() {
    DatagramPacketPolicy policy = mock(DatagramPacketPolicy.class);
    assertDoesNotThrow(() -> reader.setDatagramPacketPolicy(policy));
  }

  @Test
  @DisplayName("initialize() then shutdown() covers onInitialized and onShutdown")
  void testInitializeThenShutdown() {
    // onInitialized() creates socketReaderHandlers list (no UDP config → no datagram handler)
    // onShutdown() iterates the empty list (no-op) and skips null datagram handler
    assertDoesNotThrow(() -> {
      reader.initialize();
      reader.shutdown();
    });
  }

  @Test
  @DisplayName("onInitialized with UDP config creates datagramReaderHandler; onShutdown closes it")
  void testOnInitializedWithUdpConfigCreatesDatagramHandler() throws Exception {
    reader.setServerAddress("127.0.0.1");
    reader.setUdpChannelConfiguration(new SocketConfiguration("udp", TransportType.UDP, 0, 1));
    SocketIoHandler socketIoHandler = mock(SocketIoHandler.class);
    when(socketIoHandler.getPacketDecoder()).thenReturn(mock(BinaryPacketDecoder.class));
    reader.setSocketIoHandler(socketIoHandler);
    reader.setDatagramIoHandler(mock(DatagramIoHandler.class));
    reader.setNetworkReaderStatistic(mock(NetworkReaderStatistic.class));
    reader.setDatagramPacketPolicy(mock(DatagramPacketPolicy.class));
    reader.setSessionManager(mock(SessionManager.class));

    // Manually initialize socketReaderHandlers list (avoid creating executor)
    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    socketReadersField.set(reader, new ArrayList<>());

    Method onInitialized = ZeroReaderImpl.class.getDeclaredMethod("onInitialized");
    onInitialized.setAccessible(true);
    assertDoesNotThrow(() -> onInitialized.invoke(reader));

    // onShutdown covers the datagramReaderHandler != null branch
    Method onShutdown = ZeroReaderImpl.class.getDeclaredMethod("onShutdown");
    onShutdown.setAccessible(true);
    assertDoesNotThrow(() -> onShutdown.invoke(reader));
  }

  @Test
  @DisplayName("onRunning creates SocketReaderHandler and loops when activated; exits on interrupt")
  void testOnRunningCreatesSocketReaderHandlerAndLoopsUntilInterrupted() throws Exception {
    reader.setSocketIoHandler(mock(SocketIoHandler.class));
    reader.setSessionManager(mock(SessionManager.class));
    reader.setNetworkReaderStatistic(mock(NetworkReaderStatistic.class));
    reader.setThreadPoolSize(1);
    reader.activate();

    // Initialize socketReaderHandlers list via reflection (avoid creating executor)
    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    List<Object> handlers = new ArrayList<>();
    socketReadersField.set(reader, handlers);

    Method onRunning = ZeroReaderImpl.class.getDeclaredMethod("onRunning");
    onRunning.setAccessible(true);

    Thread t = new Thread(() -> {
      try {
        onRunning.invoke(reader);
      } catch (Exception ignored) {
      }
    });
    t.start();

    // Wait for the SocketReaderHandler to be added to the list
    long deadline = System.currentTimeMillis() + 2000;
    while (handlers.isEmpty() && System.currentTimeMillis() < deadline) {
      Thread.sleep(10);
    }

    // Interrupt the thread; also wakeup its selector for robustness
    t.interrupt();
    if (!handlers.isEmpty()) {
      Field selectorField = handlers.get(0).getClass().getDeclaredField("readableSelector");
      selectorField.setAccessible(true);
      ((Selector) selectorField.get(handlers.get(0))).wakeup();
    }
    t.join(3000);
  }

  @Test
  @DisplayName("onStarted with datagramReaderHandler runs lambda until thread is interrupted")
  void testOnStartedWithDatagramHandlerRunsLambdaUntilInterrupted() throws Exception {
    // Inject executor service so run() can submit the lambda without calling initialize()
    Field executorField = AbstractZeroEngine.class.getDeclaredField("executorService");
    executorField.setAccessible(true);
    ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
    executorField.set(reader, exec);

    // Pre-set socketReaderHandlers to avoid NPE
    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    socketReadersField.set(reader, new ArrayList<>());

    // Set UDP config so getNumberOfExtraWorkers() = 1 (required by run())
    reader.setUdpChannelConfiguration(new SocketConfiguration("udp", TransportType.UDP, 0, 1));

    // Inject mock DatagramReaderHandler that interrupts the thread when running() is called
    DatagramReaderHandler mockDatagram = mock(DatagramReaderHandler.class);
    doAnswer(inv -> {
      Thread.currentThread().interrupt();
      return null;
    }).when(mockDatagram).running();

    Field datagramField = ZeroReaderImpl.class.getDeclaredField("datagramReaderHandler");
    datagramField.setAccessible(true);
    datagramField.set(reader, mockDatagram);

    reader.activate();

    Method onStarted = ZeroReaderImpl.class.getDeclaredMethod("onStarted");
    onStarted.setAccessible(true);
    assertDoesNotThrow(() -> onStarted.invoke(reader));

    // Wait for the lambda thread to run and exit
    Thread.sleep(500);
    exec.shutdownNow();

    verify(mockDatagram).running();
  }

  @Test
  @DisplayName("onShutdown with non-empty socketReaderHandlers calls shutdown on each handler")
  void testOnShutdownWithNonEmptySocketReaderHandlers() throws Exception {
    reader.initialize();

    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    List<SocketReaderHandler> handlers = (List<SocketReaderHandler>) socketReadersField.get(reader);

    SocketReaderHandler mockHandler = mock(SocketReaderHandler.class);
    handlers.add(mockHandler);

    reader.shutdown();

    verify(mockHandler).shutdown();
  }

  @Test
  @DisplayName("acceptClientSocketChannel delegates to the SocketReaderHandler in the list")
  void testAcceptClientSocketChannelDelegatesCorrectly() throws Exception {
    reader.setThreadPoolSize(1);

    // Create a real SocketReaderHandler and populate the list
    SocketReaderHandler socketReaderHandler = new SocketReaderHandler(
        ByteBuffer.allocate(256), mock(SessionManager.class),
        mock(NetworkReaderStatistic.class), mock(SocketIoHandler.class));

    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    List<SocketReaderHandler> handlers = new ArrayList<>();
    handlers.add(socketReaderHandler);
    socketReadersField.set(reader, handlers);

    SocketChannel channel = SocketChannel.open();
    try {
      ZeroReaderListener readerListener = (ZeroReaderListener) reader;
      assertDoesNotThrow(() -> readerListener.acceptClientSocketChannel(channel, mock(Consumer.class),
          mock(Runnable.class)));
    } finally {
      channel.close();
      socketReaderHandler.shutdown();
    }
  }

  @Test
  @DisplayName("onRunning catches IOException thrown by socketReaderHandler.running()")
  void testOnRunningCatchesIOExceptionFromSocketReaderHandler() throws Exception {
    reader.setSocketIoHandler(mock(SocketIoHandler.class));
    reader.setSessionManager(mock(SessionManager.class));
    reader.setNetworkReaderStatistic(mock(NetworkReaderStatistic.class));
    reader.setThreadPoolSize(1);
    reader.activate();

    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    socketReadersField.set(reader, new ArrayList<>());

    Method onRunning = ZeroReaderImpl.class.getDeclaredMethod("onRunning");
    onRunning.setAccessible(true);

    try (MockedConstruction<SocketReaderHandler> mockConstruction =
        mockConstruction(SocketReaderHandler.class, (mock, ctx) ->
            doThrow(new IOException("reader error")).when(mock).running())) {

      Thread t = new Thread(() -> {
        try { onRunning.invoke(reader); } catch (Exception ignored) {}
      });
      t.start();
      Thread.sleep(150);
      t.interrupt();
      t.join(2000);
    }
  }

  @Test
  @DisplayName("shutdown catches InterruptedException from awaitTermination in halting()")
  void testShutdownHandlesInterruptedExceptionInHalting() {
    reader.initialize();
    Thread.currentThread().interrupt();
    try {
      reader.shutdown();
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  @DisplayName("onShutdown catches IOException thrown by datagramReaderHandler.shutdown()")
  void testOnShutdownCatchesIOExceptionFromDatagramHandler() throws Exception {
    DatagramReaderHandler mockDatagram = mock(DatagramReaderHandler.class);
    doThrow(new java.io.IOException("shutdown failed")).when(mockDatagram).shutdown();

    Field datagramField = ZeroReaderImpl.class.getDeclaredField("datagramReaderHandler");
    datagramField.setAccessible(true);
    datagramField.set(reader, mockDatagram);

    Field socketReadersField = ZeroReaderImpl.class.getDeclaredField("socketReaderHandlers");
    socketReadersField.setAccessible(true);
    socketReadersField.set(reader, new ArrayList<>());

    Method onShutdown = ZeroReaderImpl.class.getDeclaredMethod("onShutdown");
    onShutdown.setAccessible(true);
    assertDoesNotThrow(() -> onShutdown.invoke(reader));

    verify(mockDatagram).shutdown();
  }
}
