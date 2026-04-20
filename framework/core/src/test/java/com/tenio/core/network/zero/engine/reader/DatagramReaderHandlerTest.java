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

package com.tenio.core.network.zero.engine.reader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For DatagramReaderHandler")
class DatagramReaderHandlerTest {

  private DatagramReaderHandler handler;

  @BeforeEach
  void setUp() throws IOException {
    handler = new DatagramReaderHandler(
        ByteBuffer.allocate(512),
        mock(SessionManager.class),
        mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class),
        mock(DatagramIoHandler.class),
        mock(DatagramPacketPolicy.class)
    );
  }

  @Test
  @DisplayName("Constructor creates a non-null handler")
  void testConstructorCreatesHandler() throws IOException {
    assertNotNull(handler);
    handler.shutdown();
  }

  @Test
  @DisplayName("shutdown closes the selector without throwing")
  void testShutdownDoesNotThrow() {
    assertDoesNotThrow(() -> handler.shutdown());
  }

  @Test
  @DisplayName("openDatagramChannels with cacheSize zero throws IllegalArgumentException")
  void testOpenDatagramChannelsWithZeroCacheSizeThrows() throws IOException {
    assertThrows(IllegalArgumentException.class,
        () -> handler.openDatagramChannels("127.0.0.1", 19999, 0));
    handler.shutdown();
  }

  @Test
  @DisplayName("openDatagramChannels with negative cacheSize throws IllegalArgumentException")
  void testOpenDatagramChannelsWithNegativeCacheSizeThrows() throws IOException {
    assertThrows(IllegalArgumentException.class,
        () -> handler.openDatagramChannels("127.0.0.1", 19999, -1));
    handler.shutdown();
  }

  @Test
  @DisplayName("openDatagramChannels with valid cacheSize binds and registers channels without throwing")
  void testOpenDatagramChannelsWithValidCacheSizeDoesNotThrow() throws IOException {
    assertDoesNotThrow(() -> handler.openDatagramChannels("127.0.0.1", 0, 1));
    handler.shutdown();
  }

  @Test
  @DisplayName("running() returns immediately when selector is woken up (0 ready keys)")
  void testRunningWithWakeupReturnsImmediately() throws Exception {
    Field f = DatagramReaderHandler.class.getDeclaredField("readableSelector");
    f.setAccessible(true);
    Selector selector = (Selector) f.get(handler);
    selector.wakeup();

    assertDoesNotThrow(() -> handler.running());
    handler.shutdown();
  }

  @Test
  @DisplayName("running() reads UDP data and calls datagramIoHandler.channelRead when no session found")
  void testRunningWithRealUdpDataCallsChannelRead() throws Exception {
    DatagramIoHandler ioHandler = mock(DatagramIoHandler.class);
    DatagramPacketPolicy policy = mock(DatagramPacketPolicy.class);
    SessionManager sessionManager = mock(SessionManager.class);
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    NetworkReaderStatistic statistic = mock(NetworkReaderStatistic.class);
    DataCollection dataCollection = mock(DataCollection.class);

    when(decoder.decode(any(byte[].class))).thenReturn(dataCollection);
    when(policy.applyPolicy(dataCollection)).thenReturn(Pair.of(0, dataCollection));
    when(sessionManager.getSessionByDatagram(0)).thenReturn(null);

    DatagramReaderHandler udpHandler = new DatagramReaderHandler(
        ByteBuffer.allocate(512), sessionManager, decoder, statistic, ioHandler, policy);
    udpHandler.openDatagramChannels("127.0.0.1", 0, 1);

    Field f = DatagramReaderHandler.class.getDeclaredField("readableSelector");
    f.setAccessible(true);
    Selector selector = (Selector) f.get(udpHandler);
    int port = -1;
    for (SelectionKey key : selector.keys()) {
      if (key.channel() instanceof DatagramChannel dc) {
        port = ((InetSocketAddress) dc.getLocalAddress()).getPort();
        break;
      }
    }

    DatagramChannel sender = DatagramChannel.open();
    sender.send(ByteBuffer.wrap(new byte[]{1, 2, 3}), new InetSocketAddress("127.0.0.1", port));
    sender.close();

    udpHandler.running();

    verify(ioHandler).channelRead(any(), any(), any());
    udpHandler.shutdown();
  }

  private DatagramReaderHandler createUdpHandler(DatagramIoHandler ioHandler,
      DatagramPacketPolicy policy, SessionManager sessionManager,
      BinaryPacketDecoder decoder, NetworkReaderStatistic statistic,
      DataCollection dataCollection, int conveyId) throws Exception {
    when(decoder.decode(any(byte[].class))).thenReturn(dataCollection);
    when(policy.applyPolicy(dataCollection)).thenReturn(Pair.of(conveyId, dataCollection));
    DatagramReaderHandler udpHandler = new DatagramReaderHandler(
        ByteBuffer.allocate(512), sessionManager, decoder, statistic, ioHandler, policy);
    udpHandler.openDatagramChannels("127.0.0.1", 0, 1);
    return udpHandler;
  }

  private int getUdpPort(DatagramReaderHandler udpHandler) throws Exception {
    Field f = DatagramReaderHandler.class.getDeclaredField("readableSelector");
    f.setAccessible(true);
    Selector selector = (Selector) f.get(udpHandler);
    for (SelectionKey key : selector.keys()) {
      if (key.channel() instanceof DatagramChannel dc) {
        return ((InetSocketAddress) dc.getLocalAddress()).getPort();
      }
    }
    return -1;
  }

  @Test
  @DisplayName("running() reads UDP data and calls datagramIoHandler.sessionRead when activated session found")
  void testRunningWithActivatedSessionCallsSessionRead() throws Exception {
    DatagramIoHandler ioHandler = mock(DatagramIoHandler.class);
    DatagramPacketPolicy policy = mock(DatagramPacketPolicy.class);
    SessionManager sessionManager = mock(SessionManager.class);
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    NetworkReaderStatistic statistic = mock(NetworkReaderStatistic.class);
    DataCollection dataCollection = mock(DataCollection.class);
    Session session = mock(Session.class);

    DatagramReaderHandler udpHandler = createUdpHandler(
        ioHandler, policy, sessionManager, decoder, statistic, dataCollection, 42);
    when(sessionManager.getSessionByDatagram(42)).thenReturn(session);
    when(session.isActivated()).thenReturn(true);

    int port = getUdpPort(udpHandler);
    DatagramChannel sender = DatagramChannel.open();
    sender.send(ByteBuffer.wrap(new byte[]{1, 2, 3}), new InetSocketAddress("127.0.0.1", port));
    sender.close();

    udpHandler.running();

    verify(ioHandler).sessionRead(session, dataCollection);
    udpHandler.shutdown();
  }

  @Test
  @DisplayName("running() reads UDP data and skips sessionRead when session is inactive")
  void testRunningWithInactiveSessionSkipsSessionRead() throws Exception {
    DatagramIoHandler ioHandler = mock(DatagramIoHandler.class);
    DatagramPacketPolicy policy = mock(DatagramPacketPolicy.class);
    SessionManager sessionManager = mock(SessionManager.class);
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    NetworkReaderStatistic statistic = mock(NetworkReaderStatistic.class);
    DataCollection dataCollection = mock(DataCollection.class);
    Session session = mock(Session.class);

    DatagramReaderHandler udpHandler = createUdpHandler(
        ioHandler, policy, sessionManager, decoder, statistic, dataCollection, 99);
    when(sessionManager.getSessionByDatagram(99)).thenReturn(session);
    when(session.isActivated()).thenReturn(false);

    int port = getUdpPort(udpHandler);
    DatagramChannel sender = DatagramChannel.open();
    sender.send(ByteBuffer.wrap(new byte[]{1, 2, 3}), new InetSocketAddress("127.0.0.1", port));
    sender.close();

    udpHandler.running();

    verify(ioHandler, never()).sessionRead(any(), any());
    udpHandler.shutdown();
  }

  @Test
  @DisplayName("readUpdData with IOException from receive calls channelException")
  void testReadUpdDataWithIOExceptionFromReceiveCallsChannelException() throws Exception {
    DatagramIoHandler ioHandler = mock(DatagramIoHandler.class);
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);

    when(selectionKey.isValid()).thenReturn(true);
    when(selectionKey.isReadable()).thenReturn(true);
    when(datagramChannel.receive(any(ByteBuffer.class))).thenThrow(new IOException("receive failed"));

    DatagramReaderHandler h = new DatagramReaderHandler(
        ByteBuffer.allocate(512), mock(SessionManager.class), mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class), ioHandler, mock(DatagramPacketPolicy.class));

    Method m = DatagramReaderHandler.class.getDeclaredMethod(
        "readUpdData", DatagramChannel.class, SelectionKey.class, ByteBuffer.class);
    m.setAccessible(true);
    m.invoke(h, datagramChannel, selectionKey, ByteBuffer.allocate(512));

    verify(ioHandler).channelException(eq(datagramChannel), any(IOException.class));
    h.shutdown();
  }

  @Test
  @DisplayName("readUpdData with null remoteAddress calls channelException")
  void testReadUpdDataWithNullRemoteAddressCallsChannelException() throws Exception {
    DatagramIoHandler ioHandler = mock(DatagramIoHandler.class);
    DatagramChannel datagramChannel = mock(DatagramChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);

    when(selectionKey.isValid()).thenReturn(true);
    when(selectionKey.isReadable()).thenReturn(true);
    when(datagramChannel.receive(any(ByteBuffer.class))).thenReturn(null);

    DatagramReaderHandler h = new DatagramReaderHandler(
        ByteBuffer.allocate(512), mock(SessionManager.class), mock(BinaryPacketDecoder.class),
        mock(NetworkReaderStatistic.class), ioHandler, mock(DatagramPacketPolicy.class));

    Method m = DatagramReaderHandler.class.getDeclaredMethod(
        "readUpdData", DatagramChannel.class, SelectionKey.class, ByteBuffer.class);
    m.setAccessible(true);
    m.invoke(h, datagramChannel, selectionKey, ByteBuffer.allocate(512));

    verify(ioHandler).channelException(eq(datagramChannel), any(RuntimeException.class));
    h.shutdown();
  }
}
