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

package com.tenio.core.network.zero.engine.acceptor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import com.tenio.core.network.utility.SocketUtility;
import java.io.IOException;
import java.util.function.Consumer;
import org.mockito.MockedStatic;

import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For AcceptorHandler")
class AcceptorHandlerTest {

  private ConnectionFilter connectionFilter;
  private ZeroReaderListener readerListener;
  private SocketIoHandler ioHandler;
  private AcceptorHandler handler;

  private static Selector getSelector(AcceptorHandler h) throws Exception {
    Field f = AcceptorHandler.class.getDeclaredField("acceptableSelector");
    f.setAccessible(true);
    return (Selector) f.get(h);
  }

  private static int getBoundPort(Selector selector) {
    for (SelectionKey key : selector.keys()) {
      if (key.channel() instanceof ServerSocketChannel ssc) {
        return ssc.socket().getLocalPort();
      }
    }
    return -1;
  }

  @BeforeEach
  void setUp() {
    connectionFilter = mock(ConnectionFilter.class);
    readerListener = mock(ZeroReaderListener.class);
    ioHandler = mock(SocketIoHandler.class);
    handler = new AcceptorHandler("0.0.0.0", connectionFilter, readerListener,
        new SocketConfiguration("tcp", TransportType.TCP, 0, 1), ioHandler);
  }

  @AfterEach
  void tearDown() {
    try {
      handler.shutdown();
    } catch (Exception ignored) {
      // already shut down in this test
    }
  }

  @Test
  @DisplayName("Constructor creates handler without throwing")
  void testConstructorCreatesHandlerSuccessfully() {
    assertNotNull(handler);
  }

  @Test
  @DisplayName("shutdown does not throw")
  void testShutdownDoesNotThrow() {
    assertDoesNotThrow(() -> handler.shutdown());
  }

  @Test
  @DisplayName("running returns immediately when selector is woken up (0 ready keys)")
  void testRunningReturnsImmediatelyOnWakeup() throws Exception {
    Selector selector = getSelector(handler);
    selector.wakeup();
    assertDoesNotThrow(() -> handler.running());
  }

  @Test
  @DisplayName("running accepts an incoming TCP connection and delegates to reader listener")
  void testRunningAcceptsIncomingConnection() throws Exception {
    Selector selector = getSelector(handler);
    int port = getBoundPort(selector);

    SocketChannel client = SocketChannel.open();
    try {
      client.connect(new InetSocketAddress("127.0.0.1", port));
      assertDoesNotThrow(() -> handler.running());
      verify(readerListener, timeout(1000)).acceptClientSocketChannel(any(), any(), any());
    } finally {
      client.close();
    }
  }

  @Test
  @DisplayName("running handles RefusedConnectionAddressException from connection filter")
  void testRunningHandlesRefusedConnection() throws Exception {
    doThrow(new RefusedConnectionAddressException("refused", "127.0.0.1"))
        .when(connectionFilter).validateAndAddAddress(any());

    Selector selector = getSelector(handler);
    int port = getBoundPort(selector);

    SocketChannel client = SocketChannel.open();
    try {
      client.connect(new InetSocketAddress("127.0.0.1", port));
      assertDoesNotThrow(() -> handler.running());
      verify(ioHandler, timeout(1000)).channelException(any(), any());
      verify(ioHandler, timeout(1000)).channelInactive(any(), any(),
          eq(ConnectionDisconnectMode.REFUSED_CONNECTION));
    } finally {
      client.close();
    }
  }

  @Test
  @DisplayName("running handles IOException from readerListener.acceptClientSocketChannel")
  void testRunningHandlesIOExceptionFromReaderListener() throws Exception {
    doThrow(new ClosedChannelException())
        .when(readerListener).acceptClientSocketChannel(any(), any(), any());

    Selector selector = getSelector(handler);
    int port = getBoundPort(selector);

    SocketChannel client = SocketChannel.open();
    try {
      client.connect(new InetSocketAddress("127.0.0.1", port));
      assertDoesNotThrow(() -> handler.running());
      verify(ioHandler, timeout(1000)).channelInactive(any(), any(), eq(ConnectionDisconnectMode.EXCEPTION));
    } finally {
      client.close();
    }
  }

  @Test
  @DisplayName("running invokes onSuccess callback which triggers channelActive on ioHandler")
  void testRunningInvokesOnSuccessCallbackTriggeringChannelActive() throws Exception {
    doAnswer(inv -> {
      Consumer<SelectionKey> onSuccess = inv.getArgument(1);
      onSuccess.accept(mock(SelectionKey.class));
      return null;
    }).when(readerListener).acceptClientSocketChannel(any(), any(), any());

    Selector selector = getSelector(handler);
    int port = getBoundPort(selector);

    SocketChannel client = SocketChannel.open();
    try {
      client.connect(new InetSocketAddress("127.0.0.1", port));
      assertDoesNotThrow(() -> handler.running());
      verify(ioHandler, timeout(1000)).channelActive(any(), any());
    } finally {
      client.close();
    }
  }

  @Test
  @DisplayName("running invokes onFailed callback which closes the socket channel")
  void testRunningInvokesOnFailedCallbackClosingChannel() throws Exception {
    doAnswer(inv -> {
      Runnable onFailed = inv.getArgument(2);
      onFailed.run();
      return null;
    }).when(readerListener).acceptClientSocketChannel(any(), any(), any());

    Selector selector = getSelector(handler);
    int port = getBoundPort(selector);

    SocketChannel client = SocketChannel.open();
    try {
      client.connect(new InetSocketAddress("127.0.0.1", port));
      assertDoesNotThrow(() -> handler.running());
    } finally {
      client.close();
    }
  }

  @Test
  @DisplayName("shutdown catches IOException from SocketUtility.shutdownSelector")
  void testShutdownCatchesIOExceptionFromShutdownSelector() {
    try (MockedStatic<SocketUtility> socketUtilMock = mockStatic(SocketUtility.class)) {
      socketUtilMock.when(() -> SocketUtility.shutdownSelector(any()))
          .thenThrow(new IOException("selector close failed"));
      assertDoesNotThrow(() -> handler.shutdown());
    }
  }

  @Test
  @DisplayName("running onFailed callback catches IOException from closeSocket")
  void testRunningOnFailedCallbackCatchesIOException() throws Exception {
    doAnswer(inv -> {
      Runnable onFailed = inv.getArgument(2);
      try (MockedStatic<SocketUtility> socketUtilMock = mockStatic(SocketUtility.class)) {
        socketUtilMock.when(() -> SocketUtility.closeSocket(any(), any()))
            .thenThrow(new IOException("close failed"));
        onFailed.run();
      }
      return null;
    }).when(readerListener).acceptClientSocketChannel(any(), any(), any());

    Selector selector = getSelector(handler);
    int port = getBoundPort(selector);

    SocketChannel client = SocketChannel.open();
    try {
      client.connect(new InetSocketAddress("127.0.0.1", port));
      assertDoesNotThrow(() -> handler.running());
    } finally {
      client.close();
    }
  }
}
