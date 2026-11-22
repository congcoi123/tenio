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

package com.tenio.core.entity.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.event.implement.EventManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ChannelImpl")
class ChannelImplTest {

  private EventManager eventManager;
  private Channel channel;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    channel = ChannelImpl.newInstance("ch1", eventManager, "test channel");
  }

  @Test
  @DisplayName("newInstance returns a non-null channel")
  void testNewInstanceReturnsNonNull() {
    assertNotNull(ChannelImpl.newInstance("id", eventManager, "desc"));
  }

  @Test
  @DisplayName("getId returns the id provided at creation")
  void testGetIdReturnsProvidedId() {
    assertEquals("ch1", channel.getId());
  }

  @Test
  @DisplayName("getDescription returns the description provided at creation")
  void testGetDescriptionReturnsInitialDescription() {
    assertEquals("test channel", channel.getDescription());
  }

  @Test
  @DisplayName("setDescription updates the description")
  void testSetDescriptionUpdatesValue() {
    channel.setDescription("new desc");
    assertEquals("new desc", channel.getDescription());
  }

  @Test
  @DisplayName("getPlayers is empty initially")
  void testGetPlayersInitiallyEmpty() {
    assertTrue(channel.getPlayers().isEmpty());
  }

  @Test
  @DisplayName("countPlayer returns 0 initially")
  void testCountPlayerInitiallyZero() {
    assertEquals(0, channel.countPlayer());
  }

  @Test
  @DisplayName("containsPlayer returns false for unknown identity")
  void testContainsPlayerReturnsFalseForUnknown() {
    assertFalse(channel.containsPlayer("nobody"));
  }

  @Test
  @DisplayName("addPlayer adds the player to the map and emits PLAYER_SUBSCRIBED_CHANNEL")
  void testAddPlayerAddsToMapAndEmitsEvent() {
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");

    channel.addPlayer(player);

    assertEquals(1, channel.countPlayer());
    assertTrue(channel.containsPlayer("p1"));
    verify(eventManager).emit(ServerEvent.PLAYER_SUBSCRIBED_CHANNEL, channel, player);
  }

  @Test
  @DisplayName("removePlayer removes the player from the map and emits PLAYER_UNSUBSCRIBED_CHANNEL")
  void testRemovePlayerRemovesFromMapAndEmitsEvent() {
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    channel.addPlayer(player);

    channel.removePlayer(player);

    assertEquals(0, channel.countPlayer());
    assertFalse(channel.containsPlayer("p1"));
    verify(eventManager).emit(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, channel, player);
  }

  @Test
  @DisplayName("removePlayers removes all players and emits PLAYER_UNSUBSCRIBED_CHANNEL for each")
  void testRemovePlayersRemovesAllAndEmitsForEach() {
    var p1 = mock(Player.class);
    var p2 = mock(Player.class);
    when(p1.getIdentity()).thenReturn("p1");
    when(p2.getIdentity()).thenReturn("p2");
    channel.addPlayer(p1);
    channel.addPlayer(p2);

    channel.removePlayers();

    assertEquals(0, channel.countPlayer());
    verify(eventManager, times(1)).emit(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, channel, p1);
    verify(eventManager, times(1)).emit(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, channel, p2);
  }

  @Test
  @DisplayName("getReadonlyPlayers returns a list of current players")
  void testGetReadonlyPlayersReturnsCurrentPlayers() {
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    channel.addPlayer(player);

    assertEquals(1, channel.getReadonlyPlayers().size());
    assertTrue(channel.getReadonlyPlayers().contains(player));
  }

  @Test
  @DisplayName("containsPlayer returns true after the player is added")
  void testContainsPlayerReturnsTrueAfterAdd() {
    var player = mock(Player.class);
    when(player.getIdentity()).thenReturn("p1");
    channel.addPlayer(player);

    assertTrue(channel.containsPlayer("p1"));
  }

  @Test
  @DisplayName("equals returns true for channels with the same id")
  void testEqualsBasedOnSameId() {
    Channel other = ChannelImpl.newInstance("ch1", mock(EventManager.class), "other");
    assertEquals(channel, other);
  }

  @Test
  @DisplayName("equals returns false for channels with different ids")
  void testEqualsReturnsFalseForDifferentId() {
    Channel other = ChannelImpl.newInstance("ch2", eventManager, "test");
    assertNotEquals(channel, other);
  }

  @Test
  @DisplayName("hashCode is the same for channels with equal ids")
  void testHashCodeEqualForSameId() {
    Channel other = ChannelImpl.newInstance("ch1", mock(EventManager.class), "other");
    assertEquals(channel.hashCode(), other.hashCode());
  }

  @Test
  @DisplayName("toString contains the channel id")
  void testToStringContainsId() {
    assertTrue(channel.toString().contains("ch1"));
  }

  @Test
  @DisplayName("toString contains the player count")
  void testToStringContainsPlayerCount() {
    assertTrue(channel.toString().contains("playerCount=0"));
  }
}
