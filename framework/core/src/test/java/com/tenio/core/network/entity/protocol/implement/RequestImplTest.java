package com.tenio.core.network.entity.protocol.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.network.define.RequestPriority;
import org.junit.jupiter.api.Test;

class RequestImplTest {
  @Test
  void testNewInstance() {
    assertEquals(RequestPriority.NORMAL, RequestImpl.newInstance().getPriority());
  }
}

