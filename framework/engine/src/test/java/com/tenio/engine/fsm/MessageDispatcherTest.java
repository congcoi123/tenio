package com.tenio.engine.fsm;

import static org.mockito.Mockito.mock;

import com.tenio.engine.message.ExtraMessage;
import org.junit.jupiter.api.Test;

class MessageDispatcherTest {
  @Test
  void testConstructor() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     MessageDispatcher.telegrams
    //     MessageDispatcher.entityManager
    //     MessageDispatcher.messageListeners

    new MessageDispatcher(new EntityManager());
  }

  @Test
  void testDispatchMessage() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by dispatchMessage(double, String, String, int, ExtraMessage)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new MessageDispatcher(new EntityManager())).dispatchMessage(10.0, "Sender", "Receiver", 1,
        mock(ExtraMessage.class));
  }

  @Test
  void testUpdate() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by update(float)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new MessageDispatcher(new EntityManager())).update(0.5f);
  }

  @Test
  void testListen() {
    // TODO: This test is incomplete.
    //   Reason: R004 No meaningful assertions found.
    //   Diffblue Cover was unable to create an assertion.
    //   Make sure that fields modified by listen(MessageListener)
    //   have package-private, protected, or public getters.
    //   See https://diff.blue/R004 to resolve this issue.

    (new MessageDispatcher(new EntityManager())).listen(mock(MessageListener.class));
  }

  @Test
  void testClear() {
    // TODO: This test is incomplete.
    //   Reason: R002 Missing observers.
    //   Diffblue Cover was unable to create an assertion.
    //   Add getters for the following fields or make them package-private:
    //     MessageDispatcher.entityManager
    //     MessageDispatcher.messageListeners
    //     MessageDispatcher.telegrams

    (new MessageDispatcher(new EntityManager())).clear();
  }
}

