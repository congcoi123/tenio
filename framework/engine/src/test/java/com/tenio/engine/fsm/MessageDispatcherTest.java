package com.tenio.engine.fsm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.engine.fsm.entity.AbstractEntity;
import com.tenio.engine.fsm.entity.Telegram;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageDispatcherTest {

  private EntityManager entityManager;
  private MessageDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    entityManager = new EntityManager();
    dispatcher = new MessageDispatcher(entityManager);
  }

  @Test
  void testConstructor() {
    new MessageDispatcher(new EntityManager());
  }

  @Test
  void testDispatchMessageWithUnknownReceiverDoesNotThrow() {
    assertDoesNotThrow(() ->
        dispatcher.dispatchMessage(10.0, "Sender", "Receiver", 1, null));
  }

  @Test
  void testDispatchMessageImmediateHandledNotifiesListener() {
    List<Boolean> results = new ArrayList<>();
    entityManager.register(new TestEntity("receiver", true));
    dispatcher.listen((msg, handled) -> results.add(handled));
    dispatcher.dispatchMessage(0, "sender", "receiver", 1, null);
    assertFalse(results.isEmpty());
    assertTrue(results.get(0));
  }

  @Test
  void testDispatchMessageImmediateUnhandledNotifiesListener() {
    List<Boolean> results = new ArrayList<>();
    entityManager.register(new TestEntity("receiver", false));
    dispatcher.listen((msg, handled) -> results.add(handled));
    dispatcher.dispatchMessage(0, "sender", "receiver", 1, null);
    assertFalse(results.isEmpty());
    assertFalse(results.get(0));
  }

  @Test
  void testDispatchMessageWithDelayDoesNotThrow() {
    entityManager.register(new TestEntity("receiver", true));
    assertDoesNotThrow(() ->
        dispatcher.dispatchMessage(10.0, "sender", "receiver", 1, null));
  }

  @Test
  void testUpdateDoesNotThrow() {
    assertDoesNotThrow(() -> dispatcher.update(0.5f));
  }

  @Test
  void testListenDoesNotThrow() {
    assertDoesNotThrow(() -> dispatcher.listen(mock(MessageListener.class)));
  }

  @Test
  void testClearDoesNotThrow() {
    assertDoesNotThrow(() -> dispatcher.clear());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testUpdateDispatchesPastDueTelegrams() throws Exception {
    entityManager.register(new TestEntity("target", true));
    // Create telegram with delay time in the past (Unix epoch second 1 is long past)
    var pastTelegram = new Telegram(1.0, "sender", "target", 1, null);
    Field field = MessageDispatcher.class.getDeclaredField("telegrams");
    field.setAccessible(true);
    TreeSet<Telegram> telegrams = (TreeSet<Telegram>) field.get(dispatcher);
    telegrams.add(pastTelegram);
    assertDoesNotThrow(() -> dispatcher.update(0.1f));
  }

  private static class TestEntity extends AbstractEntity {

    private final boolean handleResult;

    TestEntity(String id, boolean handleResult) {
      super(id);
      this.handleResult = handleResult;
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public boolean handleMessage(Telegram msg) {
      return handleResult;
    }
  }
}
