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
import com.tenio.common.utility.OsUtility;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoderImpl;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.codec.encoder.BinaryPacketEncoderImpl;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.StandardSocketOptions;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Create an object for handling a Datagram socket connection. It is used to
 * send messages to a server or receive messages from that one.
 */
public final class UDP {

  private static final int DEFAULT_BYTE_BUFFER_SIZE = 10240;
  private static final String BROADCAST_ADDRESS = "0.0.0.0";
  /**
   * The desired port for listening.
   */
  private int port;
  private BinaryPacketEncoder binaryPacketEncoder;
  private BinaryPacketDecoder binaryPacketDecoder;
  private Future<?> future;
  private DatagramSocket datagramSocket;
  private InetAddress inetAddress;

  /**
   * Listen in a port on the local machine.
   *
   * @param port      the desired port
   * @param broadcast sets to {@code true} to enable broadcasting
   * @param onSuccess UDP connected successfully
   */
  public UDP(int port, boolean broadcast, Consumer<UDP> onSuccess) {
    try {
      if (broadcast) {
        datagramSocket = new DatagramSocket(port, InetAddress.getByName(BROADCAST_ADDRESS));
        datagramSocket.setBroadcast(true);
        if (OsUtility.getOperatingSystemType() == OsUtility.OsType.WINDOWS) {
          datagramSocket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        } else {
          datagramSocket.setOption(StandardSocketOptions.SO_REUSEPORT, true);
        }
      } else {
        datagramSocket = new DatagramSocket();
      }

      inetAddress = InetAddress.getLocalHost();
      this.port = port;

      var binaryCompressor = new DefaultBinaryPacketCompressor();
      var binaryEncryptor = new DefaultBinaryPacketEncryptor();

      binaryPacketEncoder = new BinaryPacketEncoderImpl();
      binaryPacketEncoder.setCompressor(binaryCompressor);
      binaryPacketEncoder.setEncryptor(binaryEncryptor);

      binaryPacketDecoder = new BinaryPacketDecoderImpl();
      binaryPacketDecoder.setCompressor(binaryCompressor);
      binaryPacketDecoder.setEncryptor(binaryEncryptor);

      onSuccess.accept(this);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Listen in a port on the local machine.
   *
   * @param port      the desired port
   * @param onSuccess UDP connected successfully
   */
  public UDP(int port, Consumer<UDP> onSuccess) {
    this(port, false, onSuccess);
  }

  public int getLocalPort() {
    return datagramSocket.getLocalPort();
  }

  public InetAddress getLocalAddress() {
    return inetAddress;
  }

  public int getRemotePort() {
    return port;
  }

  public DatagramSocket getDatagramSocket() {
    return datagramSocket;
  }

  /**
   * Send a message to the server.
   *
   * @param message the desired message
   */
  public void send(DataCollection message) {
    // convert message object to bytes data
    var packet = PacketImpl.newInstance();
    packet.setDataType(message.getType());
    packet.setData(message.toBinaries());
    packet = binaryPacketEncoder.encode(packet);
    var sendingPacket = packet.getData();
    var request = new DatagramPacket(sendingPacket, sendingPacket.length, inetAddress, port);
    try {
      datagramSocket.send(request);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Listen to messages that came from the server.
   *
   * @param listener instance of {@link DatagramListener}
   */
  public void receive(DatagramListener listener) {
    var executorService = Executors.newSingleThreadExecutor();
    future = executorService.submit(() -> {
      var binaries = new byte[DEFAULT_BYTE_BUFFER_SIZE];
      while (true) {
        try {
          var response = new DatagramPacket(binaries, binaries.length);
          datagramSocket.receive(response);
          listener.onReceivedUDP(binaryPacketDecoder.decode(binaries));
        } catch (IOException exception) {
          exception.printStackTrace();
          return;
        }
      }
    });
  }

  /**
   * Close this connection.
   */
  public void close() {
    datagramSocket.close();
    future.cancel(true);
  }
}
