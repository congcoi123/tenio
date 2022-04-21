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

package com.tenio.core.network.entity.packet;

import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;

/**
 * The smallest unit to hold and transfer data from the server to clients.
 *
 * @see PacketQueue
 * @see PacketQueuePolicy
 */
public interface Packet {

  /**
   * Retrieves the unique ID of packet.
   *
   * @return the unique {@code long} value ID of packet
   */
  long getId();

  /**
   * Retrieves data of binaries conveyed by the packet.
   *
   * @return data of {@code byte} array conveyed by the packet
   */
  byte[] getData();

  /**
   * Puts data of binaries into the packet.
   *
   * @param binary data of {@code byte} array conveyed by the packet
   */
  void setData(byte[] binary);

  /**
   * Retrieves the transportation type of packet.
   *
   * @return the {@link TransportType} using by the packet
   */
  TransportType getTransportType();

  /**
   * Sets transportation type of the packet.
   *
   * @param transportType the {@link TransportType} using by the packet
   */
  void setTransportType(TransportType transportType);

  /**
   * Retrieves the priority of packet.
   *
   * @return the {@link ResponsePriority} set for the packet
   */
  ResponsePriority getPriority();

  /**
   * Sets priority of the packet.
   *
   * @param priority the {@link ResponsePriority} set for the packet
   */
  void setPriority(ResponsePriority priority);

  /**
   * Determines whether the packet data is encrypted.
   *
   * @return {@code true} if the packet data is encrypted, otherwise returns {@code false}
   */
  boolean isEncrypted();

  /**
   * Marks the packet data is encrypted or not.
   *
   * @param encrypted is set to {@code true} if the packet data is encrypted, otherwise
   *                  {@code false}
   */
  void setEncrypted(boolean encrypted);

  /**
   * Retrieves a collection of sessions which play roles as recipients.
   *
   * @return an unmodifiable collection of {@link Session}s and this can be empty
   * @see Collection
   */
  Collection<Session> getRecipients();

  /**
   * Sets a collection of sessions which play roles as recipients.
   *
   * @param recipients a collection of {@link Session}s and this can be empty
   * @see Collection
   */
  void setRecipients(Collection<Session> recipients);

  /**
   * Retrieves the creation time of packet in milliseconds.
   *
   * @return the creation time in milliseconds ({@code long} value)
   */
  long getCreatedTime();

  /**
   * Retrieves the real size of packet's data in bytes.
   *
   * @return the real packet's data size in {@code integer} value of bytes
   */
  int getOriginalSize();

  /**
   * Determines whether the packet's transportation type is TCP.
   *
   * @return {@code true} if the packet's transportation type is TCP, otherwise
   * {@code false}
   */
  boolean isTcp();

  /**
   * Determines whether the packet's transportation type is UDP.
   *
   * @return {@code true} if the packet's transportation type is UDP, otherwise
   * {@code false}
   */
  boolean isUdp();

  /**
   * Determines whether the packet's transportation type is WebSocket.
   *
   * @return {@code true} if the packet's transportation type is WebSocket, otherwise
   * {@code false}
   */
  boolean isWebSocket();

  /**
   * Retrieves the rest of sending binaries from the packet's data.
   *
   * @return the rest of sending {@code byte} array from the packet's data
   */
  byte[] getFragmentBuffer();

  /**
   * Updates the rest of sending binaries from the packet's data. The data may not be sent at
   * once, but it is split to some parts according to the sending process.
   *
   * @param binary the rest of sending {@code byte} array from the packet's data
   */
  void setFragmentBuffer(byte[] binary);

  /**
   * Determines whether the packet's data is fragmented.
   *
   * @return {@code true} if there is fragmented binaries data waiting to send (In case, the
   * packet's data could not be sent at once), otherwise returns {@code false}
   */
  boolean isFragmented();

  /**
   * Retrieves the Packet's clone instance.
   *
   * @return the {@link Packet}'s clone instance
   */
  Packet clone();
}
