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
import static org.mockito.Mockito.mock;

import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.reader.policy.DatagramPacketPolicy;
import com.tenio.core.network.zero.handler.DatagramIoHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
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
}
