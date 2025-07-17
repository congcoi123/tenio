/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.zero.engine.writer.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.SessionTicketsQueueManager;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For AbstractWriterHandler")
class AbstractWriterHandlerTest {

  private AbstractWriterHandler handler;
  private SessionTicketsQueueManager queueManager;
  private NetworkWriterStatistic writerStatistic;

  @BeforeEach
  void setUp() {
    handler = new AbstractWriterHandler() {
      @Override
      public void send(PacketQueue packetQueue, Session session, Packet packet) {
        // no-op for test
      }
    };
    queueManager = mock(SessionTicketsQueueManager.class);
    writerStatistic = mock(NetworkWriterStatistic.class);
  }

  @Test
  @DisplayName("Test session tickets queue manager setter/getter")
  void testSetAndGetSessionTicketsQueueManager() {
    BlockingQueue<Session> queue = new LinkedBlockingQueue<>();
    when(queueManager.getQueueByElementId(42L)).thenReturn(queue);
    handler.setSessionTicketsQueueManager(queueManager);
    assertEquals(queue, handler.getSessionTicketsQueue(42L));
  }

  @Test
  @DisplayName("Test network writer statistic getter/setter")
  void testSetAndGetNetworkWriterStatistic() {
    handler.setNetworkWriterStatistic(writerStatistic);
    assertEquals(writerStatistic, handler.getNetworkWriterStatistic());
  }

  @Test
  @DisplayName("Test allocating writing buffer")
  void testAllocateAndGetBuffer() {
    handler.allocateBuffer(128);
    ByteBuffer buffer = handler.getBuffer();
    assertNotNull(buffer);
    assertEquals(128, buffer.capacity());
  }
}
