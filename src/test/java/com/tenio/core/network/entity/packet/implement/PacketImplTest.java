package com.tenio.core.network.entity.packet.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.Packet;
import org.junit.jupiter.api.Test;

class PacketImplTest {
  @Test
  void testNewInstance() {
    Packet actualNewInstanceResult = PacketImpl.newInstance();
    assertFalse(actualNewInstanceResult.isEncrypted());
    assertEquals(TransportType.UNKNOWN, actualNewInstanceResult.getTransportType());
    assertEquals(ResponsePriority.NORMAL, actualNewInstanceResult.getPriority());
  }
}

