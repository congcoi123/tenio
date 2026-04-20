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

package com.tenio.core.network.zero;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.ZeroWriter;
import java.lang.reflect.Field;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ZeroSocketImpl")
class ZeroSocketImplTest {

  private ZeroSocket socket;

  @BeforeEach
  void setUp() {
    socket = ZeroSocketImpl.newInstance(EventManager.newInstance());
  }

  @Test
  @DisplayName("newInstance returns a ZeroSocket")
  void testNewInstanceIsZeroSocket() {
    assertInstanceOf(ZeroSocket.class, socket);
  }

  @Test
  @DisplayName("getName returns 'zero-socket'")
  void testGetName() {
    assertEquals("zero-socket", socket.getName());
  }

  @Test
  @DisplayName("setName throws UnsupportedOperationException")
  void testSetNameThrows() {
    assertThrows(UnsupportedOperationException.class, () -> socket.setName("custom"));
  }

  @Test
  @DisplayName("isActivated throws UnsupportedOperationException")
  void testIsActivatedThrows() {
    assertThrows(UnsupportedOperationException.class, () -> socket.isActivated());
  }

  @Test
  @DisplayName("start before initialize is a no-op and does not throw")
  void testStartBeforeInitializeIsNoOp() {
    assertDoesNotThrow(() -> socket.start());
  }

  @Test
  @DisplayName("shutdown before initialize is a no-op and does not throw")
  void testShutdownBeforeInitializeIsNoOp() {
    assertDoesNotThrow(() -> socket.shutdown());
  }

  @Test
  @DisplayName("getMaximumStartingTimeInMilliseconds is non-negative")
  void testMaxStartingTime() {
    socket.initialize();
    assertDoesNotThrow(() -> {
      int t = socket.getMaximumStartingTimeInMilliseconds();
      assert t >= 0;
    });
    socket.shutdown();
  }

  @Test
  @DisplayName("All setter methods delegate to internal engines without throwing")
  void testAllSettersDelegateWithoutThrowing() {
    socket.initialize();
    assertDoesNotThrow(() -> {
      socket.setAcceptorServerAddress("localhost");
      socket.setAcceptorBufferSize(4096);
      socket.setAcceptorWorkerSize(2);
      socket.setReaderBufferSize(2048);
      socket.setReaderWorkerSize(2);
      socket.setWriterBufferSize(1024);
      socket.setWriterWorkerSize(2);
      socket.setConnectionFilter(mock(ConnectionFilter.class));
      socket.setSessionManager(mock(SessionManager.class));
      socket.setNetworkReaderStatistic(mock(NetworkReaderStatistic.class));
      socket.setNetworkWriterStatistic(mock(NetworkWriterStatistic.class));
      socket.setSocketConfigurations(
          new SocketConfiguration("tcp", TransportType.TCP, 9090, 8),
          new SocketConfiguration("udp", TransportType.UDP, 9091, 4));
      socket.setPacketEncoder(mock(BinaryPacketEncoder.class));
      socket.setPacketDecoder(mock(BinaryPacketDecoder.class));
      socket.setDatagramPacketPolicy(mock(DatagramPacketPolicy.class));
    });
    socket.shutdown();
  }

  @Test
  @DisplayName("activate delegates to all internal engines without throwing")
  void testActivateDoesNotThrow() {
    socket.initialize();
    assertDoesNotThrow(() -> socket.activate());
    socket.shutdown();
  }

  @Test
  @DisplayName("write with null recipients is a no-op and does not throw")
  void testWriteWithNullRecipientsIsNoOp() {
    socket.initialize();
    Packet packet = mock(Packet.class);
    when(packet.getRecipients()).thenReturn(null);
    assertDoesNotThrow(() -> socket.write(packet));
    socket.shutdown();
  }

  @Test
  @DisplayName("start after initialize calls reader, writer and acceptor start")
  void testStartAfterInitializeCallsSubComponentStarts() throws Exception {
    socket.initialize();

    ZeroReader mockReader = mock(ZeroReader.class);
    ZeroWriter mockWriter = mock(ZeroWriter.class);
    ZeroAcceptor mockAcceptor = mock(ZeroAcceptor.class);

    Field readerField = ZeroSocketImpl.class.getDeclaredField("reader");
    readerField.setAccessible(true);
    readerField.set(socket, mockReader);

    Field writerField = ZeroSocketImpl.class.getDeclaredField("writer");
    writerField.setAccessible(true);
    writerField.set(socket, mockWriter);

    Field acceptorField = ZeroSocketImpl.class.getDeclaredField("acceptor");
    acceptorField.setAccessible(true);
    acceptorField.set(socket, mockAcceptor);

    socket.start();

    verify(mockReader).start();
    verify(mockWriter).start();
    verify(mockAcceptor).start();

    socket.shutdown();
  }
}
