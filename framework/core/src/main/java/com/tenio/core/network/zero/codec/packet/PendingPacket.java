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

package com.tenio.core.network.zero.codec.packet;

import java.nio.ByteBuffer;

/**
 * Holds a pending packet for the next steps.
 */
public final class PendingPacket {

  private PacketHeader packetHeader;
  private ByteBuffer byteBuffer;
  private int expectedLength;

  private PendingPacket() {
  }

  /**
   * Create a new instance.
   *
   * @return a new instance of {@link PendingPacket}
   */
  public static PendingPacket newInstance() {
    return new PendingPacket();
  }

  /**
   * Retrieves a packet's header.
   *
   * @return the {@link PacketHeader} of the packet
   */
  public PacketHeader getPacketHeader() {
    return packetHeader;
  }

  /**
   * Sets a packetHeader for a packet.
   *
   * @param packetHeader a {@link PacketHeader} of the packet
   */
  public void setPacketHeader(PacketHeader packetHeader) {
    this.packetHeader = packetHeader;
  }

  /**
   * Retrieves a {@link ByteBuffer} instance to read/write packet's data.
   *
   * @return a {@link ByteBuffer} instance to read/write packet's data
   */
  public ByteBuffer getBuffer() {
    return byteBuffer;
  }

  /**
   * Sets a {@link ByteBuffer} instance to read/write packet's data.
   *
   * @param byteBuffer a {@link ByteBuffer} instance to read/write packet's data
   */
  public void setBuffer(ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  /**
   * Retrieves the expected length of packet's data.
   *
   * @return the expected length of packet's data ({@code integer} value)
   */
  public int getExpectedLength() {
    return expectedLength;
  }

  /**
   * Sets the expected length for the packet's data.
   *
   * @param expectedLength the expected length of packet's data ({@code integer} value)
   */
  public void setExpectedLength(int expectedLength) {
    this.expectedLength = expectedLength;
  }

  @Override
  public String toString() {
    return "PendingPacket{" +
        "packetHeader=" + packetHeader +
        ", byteBuffer=" + byteBuffer +
        ", expectedLength=" + expectedLength +
        '}';
  }
}
