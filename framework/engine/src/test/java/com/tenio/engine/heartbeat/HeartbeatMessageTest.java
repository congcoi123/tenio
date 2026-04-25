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

package com.tenio.engine.heartbeat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.engine.message.ExtraMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HeartbeatMessageTest {

  private HeartbeatMessage message;
  private ExtraMessage extraMessage;

  @BeforeEach
  void setUp() {
    extraMessage = mock(ExtraMessage.class);
    message = HeartbeatMessage.newInstance(extraMessage, 1.0);
  }

  @Test
  void testNewInstance() {
    assertNotNull(message);
  }

  @Test
  void testGetId() {
    assertNotNull(message.getId());
    assertFalse(message.getId().isEmpty());
  }

  @Test
  void testGetMessage() {
    assertEquals(extraMessage, message.getMessage());
  }

  @Test
  void testGetDelayTime() {
    assertTrue(message.getDelayTime() > 0);
  }

  @Test
  void testEqualsNull() {
    assertFalse(message.equals(null));
  }

  @Test
  void testEqualsNonHeartbeatMessage() {
    assertFalse(message.equals("not a heartbeat message"));
  }

  @Test
  void testEqualsSameDelayTime() {
    // Two messages created back-to-back should have nearly the same delay time
    HeartbeatMessage other = HeartbeatMessage.newInstance(mock(ExtraMessage.class), 1.0);
    // Both are within SMALLEST_DELAY (0.25s) of each other
    assertTrue(message.equals(other));
  }

  @Test
  void testEqualsDifferentDelayTime() {
    HeartbeatMessage later = HeartbeatMessage.newInstance(mock(ExtraMessage.class), 1000.0);
    assertFalse(message.equals(later));
  }

  @Test
  void testHashCode() {
    int hash = message.hashCode();
    assertNotEquals(0, hash);
  }

  @Test
  void testCompareToSameObject() {
    assertEquals(0, message.compareTo(message));
  }

  @Test
  void testCompareToEarlierDelay() {
    HeartbeatMessage earlier = HeartbeatMessage.newInstance(mock(ExtraMessage.class), 0.0);
    // message has delay +1s, earlier has delay +0s => message > earlier => -1
    assertEquals(-1, message.compareTo(earlier));
  }

  @Test
  void testCompareToLaterDelay() {
    HeartbeatMessage later = HeartbeatMessage.newInstance(mock(ExtraMessage.class), 1000.0);
    // message delay < later delay => 1
    assertEquals(1, message.compareTo(later));
  }

  @Test
  void testToString() {
    String result = message.toString();
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }
}
