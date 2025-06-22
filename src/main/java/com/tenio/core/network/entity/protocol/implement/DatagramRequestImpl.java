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

package com.tenio.core.network.entity.protocol.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.entity.protocol.Request;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * Request implementation for Datagram channels.
 *
 * @since 0.5.0
 */
public class DatagramRequestImpl implements Request {

  private final long id;
  private final long timestamp;
  private ServerEvent event;
  private DatagramChannel datagramChannel;
  private SocketAddress datagramRemoteSocketAddress;
  private DataCollection message;
  private RequestPriority priority;

  private DatagramRequestImpl() {
    id = ID_COUNTER.getAndIncrement();
    priority = RequestPriority.LOW;
    timestamp = TimeUtility.currentTimeMillis();
  }

  /**
   * Creates a new request instance.
   *
   * @return a new instance of {@link Request}
   */
  public static Request newInstance() {
    return new DatagramRequestImpl();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public DatagramChannel getSender() {
    return datagramChannel;
  }

  @Override
  public Request setSender(Object sender) {
    datagramChannel = (DatagramChannel) sender;
    return this;
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    return datagramRemoteSocketAddress;
  }

  @Override
  public Request setRemoteSocketAddress(SocketAddress remoteSocketAddress) {
    this.datagramRemoteSocketAddress = remoteSocketAddress;
    return this;
  }

  @Override
  public DataCollection getMessage() {
    return message;
  }

  @Override
  public Request setMessage(DataCollection message) {
    this.message = message;
    return this;
  }

  @Override
  public RequestPriority getPriority() {
    return priority;
  }

  @Override
  public Request setPriority(RequestPriority priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public long getCreatedTimestamp() {
    return timestamp;
  }

  @Override
  public ServerEvent getEvent() {
    return event;
  }

  @Override
  public Request setEvent(ServerEvent event) {
    this.event = event;
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Request request)) {
      return false;
    } else {
      return getId() == request.getId();
    }
  }

  /**
   * It is generally necessary to override the <b>hashCode</b> method whenever
   * equals method is overridden, so as to maintain the general contract for the
   * hashCode method, which states that equal objects must have equal hash codes.
   *
   * @see <a href="https://imgur.com/x6rEAZE">Formula</a>
   */
  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public String toString() {
    return "DatagramRequest{" +
        "id=" + id +
        ", timestamp=" + timestamp +
        ", event=" + event +
        ", sender=" + datagramChannel +
        ", remoteSocketAddress=" + datagramRemoteSocketAddress +
        ", message=" + message +
        ", priority=" + priority +
        '}';
  }
}
