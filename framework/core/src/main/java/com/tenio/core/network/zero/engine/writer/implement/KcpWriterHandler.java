/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.network.zero.engine.KcpWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * The implementation of {@link KcpWriter} using on the server.
 *
 * @since 0.3.0
 */
public class KcpWriterHandler extends SystemLogger implements KcpWriter<DatagramChannel> {

  private static final int DEFAULT_BUFFER_SIZE = 1024;

  private final DatagramChannel datagramChannel;
  private final SocketAddress remoteAddress;
  private ByteBuffer byteBuffer;

  /**
   * Creates a writer.
   *
   * @param datagramChannel the {@link DatagramChannel} using for transmitting data
   * @param remoteAddress   the {@link SocketAddress} of destination (client)
   */
  public KcpWriterHandler(DatagramChannel datagramChannel, SocketAddress remoteAddress) {
    this.datagramChannel = datagramChannel;
    this.remoteAddress = remoteAddress;
    byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
  }

  @Override
  public InetAddress getLocalAddress() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public int getLocalPort() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DatagramChannel getWriter() {
    return datagramChannel;
  }

  @Override
  public int send(byte[] binaries, int size) throws IOException {
    byteBuffer.clear();
    // buffer size is not enough, need to be allocated more bytes
    if (byteBuffer.capacity() < size) {
      if (isDebugEnabled()) {
        debug("KCP CHANNEL SEND", "Allocate new buffer from ", byteBuffer.capacity(), " to ",
            size, " bytes");
      }
      byteBuffer = ByteBuffer.allocate(size);
    }
    byteBuffer.put(binaries, 0, size);
    byteBuffer.flip();
    return getWriter().send(byteBuffer, getRemoteAddress());
  }

  @Override
  public String toString() {
    return "KcpWriterHandler{" +
        "datagramChannel=" + datagramChannel +
        ", remoteAddress=" + remoteAddress +
        '}';
  }
}
