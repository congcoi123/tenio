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

import io.netty.channel.Channel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * The socket utility class.
 *
 * @since 0.6.6
 */
public final class SocketUtility {

  private SocketUtility() {
    throw new UnsupportedOperationException();
  }

  /**
   * Closes a socket channel and its related info.
   *
   * @param socketChannel the {@link SocketChannel}
   * @param selectionKey  the {@link SelectionKey}, selected for the socket channel by a selector
   * @throws IOException whenever there is any exception occurred
   */
  public static void closeSocket(SocketChannel socketChannel, SelectionKey selectionKey)
      throws IOException {
    if (socketChannel != null) {
      if (selectionKey != null) {
        selectionKey.cancel();
      }
      if (socketChannel.isOpen()) {
        var socket = socketChannel.socket();
        if (socket != null) {
          socket.shutdownInput();
          socket.shutdownOutput();
        }
        socketChannel.close();
      }
    }
  }

  /**
   * Closes a socket channel.
   *
   * @param channel the {@link Channel}
   */
  public static void closeSocket(Channel channel) {
    if (channel != null) {
      channel.close();
    }
  }

  /**
   * Creates a byte buffer object to hold data reading from sockets. This should be DIRECT type
   * (created from OS) to prevent any conversion.
   *
   * @param size the buffer size
   * @return an instance of {@link ByteBuffer}
   */
  public static ByteBuffer createReaderBuffer(int size) {
    // Default read buffer is DIRECT
    return ByteBuffer.allocateDirect(size);
  }

  /**
   * Creates a byte buffer object to hold data writing to sockets.
   *
   * @param size the buffer size
   * @return an instance of {@link ByteBuffer}
   */
  public static ByteBuffer createWriterBuffer(int size) {
    // Default write buffer is HEAP
    return ByteBuffer.allocate(size);
  }
}
