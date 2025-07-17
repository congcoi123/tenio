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

package com.tenio.core.network.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.channel.Channel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SocketUtility")
class SocketUtilityTest {

  @Test
  @DisplayName("Test closing socket channel and its selection key")
  void testCloseSocketChannelAndSelectionKey() throws IOException {
    SocketChannel socketChannel = mock(SocketChannel.class);
    SelectionKey selectionKey = mock(SelectionKey.class);
    when(socketChannel.isOpen()).thenReturn(true);
    when(socketChannel.socket()).thenReturn(mock(java.net.Socket.class));
    doNothing().when(socketChannel).close();
    SocketUtility.closeSocket(socketChannel, selectionKey);
    verify(selectionKey).cancel();
    verify(socketChannel).close();
  }

  @Test
  @DisplayName("Close a null socket channel should not do anything")
  void testCloseSocketChannelNulls() throws IOException {
    // Should not throw
    SocketUtility.closeSocket(null, null);
  }

  @Test
  @DisplayName("Test closing netty channel")
  void testCloseSocketNettyChannel() {
    Channel channel = mock(Channel.class);
    SocketUtility.closeSocket(channel);
    verify(channel).close();
  }

  @Test
  @DisplayName("Close a null netty channel should not do anything")
  void testCloseSocketNettyChannelNull() {
    // Should not throw
    SocketUtility.closeSocket(null);
  }

  @Test
  @DisplayName("Test creating a DIRECT reader buffer")
  void testCreateReaderBuffer() {
    ByteBuffer buffer = SocketUtility.createReaderBuffer(128);
    assertNotNull(buffer);
    assertTrue(buffer.isDirect());
    assertEquals(128, buffer.capacity());
  }

  @Test
  @DisplayName("Test creating a HEAP writer buffer")
  void testCreateWriterBuffer() {
    ByteBuffer buffer = SocketUtility.createWriterBuffer(256);
    assertNotNull(buffer);
    assertFalse(buffer.isDirect());
    assertEquals(256, buffer.capacity());
  }
}
