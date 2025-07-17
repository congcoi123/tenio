/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.server.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.api.ServerApi;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.entity.define.result.PlayerReconnectedResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.network.entity.protocol.implement.DatagramRequest;
import com.tenio.core.network.entity.protocol.implement.SessionRequest;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ZeroProcessorServiceImplTest {

  private static final String PLAYER_IDENTITY = "test-player";
  private static final int MAX_PLAYERS = 100;
  private static final SocketAddress REMOTE_ADDRESS = new InetSocketAddress("localhost", 8080);
  @Mock
  private EventManager eventManager;
  @Mock
  private ServerApi serverApi;
  @Mock
  private DatagramChannelManager datagramChannelManager;
  @Mock
  private SessionManager sessionManager;
  @Mock
  private PlayerManager playerManager;
  @Mock
  private Session session;
  @Mock
  private Player player;
  @Mock
  private DataCollection message;
  @Mock
  private DatagramChannel datagramChannel;
  @Mock
  private NetworkReaderStatistic networkReaderStatistic;
  @Mock
  private NetworkWriterStatistic networkWriterStatistic;
  private ZeroProcessorServiceImpl processor;

  @Before
  public void setUp() {
    processor =
        ZeroProcessorServiceImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    processor.setSessionManager(sessionManager);
    processor.setPlayerManager(playerManager);
    processor.setMaxNumberPlayers(MAX_PLAYERS);
    processor.setKeepPlayerOnDisconnection(false);
    processor.setNetworkReaderStatistic(networkReaderStatistic);
    processor.setNetworkWriterStatistic(networkWriterStatistic);
    processor.initialize();
    processor.subscribe();

    // Set up common session behavior
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING))
        .thenReturn(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE))
        .thenReturn(true);
    when(session.getName())
        .thenReturn(PLAYER_IDENTITY);
    when(session.isTcp())
        .thenReturn(true);
  }

  private void processSessionWillBeClosed(Session session)
      throws Exception {
    Method method =
        ZeroProcessorServiceImpl.class.getDeclaredMethod("processSessionWillBeClosed",
            Session.class, PlayerDisconnectMode.class);
    method.setAccessible(true);
    method.invoke(processor, session, PlayerDisconnectMode.CLIENT_REQUEST);
  }

  // Connection Handling Tests
  @Test
  public void shouldEstablishNewConnectionWhenBelowMax() {
    when(playerManager.getPlayerCount()).thenReturn(MAX_PLAYERS - 1);
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING))
        .thenReturn(true);

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);

    verify(eventManager).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        eq(session), eq(message), eq(ConnectionEstablishedResult.SUCCESS));
  }

  @Test
  public void shouldRejectConnectionWhenReachedMax() throws IOException {
    when(playerManager.getPlayerCount()).thenReturn(MAX_PLAYERS);
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING))
        .thenReturn(true);

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);

    verify(eventManager).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        eq(session), eq(message), eq(ConnectionEstablishedResult.REACHED_MAX_CONNECTION));
    verify(session).close(eq(ConnectionDisconnectMode.REACHED_MAX_CONNECTION),
        eq(PlayerDisconnectMode.CONNECTION_LOST));
  }

  @Test
  public void shouldHandlePlayerReconnectionSuccessfully() {
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING))
        .thenReturn(true);
    when(eventManager.emit(eq(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE), eq(session),
        eq(message)))
        .thenReturn(Optional.of(player));
    when(player.getSession()).thenReturn(Optional.of(session));
    when(session.isActivated()).thenReturn(true);
    when(player.isInRoom()).thenReturn(false);

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);

    verify(player).setSession(session);
    verify(eventManager).emit(eq(ServerEvent.PLAYER_RECONNECTED_RESULT),
        eq(player), eq(session), eq(PlayerReconnectedResult.SUCCESS));
  }

  // Session Management Tests
  @Test
  public void shouldHandleSessionWillBeClosed() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(player);
    when(player.isInRoom()).thenReturn(false);
    when(player.getIdentity()).thenReturn(PLAYER_IDENTITY);

    processSessionWillBeClosed(session);

    verify(serverApi).unsubscribeFromAllChannels(player);
    verify(playerManager).removePlayerByIdentity(PLAYER_IDENTITY);
    verify(player).clean();
    verify(session).setName(null);
    verify(session).setAssociatedToPlayer(Session.AssociatedState.NONE);
    verify(session).remove();
    verify(eventManager).emit(eq(ServerEvent.DISCONNECT_PLAYER), eq(player),
        eq(PlayerDisconnectMode.CLIENT_REQUEST));
  }

  @Test
  public void shouldProcessSessionReadMessage() {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(player);
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_READ_MESSAGE)
        .setSender(session)
        .setMessage(message);
    reset(eventManager); // Clear previous interactions
    processor.processRequest(request);
    verify(eventManager, atLeastOnce()).emit(eq(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER),
        eq(player), eq(message));
  }

  // Datagram Channel Tests
  @Test
  public void shouldHandleDatagramChannelAccessValidation() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.containsSession()).thenReturn(true);
    when(player.getSession()).thenReturn(Optional.of(session));
    when(session.isTcp()).thenReturn(true);
    when(datagramChannelManager.getCurrentUdpConveyId()).thenReturn(1);

    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);

    processor.processRequest(request);

    verify(session).setDatagramRemoteAddress(REMOTE_ADDRESS);
    verify(sessionManager).addDatagramForSession(datagramChannel, 1, session);
    verify(eventManager).emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT),
        eq(player), eq(1), eq(AccessDatagramChannelResult.SUCCESS));
  }

  @Test
  public void shouldRejectDatagramChannelAccessWhenPlayerNotFound() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenReturn(Optional.empty());

    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE_FIRST_TIME)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);

    processor.processRequest(request);

    verify(eventManager).emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT),
        eq(null), eq(Session.EMPTY_DATAGRAM_CONVEY_ID),
        eq(AccessDatagramChannelResult.PLAYER_NOT_FOUND));
  }

  // Configuration Tests
  @Test
  public void shouldSetAndGetMaxNumberPlayers() {
    int newMaxPlayers = 200;
    processor.setMaxNumberPlayers(newMaxPlayers);
    when(playerManager.getPlayerCount()).thenReturn(newMaxPlayers);
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING))
        .thenReturn(true);

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);

    verify(eventManager).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        eq(session), eq(message), eq(ConnectionEstablishedResult.REACHED_MAX_CONNECTION));
  }

  @Test
  public void shouldSetKeepPlayerOnDisconnection() throws Exception {
    processor.setKeepPlayerOnDisconnection(true);
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(player);
    reset(eventManager); // Clear previous interactions
    processSessionWillBeClosed(session);
    verify(playerManager, never()).removePlayerByIdentity(any());
  }
} 