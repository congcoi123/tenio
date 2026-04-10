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

package com.tenio.core.network.entity.inbound.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.common.data.DataCollection;
import com.tenio.core.network.entity.inbound.Request;
import com.tenio.core.network.entity.session.Session;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For SessionRequest")
class SessionRequestTest {

  private Request request;

  @BeforeEach
  void setUp() {
    request = SessionRequest.newInstance();
  }

  @Test
  @DisplayName("newInstance creates a non-null request")
  void testNewInstanceCreatesNonNull() {
    assertNotNull(request);
  }

  @Test
  @DisplayName("getId returns a positive value")
  void testGetIdReturnsPositiveValue() {
    assertTrue(request.getId() > 0);
  }

  @Test
  @DisplayName("two consecutive instances have different IDs")
  void testTwoInstancesHaveDifferentIds() {
    Request other = SessionRequest.newInstance();
    assertNotEquals(request.getId(), other.getId());
  }

  @Test
  @DisplayName("getCreatedTimestamp returns a positive value")
  void testGetCreatedTimestampIsPositive() {
    assertTrue(request.getCreatedTimestamp() > 0);
  }

  @Test
  @DisplayName("getSender returns null before setSender is called")
  void testGetSenderReturnsNullInitially() {
    assertNull(request.getSender());
  }

  @Test
  @DisplayName("setSender stores the Session and getSender returns it")
  void testSetAndGetSender() {
    Session session = mock(Session.class);
    request.setSender(session);
    assertEquals(session, request.getSender());
  }

  @Test
  @DisplayName("getRemoteAddress throws UnsupportedOperationException")
  void testGetRemoteAddressThrowsUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> request.getRemoteAddress());
  }

  @Test
  @DisplayName("setRemoteAddress throws UnsupportedOperationException")
  void testSetRemoteAddressThrowsUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class,
        () -> request.setRemoteAddress(new InetSocketAddress("127.0.0.1", 9000)));
  }

  @Test
  @DisplayName("getMessage returns null before setMessage is called")
  void testGetMessageReturnsNullInitially() {
    assertNull(request.getMessage());
  }

  @Test
  @DisplayName("setMessage stores the message and getMessage returns it")
  void testSetAndGetMessage() {
    DataCollection message = mock(DataCollection.class);
    request.setMessage(message);
    assertEquals(message, request.getMessage());
  }

  @Test
  @DisplayName("getPriority returns 0 by default")
  void testGetPriorityDefaultIsZero() {
    assertEquals(0, request.getPriority());
  }

  @Test
  @DisplayName("setPriority stores value and getPriority returns it")
  void testSetAndGetPriority() {
    request.setPriority(3);
    assertEquals(3, request.getPriority());
  }

  @Test
  @DisplayName("getEvent returns null before setEvent is called")
  void testGetEventReturnsNullInitially() {
    assertNull(request.getEvent());
  }

  @Test
  @DisplayName("equals returns true for the same instance")
  void testEqualsSameInstance() {
    assertEquals(request, request);
  }

  @Test
  @DisplayName("equals returns false for two different instances with different IDs")
  void testEqualsReturnsFalseForDifferentInstances() {
    Request other = SessionRequest.newInstance();
    assertFalse(request.equals(other));
  }

  @Test
  @DisplayName("equals returns false for a non-Request object")
  void testEqualsReturnsFalseForNonRequest() {
    assertFalse(request.equals("not a request"));
  }

  @Test
  @DisplayName("hashCode is consistent with id")
  void testHashCodeConsistentWithId() {
    assertEquals(Long.hashCode(request.getId()), request.hashCode());
  }

  @Test
  @DisplayName("toString is not null and contains id")
  void testToStringContainsId() {
    String str = request.toString();
    assertNotNull(str);
    assertTrue(str.contains(String.valueOf(request.getId())));
  }
}
