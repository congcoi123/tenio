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
import com.tenio.core.network.entity.session.Session;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** A blocking example transport with one owned reader and serialized writes. */
public final class TCP implements AutoCloseable {

  private final Socket socket = new Socket();
  private final ClientCodec codec = new ClientCodec();
  private final Session session = new CustomSession();
  private final AtomicBoolean receiving = new AtomicBoolean();
  private final AtomicBoolean closed = new AtomicBoolean();
  private final DataInputStream input;
  private final DataOutputStream output;
  private volatile Consumer<Throwable> errorHandler = Throwable::printStackTrace;
  private volatile Runnable disconnected = () -> { };

  /** Connect without starting callbacks; configure listeners before calling receive. */
  public TCP(String host, int port, int timeoutMillis) throws IOException {
    if (timeoutMillis <= 0) {
      throw new IllegalArgumentException("Connection timeout must be positive");
    }
    try {
      socket.connect(new InetSocketAddress(host, port), timeoutMillis);
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      input = new DataInputStream(socket.getInputStream());
      output = new DataOutputStream(socket.getOutputStream());
    } catch (IOException | RuntimeException exception) {
      socket.close();
      throw exception;
    }
  }

  /** Compatibility adapter for existing examples. Prefer the explicit constructor. */
  public TCP(int port, Consumer<TCP> onSuccess) {
    this("localhost", port, onSuccess);
  }

  public TCP(String host, int port, Consumer<TCP> onSuccess) {
    try {
      socket.connect(new InetSocketAddress(host, port), 5000);
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      input = new DataInputStream(socket.getInputStream());
      output = new DataOutputStream(socket.getOutputStream());
    } catch (IOException exception) {
      close();
      throw new UncheckedIOException(exception);
    }
    try {
      Objects.requireNonNull(onSuccess).accept(this);
    } catch (RuntimeException exception) {
      close();
      throw exception;
    }
  }

  public Session getSession() {
    return session;
  }

  public boolean isClosed() {
    return closed.get();
  }

  public void handleException(Consumer<Throwable> handler) {
    errorHandler = Objects.requireNonNull(handler);
  }

  /** Registered before receive; invoked once when this transport closes, including EOF. */
  public void handleDisconnection(Runnable action) {
    disconnected = Objects.requireNonNull(action);
  }

  public synchronized void send(DataCollection message) {
    if (isClosed()) {
      throw new IllegalStateException("TCP is closed");
    }
    byte[] bytes = codec.encode(Objects.requireNonNull(message), true);
    try {
      output.write(bytes);
      output.flush();
    } catch (IOException exception) {
      close();
      throw new UncheckedIOException(exception);
    }
  }

  /** Call once. Callbacks execute on the reader; do not block them. */
  public <D extends DataCollection> void receive(SocketListener<D> listener) {
    Objects.requireNonNull(listener);
    if (isClosed() || !receiving.compareAndSet(false, true)) {
      throw new IllegalStateException("TCP receive already started or connection closed");
    }
    // Platform thread keeps command-line clients alive; close/EOF terminates it.
    Thread.ofPlatform().name("tenio-example-tcp-reader").start(() -> {
      try {
        while (!isClosed()) {
          var message = codec.read(input);
          if (message == null) {
            break;
          }
          deliver(listener, message);
        }
      } catch (IOException | RuntimeException exception) {
        if (!isClosed()) {
          errorHandler.accept(exception);
        }
      } finally {
        close();
      }
    });
  }

  @SuppressWarnings("unchecked")
  private static <D extends DataCollection> void deliver(SocketListener<D> listener, DataCollection message) {
    listener.onReceivedTCP((D) message);
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      try {
        socket.close(); // Unblocks read without waiting on the writer monitor.
      } catch (IOException exception) {
        errorHandler.accept(exception);
      } finally {
        disconnected.run();
      }
    }
  }
}
