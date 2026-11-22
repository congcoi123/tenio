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

package com.tenio.core.scheduler.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.network.entity.session.Session;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoScanAliveMainConnectionTaskTest {

  private AutoScanAliveMainConnectionTask task;
  private PlayerManager players;
  private Player player;
  private Session session;

  @BeforeEach
  void setUp() {
    task = new AutoScanAliveMainConnectionTask();
    players = mock(PlayerManager.class);
    player = mock(Player.class);
    session = mock(Session.class);
    task.setPlayerManager(players);
    task.setTimeoutSeconds(30);
    when(players.getSnapshotPlayersList()).thenReturn(List.of(player));
    when(player.getSession()).thenReturn(Optional.of(session));
    when(session.isTcp()).thenReturn(true);
    when(session.isActivated()).thenReturn(true);
    when(session.getLastReliableReadTime()).thenReturn(1000L);
  }

  @Test
  void closesAtDeadlineUsingConnectionLostPolicy() throws IOException {
    task.scan(31000L);
    verify(session).close(ConnectionDisconnectMode.IDLE, PlayerDisconnectMode.CONNECTION_LOST);
    verifyNoInteractionsExceptRead();
  }

  private void verifyNoInteractionsExceptRead() {
    // No logout, registry removal or application room operation belongs in this task.
    verify(players).getSnapshotPlayersList();
    verifyNoMoreInteractions(players);
  }

  @Test
  void recentTcpTrafficKeepsSessionAlive() throws IOException {
    task.scan(30999L);
    verify(session, never()).close(any(), any());
  }

  @Test
  void renewedReliableTimestampExtendsDeadline() throws IOException {
    when(session.getLastReliableReadTime()).thenReturn(30000L);
    task.scan(31000L);
    verify(session, never()).close(any(), any());
  }

  @Test
  void generalReadsAndOutboundWritesCannotConcealSilentTcp() throws IOException {
    when(session.getLastReadTime()).thenReturn(31000L);
    when(session.getLastWriteTime()).thenReturn(31000L);
    task.scan(31000L);
    verify(session).close(ConnectionDisconnectMode.IDLE, PlayerDisconnectMode.CONNECTION_LOST);
    verify(session, never()).getLastReadTime();
    verify(session, never()).getLastWriteTime();
  }

  @Test
  void skipsDetachedPlayer() {
    when(player.getSession()).thenReturn(Optional.empty());
    task.scan(31000L);
    verifyNoInteractions(session);
  }

  @Test
  void skipsInactiveSession() throws IOException {
    when(session.isActivated()).thenReturn(false);
    task.scan(31000L);
    verify(session, never()).close(any(), any());
  }

  @Test
  void skipsNonTcpPrimarySession() throws IOException {
    when(session.isTcp()).thenReturn(false);
    task.scan(31000L);
    verify(session, never()).close(any(), any());
  }

  @Test
  void skipsReplacedSession() throws IOException {
    Session replacement = mock(Session.class);
    when(player.getSession()).thenReturn(Optional.of(session), Optional.of(replacement));
    task.scan(31000L);
    verify(session, never()).close(any(), any());
    verifyNoInteractions(replacement);
  }

  @Test
  void skipsFutureReadTimestampAfterClockAdjustment() throws IOException {
    when(session.getLastReliableReadTime()).thenReturn(40000L);
    task.scan(31000L);
    verify(session, never()).close(any(), any());
  }

  @Test
  void continuesAfterOneCloseFails() throws IOException {
    Player other = mock(Player.class);
    Session otherSession = mock(Session.class);
    when(other.getSession()).thenReturn(Optional.of(otherSession));
    when(otherSession.isTcp()).thenReturn(true);
    when(otherSession.isActivated()).thenReturn(true);
    when(players.getSnapshotPlayersList()).thenReturn(List.of(player, other));
    doThrow(new IOException("failed close")).when(session).close(any(), any());
    task.scan(31000L);
    verify(otherSession).close(ConnectionDisconnectMode.IDLE, PlayerDisconnectMode.CONNECTION_LOST);
  }

  @Test
  void zeroTimeoutSkipsPlayerRegistry() {
    task.setTimeoutSeconds(0);
    task.scan(Long.MAX_VALUE);
    verifyNoInteractions(players);
  }

  @Test
  void rejectsNegativeTimeout() {
    assertThrows(IllegalArgumentException.class, () -> task.setTimeoutSeconds(-1));
  }

  @Test
  void supportsLargeTimeoutWithoutIntegerOverflow() throws IOException {
    task.setTimeoutSeconds(Integer.MAX_VALUE);
    task.scan(31000L);
    verify(session, never()).close(any(), any());
  }

  @Test
  void continuesAfterOnePlayerLookupThrowsRuntimeException() throws IOException {
    Player brokenPlayer = mock(Player.class);
    when(brokenPlayer.getSession()).thenThrow(new IllegalStateException("player unavailable"));
    when(players.getSnapshotPlayersList()).thenReturn(List.of(brokenPlayer, player));

    assertDoesNotThrow(() -> task.scan(31000L));

    verify(session).close(ConnectionDisconnectMode.IDLE, PlayerDisconnectMode.CONNECTION_LOST);
  }
}
