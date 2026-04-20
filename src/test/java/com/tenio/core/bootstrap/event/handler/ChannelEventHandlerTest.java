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
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.event.handler.implement.ChannelEventHandler;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.handler.event.EventBroadcastToChannel;
import com.tenio.core.handler.event.EventChannelCreated;
import com.tenio.core.handler.event.EventChannelWillBeRemoved;
import com.tenio.core.handler.event.EventPlayerSubscribedChannel;
import com.tenio.core.handler.event.EventPlayerUnsubscribedChannel;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ChannelEventHandler")
class ChannelEventHandlerTest {

  @Test
  @DisplayName("Test initialize with all null event listeners does not throw")
  void testInitialize() {
    ChannelEventHandler handler = new ChannelEventHandler();
    handler.initialize(EventManager.newInstance());
  }

  @Test
  @DisplayName("Test CHANNEL_CREATED event dispatches to listener")
  void testChannelCreatedEvent() throws Exception {
    ChannelEventHandler handler = new ChannelEventHandler();
    EventManager em = EventManager.newInstance();
    EventChannelCreated mockEvent = mock(EventChannelCreated.class);
    Field field = ChannelEventHandler.class.getDeclaredField("eventChannelCreated");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Channel channel = mock(Channel.class);
    em.emit(ServerEvent.CHANNEL_CREATED, channel);
    verify(mockEvent).onChannelCreated(channel);
  }

  @Test
  @DisplayName("Test CHANNEL_WILL_BE_REMOVED event dispatches to listener")
  void testChannelWillBeRemovedEvent() throws Exception {
    ChannelEventHandler handler = new ChannelEventHandler();
    EventManager em = EventManager.newInstance();
    EventChannelWillBeRemoved mockEvent = mock(EventChannelWillBeRemoved.class);
    Field field = ChannelEventHandler.class.getDeclaredField("eventChannelWillBeRemoved");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Channel channel = mock(Channel.class);
    em.emit(ServerEvent.CHANNEL_WILL_BE_REMOVED, channel);
    verify(mockEvent).onChannelWillBeRemoved(channel);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_SUBSCRIBED_CHANNEL event dispatches to listener")
  void testPlayerSubscribedChannelEvent() throws Exception {
    ChannelEventHandler handler = new ChannelEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerSubscribedChannel<Player> mockEvent = mock(EventPlayerSubscribedChannel.class);
    Field field = ChannelEventHandler.class.getDeclaredField("eventPlayerSubscribedChannel");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Channel channel = mock(Channel.class);
    Player player = mock(Player.class);
    em.emit(ServerEvent.PLAYER_SUBSCRIBED_CHANNEL, channel, player);
    verify(mockEvent).onPlayerSubscribedChannel(channel, player);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test PLAYER_UNSUBSCRIBED_CHANNEL event dispatches to listener")
  void testPlayerUnsubscribedChannelEvent() throws Exception {
    ChannelEventHandler handler = new ChannelEventHandler();
    EventManager em = EventManager.newInstance();
    EventPlayerUnsubscribedChannel<Player> mockEvent = mock(EventPlayerUnsubscribedChannel.class);
    Field field = ChannelEventHandler.class.getDeclaredField("eventPlayerUnsubscribedChannel");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Channel channel = mock(Channel.class);
    Player player = mock(Player.class);
    em.emit(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, channel, player);
    verify(mockEvent).onPlayerUnsubscribedChannel(channel, player);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Test BROADCAST_TO_CHANNEL event dispatches to listener")
  void testBroadcastToChannelEvent() throws Exception {
    ChannelEventHandler handler = new ChannelEventHandler();
    EventManager em = EventManager.newInstance();
    EventBroadcastToChannel<Player, DataCollection> mockEvent =
        mock(EventBroadcastToChannel.class);
    Field field = ChannelEventHandler.class.getDeclaredField("eventBroadcastToChannel");
    field.setAccessible(true);
    field.set(handler, mockEvent);
    handler.initialize(em);
    em.subscribe();
    Channel channel = mock(Channel.class);
    Player player = mock(Player.class);
    DataCollection message = mock(DataCollection.class);
    em.emit(ServerEvent.BROADCAST_TO_CHANNEL, channel, player, message);
    verify(mockEvent).onBroadcastToChannel(channel, player, message);
  }
}
