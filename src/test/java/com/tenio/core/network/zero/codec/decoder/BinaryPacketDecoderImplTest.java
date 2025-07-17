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

package com.tenio.core.network.zero.codec.decoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.codec.packet.PacketReadState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BinaryPacketDecoderImpl")
class BinaryPacketDecoderImplTest {

  private BinaryPacketDecoderImpl decoder;
  private Session session;

  @BeforeEach
  void setUp() {
    decoder = new BinaryPacketDecoderImpl();
    session = mock(Session.class);
    when(session.getPacketReadState()).thenReturn(PacketReadState.WAIT_NEW_PACKET);
    when(session.getProcessedPacket()).thenReturn(
        mock(com.tenio.core.network.zero.codec.packet.ProcessedPacket.class));
    when(session.getPendingPacket()).thenReturn(
        mock(com.tenio.core.network.zero.codec.packet.PendingPacket.class));
  }

  @Test
  @DisplayName("Decode null data should not throw any exception")
  void testDecodeNullData() {
    assertDoesNotThrow(() -> decoder.decode(session, null));
  }

  @Test
  @DisplayName("Decode valid data should not throw any exception")
  void testDecodeValidData() {
    byte[] data = new byte[] {1, 2, 3};
    assertDoesNotThrow(() -> decoder.decode(session, data));
  }
}
