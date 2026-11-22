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

package com.tenio.examples.client;

import com.tenio.common.data.DataCollection;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Peer-specific UDP transport. UDP registration and convey IDs belong to application code. */
public final class UDP implements AutoCloseable {

  private static final int MAX_DATAGRAM = 65507;

  private final DatagramSocket socket;
  private final InetAddress remoteAddress;
  private final int remotePort;
  private final ClientCodec codec = new ClientCodec();
  private final AtomicBoolean receiving = new AtomicBoolean();
  private volatile Consumer<Throwable> errorHandler = Throwable::printStackTrace;

  public UDP(String host, int port) throws IOException {
    remoteAddress = InetAddress.getByName(host);
    remotePort = port;
    socket = new DatagramSocket(null);
    try {
      socket.bind(new InetSocketAddress(0));
      socket.connect(remoteAddress, remotePort); // Filters packets from unrelated peers.
    } catch (IOException | RuntimeException exception) {
      socket.close();
      throw exception;
    }
  }

  public UDP(int port, Consumer<UDP> onSuccess) {
    this(port, false, onSuccess);
  }

  /** Compatibility adapter; the boolean retains the old local broadcast-listener behavior. */
  public UDP(int port, boolean broadcast, Consumer<UDP> onSuccess) {
    try {
      remoteAddress = InetAddress.getLoopbackAddress();
      remotePort = port;
      socket = new DatagramSocket(null);
      try {
        socket.setReuseAddress(broadcast);
        socket.setBroadcast(broadcast);
        socket.bind(new InetSocketAddress(broadcast ? port : 0));
        if (!broadcast) {
          socket.connect(remoteAddress, remotePort);
        }
      } catch (IOException | RuntimeException exception) {
        socket.close();
        throw exception;
      }
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    try {
      Objects.requireNonNull(onSuccess).accept(this);
    } catch (RuntimeException exception) {
      close();
      throw exception;
    }
  }

  public int getLocalPort() {
    return socket.getLocalPort();
  }

  public InetAddress getLocalAddress() {
    return socket.getLocalAddress();
  }

  public InetAddress getServerAddress() {
    return remoteAddress;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public DatagramSocket getDatagramSocket() {
    return socket;
  }

  public boolean isClosed() {
    return socket.isClosed();
  }

  public void handleException(Consumer<Throwable> handler) {
    errorHandler = Objects.requireNonNull(handler);
  }

  public synchronized void send(DataCollection message) {
    if (isClosed()) {
      throw new IllegalStateException("UDP is closed");
    }
    byte[] bytes = codec.encode(Objects.requireNonNull(message), false);
    if (bytes.length > MAX_DATAGRAM) {
      throw new IllegalArgumentException("UDP packet exceeds " + MAX_DATAGRAM);
    }
    try {
      socket.send(new DatagramPacket(bytes, bytes.length, remoteAddress, remotePort));
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  public <D extends DataCollection> void receive(DatagramListener<D> listener) {
    Objects.requireNonNull(listener);
    if (isClosed() || !receiving.compareAndSet(false, true)) {
      throw new IllegalStateException("UDP receive already started or socket closed");
    }
    Thread.ofPlatform().name("tenio-example-udp-reader").start(() -> {
      byte[] bytes = new byte[MAX_DATAGRAM + 1];
      var packet = new DatagramPacket(bytes, bytes.length);
      try {
        while (!isClosed()) {
          packet.setLength(bytes.length);
          socket.receive(packet);
          try {
            if (packet.getLength() > MAX_DATAGRAM) {
              throw new IllegalArgumentException("UDP packet too large");
            }
            byte[] payload = Arrays.copyOfRange(packet.getData(), packet.getOffset(),
                packet.getOffset() + packet.getLength());
            deliver(listener, codec.decode(payload));
          } catch (RuntimeException exception) {
            errorHandler.accept(exception); // One malformed datagram does not stop reception.
          }
        }
      } catch (IOException exception) {
        if (!isClosed()) {
          errorHandler.accept(exception);
        }
      } finally {
        close();
      }
    });
  }

  @SuppressWarnings("unchecked")
  private static <D extends DataCollection> void deliver(DatagramListener<D> listener, DataCollection message) {
    listener.onReceivedUDP((D) message);
  }

  @Override
  public void close() {
    socket.close();
  }
}
