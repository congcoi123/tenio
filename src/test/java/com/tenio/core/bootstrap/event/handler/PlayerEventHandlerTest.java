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

package com.tenio.core.bootstrap.event.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.event.handler.implement.PlayerEventHandler;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.handler.event.EventDisconnectPlayer;
import com.tenio.core.handler.event.EventPlayerConnectionResumed;
import com.tenio.core.handler.event.EventPlayerConnectionRetry;
import com.tenio.core.handler.event.EventPlayerLogin;
import com.tenio.core.handler.event.EventReceivedMessageFromPlayer;
import com.tenio.core.handler.event.EventSendMessageToPlayer;
import com.tenio.core.network.entity.session.Session;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For PlayerEventHandler")
class PlayerEventHandlerTest {

  @Test
  @DisplayName("Test initialize with all null event listeners does not throw")
  void testInitialize() {
    PlayerEventHandler playerEventHandler = new PlayerEventHandler();
    playerEventHandler.initialize(EventManager.newInstance());
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_LOGIN event dispatches to listener")
  void testPlayerLoginEvent() throws Exception {
    PlayerEventHandler handler = new PlayerEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerLogin<Player> mockEvent = mock(EventPlayerLogin.class);
    Field field = PlayerEventHandler.class.getDeclaredField("eventPlayerLogin");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    em.emit(ServerEvent.PLAYER_LOGIN, player);
    verify(mockEvent).onPlayerLogin(player);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_CONNECTION_RETRY event dispatches to listener")
  void testPlayerConnectionRetryEvent() throws Exception {
    PlayerEventHandler handler = new PlayerEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerConnectionRetry<Player, DataCollection> mockEvent =
        mock(EventPlayerConnectionRetry.class);
    Field field = PlayerEventHandler.class.getDeclaredField("eventPlayerConnectionRetry");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Session session = mock(Session.class);
    DataCollection message = mock(DataCollection.class);
    em.emit(ServerEvent.PLAYER_CONNECTION_RETRY, session, message);
    verify(mockEvent).onPlayerConnectionRetry(session, message);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_CONNECTION_RESUMED event dispatches to listener")
  void testPlayerConnectionResumedEvent() throws Exception {
    PlayerEventHandler handler = new PlayerEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerConnectionResumed<Player> mockEvent = mock(EventPlayerConnectionResumed.class);
    Field field = PlayerEventHandler.class.getDeclaredField("eventPlayerConnectionResumed");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    Session session = mock(Session.class);
    em.emit(ServerEvent.PLAYER_CONNECTION_RESUMED, player, session);
    verify(mockEvent).onPlayerConnectionResumed(player, session);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test RECEIVED_MESSAGE_FROM_PLAYER event dispatches to listener")
  void testReceivedMessageFromPlayerEvent() throws Exception {
    PlayerEventHandler handler = new PlayerEventHandler();
    EventManager em = EventManager.newInstance();
    EventReceivedMessageFromPlayer<Player, DataCollection> mockEvent =
        mock(EventReceivedMessageFromPlayer.class);
    Field field = PlayerEventHandler.class.getDeclaredField("eventReceivedMessageFromPlayer");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    DataCollection message = mock(DataCollection.class);
    em.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
    verify(mockEvent).onReceivedMessageFromPlayer(player, message);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test SEND_MESSAGE_TO_PLAYER event dispatches to listener")
  void testSendMessageToPlayerEvent() throws Exception {
    PlayerEventHandler handler = new PlayerEventHandler();
    EventManager em = EventManager.newInstance();
    EventSendMessageToPlayer<Player, DataCollection> mockEvent =
        mock(EventSendMessageToPlayer.class);
    Field field = PlayerEventHandler.class.getDeclaredField("eventSendMessageToPlayer");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    DataCollection message = mock(DataCollection.class);
    em.emit(ServerEvent.SEND_MESSAGE_TO_PLAYER, player, message);
    verify(mockEvent).onSendMessageToPlayer(player, message);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test DISCONNECT_PLAYER event dispatches to listener")
  void testDisconnectPlayerEvent() throws Exception {
    PlayerEventHandler handler = new PlayerEventHandler();
    EventManager em = EventManager.newInstance();
    EventDisconnectPlayer<Player> mockEvent = mock(EventDisconnectPlayer.class);
    Field field = PlayerEventHandler.class.getDeclaredField("eventDisconnectPlayer");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Player player = mock(Player.class);
    em.emit(ServerEvent.DISCONNECT_PLAYER, player, PlayerDisconnectMode.IDLE);
    verify(mockEvent).onDisconnectPlayer(player, PlayerDisconnectMode.IDLE);
  }
}
