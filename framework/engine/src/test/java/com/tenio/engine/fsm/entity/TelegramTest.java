package com.tenio.engine.fsm.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.engine.message.ExtraMessage;
import org.junit.jupiter.api.Test;

class TelegramTest {
  @Test
  void testConstructor() {
    Telegram actualTelegram = new Telegram();
    assertEquals(-1.0, actualTelegram.getDelayTime());
    assertNull(actualTelegram.getInfo());
    assertNull(actualTelegram.getReceiver());
    assertNull(actualTelegram.getSender());
    assertEquals(-1, actualTelegram.getType());
  }

  @Test
  void testConstructor2() {
    Telegram actualTelegram = new Telegram();
    assertTrue(actualTelegram.getCreatedTime() > 0);
    assertEquals(-1, actualTelegram.getType());
    assertNull(actualTelegram.getSender());
    assertNull(actualTelegram.getReceiver());
    assertEquals(-1.0, actualTelegram.getDelayTime());
  }

  @Test
  void testConstructor3() {
    Telegram actualTelegram = new Telegram(10.0, "Sender", "Receiver", 1);

    assertEquals(1, actualTelegram.getType());
    assertEquals("Sender", actualTelegram.getSender());
    assertEquals("Receiver", actualTelegram.getReceiver());
    assertNull(actualTelegram.getInfo());
    assertEquals(10.0, actualTelegram.getDelayTime());
  }

  @Test
  void testConstructor4() {
    Telegram actualTelegram = new Telegram(10.0, "Sender", "Receiver", 1, mock(ExtraMessage.class));

    assertEquals(1, actualTelegram.getType());
    assertEquals("Sender", actualTelegram.getSender());
    assertEquals("Receiver", actualTelegram.getReceiver());
    assertEquals(10.0, actualTelegram.getDelayTime());
  }

  @Test
  void testSetDelayTime() {
    Telegram telegram = new Telegram();
    telegram.setDelayTime(10.0);
    assertTrue(telegram.getDelayTime() > 0);
  }

  @Test
  void testEquals() {
    assertFalse((new Telegram()).equals(null));
    assertFalse((new Telegram()).equals("Different type to Telegram"));
    assertTrue((new Telegram()).equals(new Telegram()));
  }

  @Test
  void testEquals2() {
    Telegram telegram = new Telegram();
    assertTrue(telegram.equals(new Telegram()));
  }

  @Test
  void testEquals3() {
    Telegram telegram = new Telegram(10.0, "Sender", "Receiver", 1);
    assertFalse(telegram.equals(new Telegram()));
  }

  @Test
  void testEquals4() {
    Telegram telegram = new Telegram(-1.0, "Sender", "Receiver", 1);
    assertFalse(telegram.equals(new Telegram()));
  }

  @Test
  void testCompareTo() {
    Telegram telegram = new Telegram();
    assertEquals(1, telegram.compareTo(new Telegram()));
  }

  @Test
  void testCompareTo2() {
    Telegram telegram = new Telegram(10.0, "Sender", "Receiver", 1);
    assertEquals(-1, telegram.compareTo(new Telegram()));
  }

  @Test
  void testHashCode() {
    Telegram telegram = new Telegram(0, "A", "B", 1);
    assertNotEquals(0, telegram.hashCode());
  }

  @Test
  void testToString() {
    Telegram telegram = new Telegram(0, "A", "B", 1, mock(ExtraMessage.class));
    String result = telegram.toString();
    assertNotNull(result);
    assertTrue(result.contains("Sender"));
    assertTrue(result.contains("Receiver"));
  }

  @Test
  void testCompareToSameInstance() {
    Telegram telegram = new Telegram(0, "A", "B", 1);
    assertEquals(0, telegram.compareTo(telegram));
  }
}

