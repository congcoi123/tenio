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

package com.tenio.core.server.core;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenio.common.data.DataCollection;
import com.tenio.core.api.ServerApi;
import com.tenio.core.processor.AbstractProcessor;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.result.AccessDatagramChannelResult;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.inbound.Request;
import com.tenio.core.network.entity.inbound.implement.DatagramRequest;
import com.tenio.core.network.entity.inbound.implement.SessionRequest;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.entity.inbound.policy.RequestPolicy;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@DisplayName("Unit Test Cases For ZeroProcessorImpl")
@RunWith(MockitoJUnitRunner.class)
public class ZeroProcessorImplTest {

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
  private ZeroProcessorImpl processor;

  @Before
  public void setUp() {
    processor = ZeroProcessorImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    processor.setSessionManager(sessionManager);
    processor.setPlayerManager(playerManager);
    processor.setMaxNumberPlayers(MAX_PLAYERS);
    processor.setKeepPlayerOnDisconnection(false);
    processor.setNetworkReaderStatistic(networkReaderStatistic);
    processor.setNetworkWriterStatistic(networkWriterStatistic);
    processor.initialize();
    processor.subscribe(true, true);

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

  private void processSessionWillBeClosed(Session session) throws Exception {
    Method method =
        ZeroProcessorImpl.class.getDeclaredMethod("processSessionWillBeClosed",
            Session.class, PlayerDisconnectMode.class);
    method.setAccessible(true);
    method.invoke(processor, session, PlayerDisconnectMode.CLIENT_REQUEST);
  }

  private void processSessionReadMessage(Session session, DataCollection message) throws Exception {
    Method method =
            ZeroProcessorImpl.class.getDeclaredMethod("processSessionReadMessage",
                    Session.class, DataCollection.class);
    method.setAccessible(true);
    method.invoke(processor, session, message);
  }

  // Connection Handling Tests
  @Test
  public void shouldEstablishNewConnectionWhenBelowMax() {
    when(playerManager.getSnapshotPlayerCount()).thenReturn(MAX_PLAYERS - 1);
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
  public void shouldRejectConnectionWhenReachedMax() throws Exception {
    when(playerManager.getSnapshotPlayerCount()).thenReturn(MAX_PLAYERS);
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
    when(eventManager.emit(eq(ServerEvent.PLAYER_CONNECTION_RETRY), eq(session),
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
    verify(eventManager).emit(eq(ServerEvent.PLAYER_CONNECTION_RESUMED), eq(player), eq(session));
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
  public void shouldProcessSessionReadMessage() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(player);

    processSessionReadMessage(session, message);

    verify(session).setLastReadTime(any(Long.class));
    verify(session).increaseReadMessages();
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
            .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
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
            .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
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
    when(playerManager.getSnapshotPlayerCount()).thenReturn(newMaxPlayers);
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

  @Test
  public void shouldReturnEarlyWhenSessionNotActivated() {
    // session.isActivated() returns false by default in Mockito
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);
    processor.processRequest(request);
    verify(eventManager, never()).emit(eq(ServerEvent.PLAYER_CONNECTION_RETRY), any(), any());
    verify(eventManager, never()).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        any(), any(), any());
  }

  @Test
  public void shouldReturnEarlyWhenTransitionStateFails() {
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING)).thenReturn(false);
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);
    processor.processRequest(request);
    verify(eventManager, never()).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        any(), any(), any());
  }

  @Test
  public void shouldHandleSessionWillBeClosedWhenNotAssociatedToDone() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(false);
    processSessionWillBeClosed(session);
    verify(playerManager, never()).getPlayerByIdentity(any());
    verify(session).setName(null);
    verify(session).setAssociatedToPlayer(Session.AssociatedState.NONE);
    verify(session).remove();
  }

  @Test
  public void shouldHandleSessionWillBeClosedWhenPlayerIsInRoom() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(player);
    when(player.isInRoom()).thenReturn(true);
    when(player.getIdentity()).thenReturn(PLAYER_IDENTITY);
    processSessionWillBeClosed(session);
    verify(serverApi).leaveRoom(eq(player), any());
  }

  @Test
  public void shouldHandleSessionWillBeClosedWhenPlayerIsNull() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(null);
    processSessionWillBeClosed(session);
    verify(eventManager, never()).emit(eq(ServerEvent.DISCONNECT_PLAYER), any(), any());
    verify(session).setName(null);
    verify(session).remove();
  }

  @Test
  public void shouldHandleSessionReadMessageWhenNotAssociated() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(false);
    processSessionReadMessage(session, message);
    verify(session).setLastReadTime(any(Long.class));
    verify(session).increaseReadMessages();
    verify(eventManager, never()).emit(eq(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER), any(), any());
  }

  @Test
  public void shouldHandleSessionReadMessageWhenPlayerIsNull() throws Exception {
    when(session.isAssociatedToPlayer(Session.AssociatedState.DONE)).thenReturn(true);
    when(session.getName()).thenReturn(PLAYER_IDENTITY);
    when(playerManager.getPlayerByIdentity(PLAYER_IDENTITY)).thenReturn(null);
    processSessionReadMessage(session, message);
    verify(session).setLastReadTime(any(Long.class));
    verify(session).increaseReadMessages();
    verify(eventManager, never()).emit(eq(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER), any(), any());
  }

  @Test
  public void shouldReturnWhenDatagramResultIsNotOptional() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenReturn("not-an-optional");
    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);
    processor.processRequest(request);
    verify(eventManager, never()).emit(
        eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT), any(), any(), any());
  }

  @Test
  public void shouldHandleDatagramAccessWhenPlayerHasNoSession() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.containsSession()).thenReturn(false);
    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);
    processor.processRequest(request);
    verify(eventManager).emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT),
        eq(player), eq(Session.EMPTY_DATAGRAM_CONVEY_ID),
        eq(AccessDatagramChannelResult.SESSION_NOT_FOUND));
  }

  @Test
  public void shouldHandleDatagramAccessWhenSessionIsNotTcp() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.containsSession()).thenReturn(true);
    when(player.getSession()).thenReturn(Optional.of(session));
    when(session.isTcp()).thenReturn(false);
    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);
    processor.processRequest(request);
    verify(eventManager).emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT),
        eq(player), eq(Session.EMPTY_DATAGRAM_CONVEY_ID),
        eq(AccessDatagramChannelResult.INVALID_SESSION_PROTOCOL));
  }

  @Test
  public void shouldCloseOldSessionOnReconnectionWhenCurrentSessionIsActivated() throws Exception {
    Session currentSession = mock(Session.class);
    when(currentSession.isActivated()).thenReturn(true);
    when(currentSession.getName()).thenReturn("old-name");
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING)).thenReturn(true);
    when(eventManager.emit(eq(ServerEvent.PLAYER_CONNECTION_RETRY), eq(session), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.getSession()).thenReturn(Optional.of(currentSession));
    when(player.isInRoom()).thenReturn(false);
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);
    processor.processRequest(request);
    verify(currentSession).close(
        eq(ConnectionDisconnectMode.RECONNECTION), eq(PlayerDisconnectMode.RECONNECTION));
  }

  @Test
  public void shouldLeaveRoomOnReconnectionWhenPlayerIsInRoom() {
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING)).thenReturn(true);
    when(eventManager.emit(eq(ServerEvent.PLAYER_CONNECTION_RETRY), eq(session), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.getSession()).thenReturn(Optional.empty());
    when(player.isInRoom()).thenReturn(true);
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);
    processor.processRequest(request);
    verify(serverApi).leaveRoom(eq(player), eq(PlayerLeaveRoomMode.RECONNECTION));
  }

  @Test
  public void shouldCoverSubscribeLambdaForSessionRequestConnection() {
    var realEventManager = EventManager.newInstance();
    var realProcessor = ZeroProcessorImpl.newInstance(
        realEventManager, serverApi, datagramChannelManager);
    realProcessor.setSessionManager(sessionManager);
    realProcessor.setPlayerManager(playerManager);
    realProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    realProcessor.setKeepPlayerOnDisconnection(false);
    realProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    realProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    realProcessor.initialize();
    realProcessor.subscribe(true, true);
    realEventManager.subscribe();
    realEventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
    realProcessor.shutdown();
  }

  @Test
  public void shouldCoverSubscribeLambdaForSessionWillBeClosed() {
    var realEventManager = EventManager.newInstance();
    var realProcessor = ZeroProcessorImpl.newInstance(
        realEventManager, serverApi, datagramChannelManager);
    realProcessor.setSessionManager(sessionManager);
    realProcessor.setPlayerManager(playerManager);
    realProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    realProcessor.setKeepPlayerOnDisconnection(false);
    realProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    realProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    realProcessor.initialize();
    realProcessor.subscribe(true, true);
    realEventManager.subscribe();
    // SESSION_WILL_BE_CLOSED lambda calls processSessionWillBeClosed synchronously
    realEventManager.emit(ServerEvent.SESSION_WILL_BE_CLOSED, session,
        null, PlayerDisconnectMode.CLIENT_REQUEST);
    realProcessor.shutdown();
  }

  @Test
  public void shouldCoverSubscribeLambdaForSessionReadMessage() {
    var realEventManager = EventManager.newInstance();
    var realProcessor = ZeroProcessorImpl.newInstance(
        realEventManager, serverApi, datagramChannelManager);
    realProcessor.setSessionManager(sessionManager);
    realProcessor.setPlayerManager(playerManager);
    realProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    realProcessor.setKeepPlayerOnDisconnection(false);
    realProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    realProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    realProcessor.initialize();
    realProcessor.subscribe(true, true);
    realEventManager.subscribe();
    realEventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
    realProcessor.shutdown();
  }

  @Test
  public void shouldCoverSubscribeLambdaForDatagramChannelRequest() {
    var realEventManager = EventManager.newInstance();
    var realProcessor = ZeroProcessorImpl.newInstance(
        realEventManager, serverApi, datagramChannelManager);
    realProcessor.setSessionManager(sessionManager);
    realProcessor.setPlayerManager(playerManager);
    realProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    realProcessor.setKeepPlayerOnDisconnection(false);
    realProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    realProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    realProcessor.initialize();
    realProcessor.subscribe(true, true);
    realEventManager.subscribe();
    realEventManager.emit(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS,
        datagramChannel, REMOTE_ADDRESS, message);
    realProcessor.shutdown();
  }

  @Test
  public void shouldCoverNoArgSubscribeMethod() {
    processor.subscribe();
  }

  @Test
  public void shouldCoverLifecycleNoOpMethods() throws Exception {
    Method onInit = ZeroProcessorImpl.class.getDeclaredMethod("onInitialized");
    onInit.setAccessible(true);
    onInit.invoke(processor);

    Method onStarted = ZeroProcessorImpl.class.getDeclaredMethod("onStarted");
    onStarted.setAccessible(true);
    onStarted.invoke(processor);

    Method onRunning = ZeroProcessorImpl.class.getDeclaredMethod("onRunning");
    onRunning.setAccessible(true);
    onRunning.invoke(processor);

    Method onShutdown = ZeroProcessorImpl.class.getDeclaredMethod("onShutdown");
    onShutdown.setAccessible(true);
    onShutdown.invoke(processor);
  }

  @Test
  public void shouldCoverSetRequestPolicyAndLambdaPolicyPath() {
    RequestPolicy requestPolicy = mock(RequestPolicy.class);
    processor.setRequestPolicy(requestPolicy);

    var realEventManager = EventManager.newInstance();
    var realProcessor = ZeroProcessorImpl.newInstance(
        realEventManager, serverApi, datagramChannelManager);
    realProcessor.setSessionManager(sessionManager);
    realProcessor.setPlayerManager(playerManager);
    realProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    realProcessor.setKeepPlayerOnDisconnection(false);
    realProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    realProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    realProcessor.setRequestPolicy(requestPolicy);
    realProcessor.initialize();
    realProcessor.subscribe(true, true);
    realEventManager.subscribe();
    realEventManager.emit(ServerEvent.SESSION_REQUEST_CONNECTION, session, message);
    realEventManager.emit(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS,
        datagramChannel, REMOTE_ADDRESS, message);
    realProcessor.shutdown();
  }

  @Test
  public void shouldHandleExceptionThrownByPlayerConnectionRetryEmit() {
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING)).thenReturn(true);
    when(eventManager.emit(eq(ServerEvent.PLAYER_CONNECTION_RETRY), any(), any()))
        .thenThrow(new RuntimeException("retry failed"));
    when(playerManager.getSnapshotPlayerCount()).thenReturn(0);

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);

    verify(eventManager).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        eq(session), eq(message), any());
  }

  @Test
  public void shouldHandleIOExceptionFromCurrentSessionCloseOnReconnection() throws Exception {
    Session currentSession = mock(Session.class);
    when(currentSession.isActivated()).thenReturn(true);
    when(currentSession.getName()).thenReturn("old-session");
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING)).thenReturn(true);
    when(eventManager.emit(eq(ServerEvent.PLAYER_CONNECTION_RETRY), eq(session), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.getSession()).thenReturn(Optional.of(currentSession));
    when(player.isInRoom()).thenReturn(false);
    org.mockito.Mockito.doThrow(new IOException("close error"))
        .when(currentSession).close(any(), any());

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);
    verify(player).setSession(session);
  }

  @Test
  public void shouldHandleIOExceptionFromSessionCloseAtMaxCapacity() throws Exception {
    when(playerManager.getSnapshotPlayerCount()).thenReturn(MAX_PLAYERS);
    when(session.isActivated()).thenReturn(true);
    when(session.transitionAssociatedState(Session.AssociatedState.NONE,
        Session.AssociatedState.DOING)).thenReturn(true);
    org.mockito.Mockito.doThrow(new IOException("close error")).when(session).close(any(), any());

    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);

    processor.processRequest(request);
    verify(eventManager).emit(eq(ServerEvent.CONNECTION_ESTABLISHED_RESULT),
        eq(session), eq(message), eq(com.tenio.core.entity.define.result.ConnectionEstablishedResult.REACHED_MAX_CONNECTION));
  }

  @Test
  public void shouldHandleExceptionThrownByDatagramValidationEmit() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenThrow(new RuntimeException("validation failed"));

    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);

    processor.processRequest(request);
    verify(eventManager, org.mockito.Mockito.never()).emit(
        eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT), any(), any(), any());
  }

  @Test
  public void shouldHandleDatagramAccessWhenPlayerContainsSessionButSessionIsEmpty() {
    when(eventManager.emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION), eq(message)))
        .thenReturn(Optional.of(player));
    when(player.containsSession()).thenReturn(true);
    when(player.getSession()).thenReturn(Optional.empty());

    Request request = DatagramRequest.newInstance()
        .setEvent(ServerEvent.DATAGRAM_CHANNEL_REQUEST_ACCESS)
        .setSender(datagramChannel)
        .setRemoteAddress(REMOTE_ADDRESS)
        .setMessage(message);

    processor.processRequest(request);
    verify(eventManager).emit(eq(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT),
        eq(player), eq(Session.EMPTY_DATAGRAM_CONVEY_ID),
        eq(AccessDatagramChannelResult.SESSION_NOT_FOUND));
  }

  @Test
  public void shouldCoverProcessingLoopWithRequestAndInterrupt() throws Exception {
    var freshProcessor = ZeroProcessorImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    freshProcessor.setSessionManager(sessionManager);
    freshProcessor.setPlayerManager(playerManager);
    freshProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    freshProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    freshProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    freshProcessor.setThreadPoolSize(1);
    freshProcessor.initialize();
    freshProcessor.activate();

    when(session.isActivated()).thenReturn(false);
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);
    freshProcessor.enqueueRequest(request);

    java.lang.reflect.Method processingMethod =
        AbstractProcessor.class.getDeclaredMethod("processing", int.class);
    processingMethod.setAccessible(true);

    Thread t = new Thread(() -> {
      try {
        processingMethod.invoke(freshProcessor, 0);
      } catch (Exception ignored) {}
    });
    t.start();
    Thread.sleep(200);
    t.interrupt();
    t.join(1000);
  }

  @Test
  public void shouldCoverProcessingThrowableCatch() throws Exception {
    var freshProcessor = ZeroProcessorImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    freshProcessor.setSessionManager(sessionManager);
    freshProcessor.setPlayerManager(playerManager);
    freshProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    freshProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    freshProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    freshProcessor.setThreadPoolSize(1);
    freshProcessor.initialize();
    freshProcessor.activate();

    when(session.isActivated()).thenThrow(new RuntimeException("unexpected error"));
    Request request = SessionRequest.newInstance()
        .setEvent(ServerEvent.SESSION_REQUEST_CONNECTION)
        .setSender(session)
        .setMessage(message);
    freshProcessor.enqueueRequest(request);

    java.lang.reflect.Method processingMethod =
        AbstractProcessor.class.getDeclaredMethod("processing", int.class);
    processingMethod.setAccessible(true);

    Thread t = new Thread(() -> {
      try {
        processingMethod.invoke(freshProcessor, 0);
      } catch (Exception ignored) {}
    });
    t.start();
    Thread.sleep(200);
    t.interrupt();
    t.join(1000);
  }

  @Test
  public void shouldCoverAttemptToShutdownInterruptedExceptionPath() {
    Thread.currentThread().interrupt();
    try {
      processor.shutdown();
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void shouldCoverProcessingWithDeactivatedState() throws Exception {
    var freshProcessor = ZeroProcessorImpl.newInstance(eventManager, serverApi, datagramChannelManager);
    freshProcessor.setSessionManager(sessionManager);
    freshProcessor.setPlayerManager(playerManager);
    freshProcessor.setMaxNumberPlayers(MAX_PLAYERS);
    freshProcessor.setNetworkReaderStatistic(networkReaderStatistic);
    freshProcessor.setNetworkWriterStatistic(networkWriterStatistic);
    freshProcessor.setThreadPoolSize(1);
    freshProcessor.initialize();
    // NOT calling activate() -> activated = false -> covers if(activated) false branch

    java.lang.reflect.Method processingMethod =
        AbstractProcessor.class.getDeclaredMethod("processing", int.class);
    processingMethod.setAccessible(true);

    Thread t = new Thread(() -> {
      try { processingMethod.invoke(freshProcessor, 0); } catch (Exception ignored) {}
    });
    t.start();
    Thread.sleep(50);
    t.interrupt();
    t.join(1000);
  }
}
