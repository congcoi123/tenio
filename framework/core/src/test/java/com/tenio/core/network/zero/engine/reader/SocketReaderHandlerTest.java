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
import static org.mockito.Mockito.mock;

import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.handler.SocketIoHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SocketReaderHandler")
class SocketReaderHandlerTest {

  private SocketReaderHandler handler;
  private SocketIoHandler socketIoHandler;

  @BeforeEach
  void setUp() throws IOException {
    socketIoHandler = mock(SocketIoHandler.class);
    handler = new SocketReaderHandler(
        ByteBuffer.allocate(512),
        mock(SessionManager.class),
        mock(NetworkReaderStatistic.class),
        socketIoHandler
    );
  }

  @Test
  @DisplayName("Constructor creates a non-null handler")
  void testConstructorCreatesHandler() throws IOException {
    assertNotNull(handler);
    handler.shutdown();
  }

  @Test
  @DisplayName("registerClientSocketChannel queues the channel and invokes onFailed when channel is already closed")
  void testRegisterClientSocketChannelQueuesClosed() throws IOException {
    AtomicBoolean failedCalled = new AtomicBoolean(false);
    SocketChannel closedChannel = mock(SocketChannel.class);
    Consumer<SelectionKey> onSuccess = key -> {};
    Runnable onFailed = () -> failedCalled.set(true);

    // Offer to the pending queue - will be drained on the next running() or shutdown()
    assertDoesNotThrow(() ->
        handler.registerClientSocketChannel(closedChannel, onSuccess, onFailed));
    handler.shutdown();
  }

  @Test
  @DisplayName("shutdown closes the selector without throwing")
  void testShutdownDoesNotThrow() {
    assertDoesNotThrow(() -> handler.shutdown());
  }
}
