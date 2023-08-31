package com.tenio.core.network.entity.session.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.session.Session;
import org.junit.jupiter.api.Test;

class SessionImplTest {
  @Test
  void testNewInstance() {
    Session actualNewInstanceResult = SessionImpl.newInstance();
    assertFalse(actualNewInstanceResult.isAssociatedToPlayer(Session.AssociatedState.DONE));
    assertFalse(actualNewInstanceResult.isActivated());
    assertEquals(0L, actualNewInstanceResult.getWrittenBytes());
    assertEquals(TransportType.UNKNOWN, actualNewInstanceResult.getTransportType());
    assertEquals(0L, actualNewInstanceResult.getReadBytes());
    assertNull(actualNewInstanceResult.getPacketQueue());
    assertEquals(0L, actualNewInstanceResult.getInactivatedTime());
    assertEquals(0L, actualNewInstanceResult.getDroppedPackets());
  }
}

