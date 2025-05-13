package com.tenio.engine.fsm.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.engine.fsm.EntityManager;
import com.tenio.engine.fsm.MessageDispatcher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FsmComponentTest {
  @Test
  void testUpdate() {
    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.update(0.5);
    assertNull(fsmComponent.getCurrentState());
    assertNull(fsmComponent.getPreviousState());
    assertNull(fsmComponent.getGlobalState());
  }

  @Test
  void testUpdate2() {
    State<Object> state = (State<Object>) mock(State.class);
    doNothing().when(state).execute((Object) any());

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setGlobalState(state);
    fsmComponent.update(0.5);
    verify(state).execute((Object) any());
  }

  @Test
  void testUpdate3() {
    State<Object> state = (State<Object>) mock(State.class);
    doNothing().when(state).execute((Object) any());
    State<Object> state1 = (State<Object>) mock(State.class);
    doNothing().when(state1).execute((Object) any());

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setCurrentState(state1);
    fsmComponent.setGlobalState(state);
    fsmComponent.update(0.5);
    verify(state1).execute((Object) any());
    verify(state).execute((Object) any());
  }

  @Test
  void testHandleMessage() {
    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    assertFalse(fsmComponent.handleMessage(new Telegram()));
  }

  @Test
  void testHandleMessage2() {
    State<Object> state = (State<Object>) mock(State.class);
    when(state.onMessage((Object) any(), (Telegram) any())).thenReturn(true);

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setGlobalState(state);
    assertTrue(fsmComponent.handleMessage(new Telegram()));
    verify(state).onMessage((Object) any(), (Telegram) any());
  }

  @Test
  void testHandleMessage3() {
    State<Object> state = (State<Object>) mock(State.class);
    when(state.onMessage((Object) any(), (Telegram) any())).thenReturn(false);

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setGlobalState(state);
    assertFalse(fsmComponent.handleMessage(new Telegram()));
    verify(state).onMessage((Object) any(), (Telegram) any());
  }

  @Test
  void testHandleMessage4() {
    State<Object> state = (State<Object>) mock(State.class);
    when(state.onMessage((Object) any(), (Telegram) any())).thenReturn(true);
    State<Object> state1 = (State<Object>) mock(State.class);
    when(state1.onMessage((Object) any(), (Telegram) any())).thenReturn(true);

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setCurrentState(state1);
    fsmComponent.setGlobalState(state);
    assertTrue(fsmComponent.handleMessage(new Telegram()));
    verify(state1).onMessage((Object) any(), (Telegram) any());
  }

  @Test
  void testHandleMessage5() {
    State<Object> state = (State<Object>) mock(State.class);
    when(state.onMessage((Object) any(), (Telegram) any())).thenReturn(true);
    State<Object> state1 = (State<Object>) mock(State.class);
    when(state1.onMessage((Object) any(), (Telegram) any())).thenReturn(false);

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setCurrentState(state1);
    fsmComponent.setGlobalState(state);
    assertTrue(fsmComponent.handleMessage(new Telegram()));
    verify(state1).onMessage((Object) any(), (Telegram) any());
    verify(state).onMessage((Object) any(), (Telegram) any());
  }

  @Test
  @Disabled
  void testChangeState() {
    State<Object> state = (State<Object>) mock(State.class);
    doNothing().when(state).exit((Object) any());

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setCurrentState(state);
    State<Object> state1 = (State<Object>) mock(State.class);
    doNothing().when(state1).enter((Object) any());
    fsmComponent.changeState(state1);
    verify(state).exit(any());
    verify(state1).enter(any());
    assertEquals("com.tenio.engine.fsm.entity.State$MockitoMock$1753640723",
        fsmComponent.getNameOfCurrentState());
  }

  @Test
  @Disabled
  void testRevertToPreviousState() {
    State<Object> state = (State<Object>) mock(State.class);
    doNothing().when(state).exit((Object) any());
    State<Object> state1 = (State<Object>) mock(State.class);
    doNothing().when(state1).enter((Object) any());

    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setPreviousState(state1);
    fsmComponent.setCurrentState(state);
    fsmComponent.revertToPreviousState();
    verify(state1).enter(any());
    verify(state).exit(any());
    assertEquals("com.tenio.engine.fsm.entity.State$MockitoMock$1753640723",
        fsmComponent.getNameOfCurrentState());
  }

  @Test
  void testIsInState() {
    FsmComponent<Object> fsmComponent =
        new FsmComponent<Object>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setCurrentState((State<Object>) mock(State.class));
    assertTrue(fsmComponent.isInState((State<Object>) mock(State.class)));
  }

  @Test
  @Disabled
  void testGetNameOfCurrentState() {
    FsmComponent<Object> fsmComponent =
        new FsmComponent<>(new MessageDispatcher(new EntityManager()), "Entity");
    fsmComponent.setCurrentState((State<Object>) mock(State.class));
    assertEquals("com.tenio.engine.fsm.entity.State$MockitoMock$1753640723",
        fsmComponent.getNameOfCurrentState());
  }

  @Test
  void testConstructor() {
    MessageDispatcher messageDispatcher = new MessageDispatcher(new EntityManager());
    FsmComponent<Object> actualFsmComponent = new FsmComponent<Object>(messageDispatcher, "Entity");
    actualFsmComponent.setCurrentState(null);
    actualFsmComponent.setGlobalState(null);
    actualFsmComponent.setPreviousState(null);
    assertNull(actualFsmComponent.getCurrentState());
    assertSame(messageDispatcher, actualFsmComponent.getDispatcher());
    assertNull(actualFsmComponent.getGlobalState());
    assertNull(actualFsmComponent.getPreviousState());
  }
}

