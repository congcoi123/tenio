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

package com.tenio.core.network.entity.outbound.packet.implement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.common.data.DataType;
import com.tenio.core.network.define.ResponseGuarantee;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.outbound.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PacketImplTest {

  private Packet packet;

  @BeforeEach
  void setUp() {
    packet = PacketImpl.newInstance();
  }

  @Test
  void testNewInstance() {
    Packet actualNewInstanceResult = PacketImpl.newInstance();
    assertFalse(actualNewInstanceResult.needsEncrypted());
    assertEquals(TransportType.UNKNOWN, actualNewInstanceResult.getTransportType());
    assertEquals(ResponseGuarantee.NORMAL, actualNewInstanceResult.getGuarantee());
  }

  @Test
  void testGetIdReturnsPositiveValue() {
    assertTrue(packet.getId() > 0);
  }

  @Test
  void testTwoInstancesHaveDifferentIds() {
    assertNotEquals(packet.getId(), PacketImpl.newInstance().getId());
  }

  @Test
  void testGetCreatedTimeIsPositive() {
    assertTrue(packet.getCreatedTime() > 0);
  }

  @Test
  void testSetDataAndOriginalSize() {
    byte[] data = {1, 2, 3, 4};
    packet.setData(data);
    assertArrayEquals(data, packet.getData());
    assertEquals(4, packet.getOriginalSize());
  }

  @Test
  void testGetDataReturnsNullInitially() {
    assertNull(packet.getData());
  }

  @Test
  void testSetAndGetDataType() {
    packet.setDataType(DataType.MSG_PACK);
    assertEquals(DataType.MSG_PACK, packet.getDataType());
  }

  @Test
  void testSetTransportTypeTcpMakesIsTcpTrue() {
    packet.setTransportType(TransportType.TCP);
    assertTrue(packet.isTcp());
    assertFalse(packet.isUdp());
  }

  @Test
  void testSetTransportTypeUdpMakesIsUdpTrue() {
    packet.setTransportType(TransportType.UDP);
    assertTrue(packet.isUdp());
    assertFalse(packet.isTcp());
  }

  @Test
  void testSetAndGetGuarantee() {
    packet.setGuarantee(ResponseGuarantee.GUARANTEED);
    assertEquals(ResponseGuarantee.GUARANTEED, packet.getGuarantee());
  }

  @Test
  void testNeedsEncryptedFlag() {
    packet.needsEncrypted(true);
    assertTrue(packet.needsEncrypted());
  }

  @Test
  void testHasLengthPrefixedFlag() {
    packet.hasLengthPrefixed(true);
    assertTrue(packet.hasLengthPrefixed());
  }

  @Test
  void testSetMarkedAsLast() {
    packet.setMarkedAsLast(true);
    assertTrue(packet.isMarkedAsLast());
  }

  @Test
  void testSetFragmentBufferAndIsFragmented() {
    assertFalse(packet.isFragmented());
    packet.setFragmentBuffer(new byte[]{0x01, 0x02});
    assertTrue(packet.isFragmented());
    assertArrayEquals(new byte[]{0x01, 0x02}, packet.getFragmentBuffer());
  }

  @Test
  void testSetAndGetRecipients() {
    Session session = mock(Session.class);
    packet.setRecipients(List.of(session));
    assertEquals(1, packet.getRecipients().size());
    assertTrue(packet.getRecipients().contains(session));
  }

  @Test
  void testDeepCopyCopiesAllFields() {
    byte[] data = {10, 20, 30};
    packet.setData(data);
    packet.setDataType(DataType.MSG_PACK);
    packet.setTransportType(TransportType.TCP);
    packet.setGuarantee(ResponseGuarantee.GUARANTEED);
    packet.needsEncrypted(true);
    packet.hasLengthPrefixed(true);
    packet.setMarkedAsLast(true);

    Packet copy = ((PacketImpl) packet).deepCopy();

    assertNotEquals(packet.getId(), copy.getId());
    assertArrayEquals(data, copy.getData());
    assertEquals(DataType.MSG_PACK, copy.getDataType());
    assertEquals(TransportType.TCP, copy.getTransportType());
    assertEquals(ResponseGuarantee.GUARANTEED, copy.getGuarantee());
    assertTrue(copy.needsEncrypted());
    assertTrue(copy.hasLengthPrefixed());
    assertTrue(copy.isMarkedAsLast());
  }

  @Test
  void testCompareToSortsNewerPacketsFirst() {
    PacketImpl older = (PacketImpl) packet;
    PacketImpl newer = (PacketImpl) PacketImpl.newInstance();
    assertTrue(older.compareTo(newer) > 0);
    assertTrue(newer.compareTo(older) < 0);
  }

  @Test
  void testEqualsSameInstance() {
    assertEquals(packet, packet);
  }

  @Test
  void testEqualsReturnsFalseForDifferentPackets() {
    assertFalse(packet.equals(PacketImpl.newInstance()));
  }

  @Test
  void testEqualsReturnsFalseForNonPacket() {
    assertFalse(packet.equals("not a packet"));
  }

  @Test
  void testHashCodeConsistentWithId() {
    assertEquals(Long.hashCode(packet.getId()), packet.hashCode());
  }

  @Test
  void testToStringIsNotNull() {
    assertNotNull(packet.toString());
    assertTrue(packet.toString().contains("Packet"));
  }
}
