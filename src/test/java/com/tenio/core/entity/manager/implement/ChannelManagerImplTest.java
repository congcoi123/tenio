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

package com.tenio.core.entity.manager.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.implement.ChannelImpl;
import com.tenio.core.entity.manager.ChannelManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ChannelNotExistException;
import com.tenio.core.exception.CreatedDuplicatedChannelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("Unit Test Cases For ChannelManagerImpl")
class ChannelManagerImplTest {

  private EventManager eventManager;
  private ChannelManager manager;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    manager = ChannelManagerImpl.newInstance(eventManager);
  }

  private Channel captureCreatedChannel(String id) {
    manager.createChannel(id, "desc");
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(eventManager).emit(eq(ServerEvent.CHANNEL_CREATED), captor.capture());
    return (Channel) captor.getValue();
  }

  @Test
  @DisplayName("newInstance returns a non-null manager")
  void testNewInstanceReturnsNonNull() {
    assertNotNull(ChannelManagerImpl.newInstance(mock(EventManager.class)));
  }

  @Test
  @DisplayName("createChannel emits CHANNEL_CREATED event")
  void testCreateChannelEmitsChannelCreatedEvent() {
    manager.createChannel("ch1", "test");
    verify(eventManager).emit(eq(ServerEvent.CHANNEL_CREATED), any());
  }

  @Test
  @DisplayName("createChannel with duplicate id throws CreatedDuplicatedChannelException")
  void testCreateChannelDuplicateIdThrows() {
    manager.createChannel("ch1", "test");
    assertThrows(CreatedDuplicatedChannelException.class,
        () -> manager.createChannel("ch1", "other"));
  }

  @Test
  @DisplayName("removeChannel emits CHANNEL_WILL_BE_REMOVED and removes players")
  void testRemoveChannelEmitsEventAndRemovesPlayers() {
    Channel channel = captureCreatedChannel("ch1");
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    channel.addPlayer(player);

    manager.removeChannel("ch1");

    verify(eventManager).emit(eq(ServerEvent.CHANNEL_WILL_BE_REMOVED), eq(channel));
    assertEquals(0, channel.countPlayer());
  }

  @Test
  @DisplayName("removeChannel for nonexistent id does nothing")
  void testRemoveChannelForNonexistentIdDoesNothing() {
    manager.removeChannel("nonexistent");
    verify(eventManager, never()).emit(eq(ServerEvent.CHANNEL_WILL_BE_REMOVED), any());
  }

  @Test
  @DisplayName("subscribe adds the player to the channel")
  void testSubscribeAddsPlayerToChannel() {
    Channel channel = captureCreatedChannel("ch1");
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");

    manager.subscribe(channel, player);

    assertEquals(1, channel.countPlayer());
    assertTrue(channel.containsPlayer("p1"));
  }

  @Test
  @DisplayName("subscribe with null channel throws ChannelNotExistException")
  void testSubscribeWithNullChannelThrows() {
    var player = mock(Player.class);
    assertThrows(ChannelNotExistException.class, () -> manager.subscribe(null, player));
  }

  @Test
  @DisplayName("subscribe with null player does nothing")
  void testSubscribeWithNullPlayerDoesNothing() {
    Channel channel = mock(Channel.class);
    manager.subscribe(channel, null);
    verify(channel, never()).addPlayer(any());
  }

  @Test
  @DisplayName("unsubscribe removes the player from the channel")
  void testUnsubscribeFromChannelRemovesPlayer() {
    Channel channel = captureCreatedChannel("ch1");
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    channel.addPlayer(player);

    manager.unsubscribe(channel, player);

    assertEquals(0, channel.countPlayer());
  }

  @Test
  @DisplayName("unsubscribe with null channel does nothing")
  void testUnsubscribeWithNullChannelDoesNothing() {
    var player = mock(Player.class);
    manager.unsubscribe(null, player);
    // No exception expected
  }

  @Test
  @DisplayName("unsubscribe with null player does nothing")
  void testUnsubscribeWithNullPlayerDoesNothing() {
    Channel channel = mock(Channel.class);
    manager.unsubscribe(channel, null);
    verify(channel, never()).removePlayer(any());
  }

  @Test
  @DisplayName("unsubscribe(player) removes player from all channels")
  void testUnsubscribePlayerFromAllChannels() {
    Channel ch1 = captureCreatedChannel("ch1");
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    ch1.addPlayer(player);
    assertEquals(1, ch1.countPlayer());

    manager.unsubscribe(player);

    assertEquals(0, ch1.countPlayer());
  }

  @Test
  @DisplayName("unsubscribe(null player) does nothing")
  void testUnsubscribeNullPlayerDoesNothing() {
    manager.unsubscribe((Player) null);
    // No exception expected
  }

  @Test
  @DisplayName("broadcast emits BROADCAST_TO_CHANNEL for each subscribed player")
  void testBroadcastEmitsForEachSubscribedPlayer() {
    var channel = ChannelImpl.newInstance("ch1", eventManager, "test");
    var p1 = mock(Player.class);
    var p2 = mock(Player.class);
    when(p1.getIdentity()).thenReturn("p1");
    when(p2.getIdentity()).thenReturn("p2");
    channel.addPlayer(p1);
    channel.addPlayer(p2);
    var message = mock(DataCollection.class);

    manager.broadcast(channel, message);

    verify(eventManager).emit(ServerEvent.BROADCAST_TO_CHANNEL, channel, p1, message);
    verify(eventManager).emit(ServerEvent.BROADCAST_TO_CHANNEL, channel, p2, message);
  }

  @Test
  @DisplayName("broadcast with null channel throws ChannelNotExistException")
  void testBroadcastWithNullChannelThrows() {
    assertThrows(ChannelNotExistException.class,
        () -> manager.broadcast(null, mock(DataCollection.class)));
  }

  @Test
  @DisplayName("getSubscribedChannelsForPlayer returns channels the player is subscribed to")
  void testGetSubscribedChannelsForPlayerReturnsSubscribedChannels() {
    Channel ch1 = captureCreatedChannel("ch1");
    // need fresh verify for ch2
    manager.createChannel("ch2", "desc2");

    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    manager.subscribe(ch1, player);

    var result = manager.getSubscribedChannelsForPlayer(player);

    assertEquals(1, result.size());
    assertTrue(result.containsKey("ch1"));
  }

  @Test
  @DisplayName("getSubscribedChannelsForPlayer returns empty map when player is not in any channel")
  void testGetSubscribedChannelsForPlayerReturnsEmptyWhenNoSubscriptions() {
    captureCreatedChannel("ch1");
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");

    var result = manager.getSubscribedChannelsForPlayer(player);

    assertTrue(result.isEmpty());
  }
}
