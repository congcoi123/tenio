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

package com.tenio.examples.client;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoderImpl;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoderImpl;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.handler.frame.BinaryPacketFramer;
import com.tenio.core.network.zero.handler.frame.PacketFramingListener;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Create an object for handling a socket connection. It is used to send
 * messages to a server or receive messages from that one.
 */
public final class TCP implements PacketFramingListener {

  private static final int DEFAULT_BYTE_BUFFER_SIZE = 10240;
  private static final String LOCAL_HOST = "localhost";

  private SocketListener socketListener;
  private Future<?> future;
  private Socket socket;
  private DataOutputStream dataOutputStream;
  private DataInputStream dataInputStream;
  private ByteArrayOutputStream byteArrayOutputStream;
  private Session session;
  private BinaryPacketFramer binaryPacketFramer;
  private BinaryPacketEncoder binaryPacketEncoder;

  /**
   * Listen in a port on the local machine.
   *
   * @param port      the desired port
   * @param onSuccess TCP connected successfully
   */
  public TCP(int port, Consumer<TCP> onSuccess) {
    try {
      socket = new Socket(LOCAL_HOST, port);
      dataOutputStream = new DataOutputStream(socket.getOutputStream());
      dataInputStream = new DataInputStream(socket.getInputStream());
      byteArrayOutputStream = new ByteArrayOutputStream();

      session = new CustomSession();

      var binaryCompressor = new DefaultBinaryPacketCompressor();
      var binaryEncryptor = new DefaultBinaryPacketEncryptor();

      binaryPacketEncoder = new BinaryPacketEncoderImpl();
      binaryPacketEncoder.setCompressor(binaryCompressor);
      binaryPacketEncoder.setEncryptor(binaryEncryptor);

      BinaryPacketDecoder binaryPacketDecoder = new BinaryPacketDecoderImpl();
      binaryPacketDecoder.setCompressor(binaryCompressor);
      binaryPacketDecoder.setEncryptor(binaryEncryptor);

      binaryPacketFramer = new BinaryPacketFramer();
      binaryPacketFramer.setBinaryPacketDecoder(binaryPacketDecoder);
      binaryPacketFramer.setPacketFramingResult(this);

      onSuccess.accept(this);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public Session getSession() {
    return session;
  }

  /**
   * Send a message to the server.
   *
   * @param message the desired message
   */
  public void send(DataCollection message) {
    // convert message object to binaries data
    var packet = PacketImpl.newInstance();
    packet.setDataType(message.getType());
    packet.setData(message.toBinaries());
    packet.needsDataCounting(true);
    packet = binaryPacketEncoder.encode(packet);
    // attach the packet's length to packet's header
    var binaries = packet.getData();
    try {
      dataOutputStream.write(binaries);
      dataOutputStream.flush();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Listen to messages that came from the server.
   *
   * @param listener the socket listener
   */
  public void receive(SocketListener listener) {
    socketListener = listener;
    var executorService = Executors.newSingleThreadExecutor();
    future = executorService.submit(() -> {
      var binaries = new byte[DEFAULT_BYTE_BUFFER_SIZE];
      int readBytes;
      try {
        while ((readBytes = dataInputStream.read(binaries, 0, binaries.length)) != -1) {
          byteArrayOutputStream.reset();
          byteArrayOutputStream.write(binaries, 0, readBytes);
          binaryPacketFramer.framing(session, byteArrayOutputStream.toByteArray());
        }
      } catch (IOException | RuntimeException exception) {
        exception.printStackTrace();
      }
    });
  }

  /**
   * Close this connection.
   */
  public void close() {
    try {
      socket.close();
      future.cancel(true);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public void onFramedResult(Session session, DataCollection message) {
    socketListener.onReceivedTCP(message);
  }
}
