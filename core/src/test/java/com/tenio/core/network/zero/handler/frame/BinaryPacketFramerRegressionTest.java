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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.codec.CodecUtility;
import com.tenio.core.network.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.codec.packet.PacketHeader;
import com.tenio.common.data.DataType;
import com.tenio.core.network.entity.session.implement.SessionImpl;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class BinaryPacketFramerRegressionTest {
  private static final class FramingSession extends SessionImpl {
    FramingSession() { createPacketSocketHandler(); }
  }

  private byte[] frame(byte[] data) {
    var header = PacketHeader.newInstance(true, false, false, false, DataType.ZERO);
    return ByteBuffer.allocate(data.length + 3)
        .put(CodecUtility.encodeFirstHeaderByte(header)).putShort((short) data.length)
        .put(data).array();
  }

  @Test
  void fragmentedBodyAndCoalescedNextFrameDecodeOnlyTheirOwnPayload() {
    var session = new FramingSession();
    var framer = new BinaryPacketFramer();
    var decoder = mock(BinaryPacketDecoder.class);
    var decoded = new ArrayList<byte[]>();
    when(decoder.decode(any(PacketHeader.class), any(byte[].class))).thenAnswer(call -> {
      decoded.add(call.getArgument(1));
      return mock(DataCollection.class);
    });
    framer.setBinaryPacketDecoder(decoder);
    framer.setPacketFramingResult((it, message) -> { });
    byte[] first = frame(new byte[] {1, 2, 3, 4});
    byte[] next = frame(new byte[] {5, 6});
    framer.framing(session, Arrays.copyOf(first, 5));
    assertTrue(decoded.isEmpty());
    byte[] rest = ByteBuffer.allocate(2 + next.length).put(first, 5, 2).put(next).array();
    framer.framing(session, rest);
    assertEquals(2, decoded.size());
    assertArrayEquals(new byte[] {1, 2, 3, 4}, decoded.get(0));
    assertArrayEquals(new byte[] {5, 6}, decoded.get(1));
  }

  @Test
  void unsignedNormalLengthWorksForWholeAndFragmentedHeaders() {
    for (boolean fragmented : new boolean[] {false, true}) {
      var session = new FramingSession();
        var framer = new BinaryPacketFramer();
      var decoder = mock(BinaryPacketDecoder.class);
      var lengths = new ArrayList<Integer>();
      when(decoder.decode(any(PacketHeader.class), any(byte[].class))).thenAnswer(call -> {
        lengths.add(((byte[]) call.getArgument(1)).length);
        return mock(DataCollection.class);
      });
      framer.setBinaryPacketDecoder(decoder);
      framer.setPacketFramingResult((it, message) -> { });
      byte[] data = frame(new byte[40000]);
      if (fragmented) {
        framer.framing(session, Arrays.copyOf(data, 2));
        framer.framing(session, Arrays.copyOfRange(data, 2, data.length));
      } else {
        framer.framing(session, data);
      }
      assertEquals(java.util.List.of(40000), lengths);
    }
  }
}
