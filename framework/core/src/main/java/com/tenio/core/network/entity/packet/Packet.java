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

package com.tenio.core.network.entity.packet;

import com.tenio.common.data.DataType;
import com.tenio.core.network.define.ResponseGuarantee;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents the smallest unit of data transfer between the server and clients.
 * This class encapsulates network communication data with support for different
 * transport types and priority levels.
 *
 * <p>Key features:
 * <ul>
 *   <li>Multiple transport type support (TCP, UDP)</li>
 *   <li>Priority-based packet handling</li>
 *   <li>Session association</li>
 *   <li>Deep copy capabilities</li>
 *   <li>Last packet marking for connection closure</li>
 * </ul>
 *
 * <p>Thread safety: This class is not thread-safe and should be used
 * within a thread-safe context like {@link PacketQueue}.
 *
 * @see PacketQueue
 * @see PacketQueuePolicy
 * @see TransportType
 * @see ResponseGuarantee
 * @see Session
 */
public interface Packet {

  /**
   * IDs generator.
   */
  AtomicLong ID_COUNTER = new AtomicLong(1L);

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
   * @param binaries data of {@code byte} array conveyed by the packet
   */
  void setData(byte[] binaries);

  /**
   * Retrieves the data type of packet.
   *
   * @return the {@link DataType} using by the packet
   * @since 0.6.7
   */
  DataType getDataType();

  /**
   * Sets data type of packet.
   *
   * @param dataType the {@link DataType} using by the packet
   * @since 0.6.7
   */
  void setDataType(DataType dataType);

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
   * Retrieves the guarantee level of packet.
   *
   * @return the {@link ResponseGuarantee} set for the packet
   */
  ResponseGuarantee getGuarantee();

  /**
   * Sets guarantee level of the packet.
   *
   * @param guarantee the {@link ResponseGuarantee} set for the packet
   */
  void setGuarantee(ResponseGuarantee guarantee);

  /**
   * Determines whether the packet data is encrypted.
   *
   * @return {@code true} if the packet data is encrypted, otherwise returns {@code false}
   */
  boolean needsEncrypted();

  /**
   * Marks the packet data is encrypted or not.
   *
   * @param encrypted is set to {@code true} if the packet data is encrypted, otherwise
   *                  {@code false}
   */
  void needsEncrypted(boolean encrypted);

  /**
   * Determines whether the packet needs data counting in the header.
   *
   * @return {@code true} if the packet data needs data counting, otherwise returns {@code false}
   * @since 0.6.7
   */
  boolean needsDataCounting();

  /**
   * Marks the packet is needed data counting or not.
   *
   * @param counting is set to {@code true} if the packet needs data counting, otherwise
   *                 {@code false}
   * @since 0.6.7
   */
  void needsDataCounting(boolean counting);

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
   * Retrieves the rest of sending binaries from the packet's data.
   *
   * @return the rest of sending {@code byte} array from the packet's data
   */
  byte[] getFragmentBuffer();

  /**
   * Updates the rest of sending binaries from the packet's data. The data may not be sent at
   * once, but it is split to some parts according to the sending process.
   *
   * @param binaries the rest of sending {@code byte} array from the packet's data
   */
  void setFragmentBuffer(byte[] binaries);

  /**
   * Determines whether the packet's data is fragmented.
   *
   * @return {@code true} if there is fragmented binaries data waiting to send (In case, the
   * packet's data could not be sent at once), otherwise returns {@code false}
   */
  boolean isFragmented();

  /**
   * Determines whether the packet is the last one or not. In case this is the last sent packet,
   * it will close the connection.
   *
   * @return {@code true} if the packet is the last sent one, otherwise returns {@code false}
   * @since 0.5.0
   */
  boolean isMarkedAsLast();

  /**
   * Marks this packet as the last one. After sending it, the session will be closed.
   *
   * @param markedAsLast determines if the packet is the last one or not
   * @since 0.5.0
   */
  void setMarkedAsLast(boolean markedAsLast);

  /**
   * Retrieves the Packet's clone instance.
   *
   * @return the {@link Packet}'s clone instance
   */
  Packet deepCopy();
}
