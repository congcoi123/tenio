/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

import com.tenio.common.data.utility.ZeroUtility;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.implement.SessionImpl;
import com.tenio.core.network.zero.codec.compression.DefaultBinaryPacketCompressor;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.DefaultBinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encoder.DefaultBinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encryption.DefaultBinaryPacketEncryptor;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Create an object for handling a socket connection. It is used to send
 * messages to a server or receive messages from that one.
 */
public final class TCP implements PacketDecoderResultListener {

  private static final int DEFAULT_BYTE_BUFFER_SIZE = 10240;
  private static final String LOCAL_HOST = "localhost";

  private SocketListener socketListener;
  private Future<?> future;
  private Socket socket;
  private DataOutputStream dataOutputStream;
  private DataInputStream dataInputStream;
  private ByteArrayOutputStream byteArrayOutputStream;
  private Session session;
  private BinaryPacketEncoder binaryPacketEncoder;
  private BinaryPacketDecoder binaryPacketDecoder;

  /**
   * Listen in a port on the local machine.
   *
   * @param port the desired port
   */
  public TCP(int port) {
    try {
      socket = new Socket(LOCAL_HOST, port);
      dataOutputStream = new DataOutputStream(socket.getOutputStream());
      dataInputStream = new DataInputStream(socket.getInputStream());
      byteArrayOutputStream = new ByteArrayOutputStream();

      session = SessionImpl.newInstance();
      session.createPacketSocketHandler();

      var binaryCompressor = new DefaultBinaryPacketCompressor();
      var binaryEncryptor = new DefaultBinaryPacketEncryptor();

      binaryPacketEncoder = new DefaultBinaryPacketEncoder();
      binaryPacketEncoder.setCompressor(binaryCompressor);
      binaryPacketEncoder.setEncryptor(binaryEncryptor);

      binaryPacketDecoder = new DefaultBinaryPacketDecoder();
      binaryPacketDecoder.setCompressor(binaryCompressor);
      binaryPacketDecoder.setEncryptor(binaryEncryptor);
      binaryPacketDecoder.setResultListener(this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Send a message to the server.
   *
   * @param message the desired message
   */
  public void send(ServerMessage message) {
    // convert message object to bytes data
    var packet = PacketImpl.newInstance();
    packet.setData(message.getData().toBinary());
    packet = binaryPacketEncoder.encode(packet);
    // attach the packet's length to packet's header
    var bytes = packet.getData();
    try {
      dataOutputStream.write(bytes);
      dataOutputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Listen for messages that came from the server.
   *
   * @param listener the socket listener
   */
  public void receive(SocketListener listener) {
    socketListener = listener;
    var executorService = Executors.newSingleThreadExecutor();
    future = executorService.submit(() -> {
      var binary = new byte[DEFAULT_BYTE_BUFFER_SIZE];
      int readBytes;
      try {
        while ((readBytes = dataInputStream.read(binary, 0, binary.length)) != -1) {
          byteArrayOutputStream.reset();
          byteArrayOutputStream.write(binary, 0, readBytes);
          binaryPacketDecoder.decode(session, byteArrayOutputStream.toByteArray());
        }
      } catch (IOException | RuntimeException e) {
        e.printStackTrace();
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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void resultFrame(Session session, byte[] binary) {
    var data = ZeroUtility.binaryToMap(binary);
    socketListener.onReceivedTCP(ServerMessage.newInstance().setData(data));
  }

  @Override
  public void updateReadDroppedPackets(long numberPackets) {
    // do nothing
  }

  @Override
  public void updateReadPackets(long numberPackets) {
    // do nothing
  }
}
