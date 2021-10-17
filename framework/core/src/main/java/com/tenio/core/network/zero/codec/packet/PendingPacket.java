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

package com.tenio.core.network.zero.codec.packet;

import java.nio.ByteBuffer;

/**
 * Holding the pending packet state for the next steps.
 */
public final class PendingPacket {

  private PacketHeader packetHeader;
  private ByteBuffer byteBuffer;
  private int expectedLength;

  private PendingPacket() {
  }

  public static PendingPacket newInstance() {
    return new PendingPacket();
  }

  public PacketHeader getPacketHeader() {
    return packetHeader;
  }

  public void setPacketHeader(PacketHeader header) {
    packetHeader = header;
  }

  public ByteBuffer getBuffer() {
    return byteBuffer;
  }

  public void setBuffer(ByteBuffer buffer) {
    byteBuffer = buffer;
  }

  public int getExpectedLength() {
    return expectedLength;
  }

  public void setExpectedLength(int expectedLength) {
    this.expectedLength = expectedLength;
  }

  @Override
  public String toString() {
    return String.format("{ packetHeader: %s, expectedLength: %d }", packetHeader.toString(),
        expectedLength);
  }
}
