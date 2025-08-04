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

package com.tenio.core.network.codec.encoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataType;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BinaryPacketEncoderImpl")
class BinaryPacketEncoderImplTest {

  private BinaryPacketEncoderImpl encoder;

  @BeforeEach
  void setUp() {
    encoder = new BinaryPacketEncoderImpl();
  }

  @Test
  @DisplayName("Try to encode an empty packet should throw exception")
  void testEncodeNullPacket() {
    var packet = PacketImpl.newInstance();
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(packet));
    packet.setData(new byte[0]);
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(packet));
  }

  @Test
  @DisplayName("Test encoding a valid packet")
  void testEncodeValidPacket() {
    Packet packet = mock(Packet.class);
    when(packet.getDataType()).thenReturn(DataType.ZERO);
    when(packet.getData()).thenReturn(new byte[] {1, 2, 3});
    assertNotNull(encoder.encode(packet));
  }
}
