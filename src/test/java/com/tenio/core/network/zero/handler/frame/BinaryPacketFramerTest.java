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

package com.tenio.core.network.zero.handler.frame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.packet.PacketHeader;
import com.tenio.core.network.codec.packet.PacketReadState;
import com.tenio.core.network.codec.packet.PendingPacket;
import com.tenio.core.network.codec.packet.ProcessedPacket;
import com.tenio.core.network.entity.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BinaryPacketFramer")
class BinaryPacketFramerTest {

  private BinaryPacketFramer framer;

  @BeforeEach
  void setUp() {
    framer = new BinaryPacketFramer();
  }

  @Test
  @DisplayName("getBinaryPacketDecoder returns null before being set")
  void testGetBinaryPacketDecoderIsNullBeforeSet() {
    assertNull(framer.getBinaryPacketDecoder());
  }

  @Test
  @DisplayName("setBinaryPacketDecoder and getBinaryPacketDecoder work correctly")
  void testSetAndGetBinaryPacketDecoder() {
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    framer.setBinaryPacketDecoder(decoder);
    assertEquals(decoder, framer.getBinaryPacketDecoder());
  }

  @Test
  @DisplayName("framing with empty byte array does not alter session state")
  void testFramingWithEmptyArrayDoesNothing() {
    Session session = mock(Session.class);
    when(session.getPacketReadState()).thenReturn(PacketReadState.WAIT_NEW_PACKET);

    framer.framing(session, new byte[0]);

    // The empty array exits the while loop immediately; setPacketReadState is still called
    verify(session).setPacketReadState(PacketReadState.WAIT_NEW_PACKET);
    verify(session, never()).getPendingPacket();
  }

  @Test
  @DisplayName("framing with header missing LENGTH_PREFIXED swallows exception and resets state")
  void testFramingWithMissingLengthPrefixedFlagResetsState() {
    Session session = mock(Session.class);
    when(session.getPacketReadState()).thenReturn(PacketReadState.WAIT_NEW_PACKET);

    // 0x00: no LENGTH_PREFIXED bit, DataType=ZERO (bits 0-1 = 0)
    framer.framing(session, new byte[]{0x00});

    // Exception thrown in handleNewPacket is swallowed; state is reset to WAIT_NEW_PACKET
    verify(session).setPacketReadState(PacketReadState.WAIT_NEW_PACKET);
  }

  @Test
  @DisplayName("framing with unsupported DataType swallows exception and resets state")
  void testFramingWithUnsupportedDataTypeResetsState() {
    Session session = mock(Session.class);
    when(session.getPacketReadState()).thenReturn(PacketReadState.WAIT_NEW_PACKET);

    // 0b10000010 = LENGTH_PREFIXED + DataType value 2 (0b10), which has no DataType mapping
    framer.framing(session, new byte[]{(byte) 0b10000010});

    verify(session).setPacketReadState(PacketReadState.WAIT_NEW_PACKET);
  }

  @Test
  @DisplayName("framing a complete small packet calls onFramedResult on the listener")
  void testFramingCompleteSmallPacketCallsListener() {
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    PacketFramingListener listener = mock(PacketFramingListener.class);
    DataCollection decodedMessage = mock(DataCollection.class);
    framer.setBinaryPacketDecoder(decoder);
    framer.setPacketFramingResult(listener);

    // Set up a real session state machine using real PendingPacket and ProcessedPacket
    Session session = mock(Session.class);
    PendingPacket pendingPacket = PendingPacket.newInstance();
    ProcessedPacket processedPacket = ProcessedPacket.newInstance();
    when(session.getPacketReadState()).thenReturn(PacketReadState.WAIT_NEW_PACKET);
    when(session.getPendingPacket()).thenReturn(pendingPacket);
    when(session.getProcessedPacket()).thenReturn(processedPacket);
    when(decoder.decode(any(PacketHeader.class), any(byte[].class))).thenReturn(decodedMessage);

    // Construct a valid binary packet:
    //   Byte 0:    0x80 = LENGTH_PREFIXED flag (bit 7) + DataType ZERO (bits 0-1 = 0)
    //   Bytes 1-2: data size as a short big-endian = 3 (0x00, 0x03)
    //   Bytes 3-5: payload data
    byte[] packet = {(byte) 0x80, 0x00, 0x03, 0x01, 0x02, 0x03};

    framer.framing(session, packet);

    verify(listener).onFramedResult(session, decodedMessage);
    verify(session).setPacketReadState(PacketReadState.WAIT_NEW_PACKET);
  }

  @Test
  @DisplayName("framing a packet with only partial size bytes transitions to WAIT_DATA_SIZE_FRAGMENT")
  void testFramingWithPartialSizeBytesTransitionsToWaitDataSizeFragment() {
    BinaryPacketDecoder decoder = mock(BinaryPacketDecoder.class);
    PacketFramingListener listener = mock(PacketFramingListener.class);
    framer.setBinaryPacketDecoder(decoder);
    framer.setPacketFramingResult(listener);

    Session session = mock(Session.class);
    PendingPacket pendingPacket = PendingPacket.newInstance();
    ProcessedPacket processedPacket = ProcessedPacket.newInstance();
    when(session.getPacketReadState()).thenReturn(PacketReadState.WAIT_NEW_PACKET);
    when(session.getPendingPacket()).thenReturn(pendingPacket);
    when(session.getProcessedPacket()).thenReturn(processedPacket);

    // Only header byte + 1 byte of the 2-byte size field: size is incomplete
    // 0x80 = LENGTH_PREFIXED + ZERO type; 0x00 = partial first size byte
    byte[] partial = {(byte) 0x80, 0x00};

    framer.framing(session, partial);

    // The loop exits due to empty binaries; final state should be WAIT_DATA_SIZE_FRAGMENT
    verify(session).setPacketReadState(PacketReadState.WAIT_DATA_SIZE_FRAGMENT);
    // Listener should NOT be called since packet is incomplete
    verify(listener, never()).onFramedResult(any(), any());
  }
}
