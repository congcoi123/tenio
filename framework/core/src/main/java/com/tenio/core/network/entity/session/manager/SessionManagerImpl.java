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

package com.tenio.core.network.entity.session.manager;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.outbound.packet.implement.OutboundQueueImpl;
import com.tenio.core.network.entity.outbound.packet.policy.OutboundQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.implement.SessionImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import io.netty.channel.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The implementation for session manager.
 *
 * @see SessionManager
 */
public final class SessionManagerImpl extends AbstractManager implements SessionManager {

  private final Map<Long, Session> sessionByIds;
  private final Map<SocketChannel, Session> sessionBySockets;
  private final Map<Channel, Session> sessionByWebSockets;
  private final Map<Integer, Session> sessionByDatagrams;
  private volatile List<Session> readonlySessionsList;
  private volatile int sessionCount;
  private OutboundQueuePolicy outboundQueuePolicy;
  private ConnectionFilter connectionFilter;
  private int inboundQueueSize;
  private int outboundQueueSize;
  private int slowConsumingInboundQueueWarningThreshold;
  private int slowConsumingOutboundQueueWarningThreshold;
  private int maxIdleTimeInSeconds;

  private SessionManagerImpl(EventManager eventManager) {
    super(eventManager);
    sessionByIds = new HashMap<>();
    sessionBySockets = new HashMap<>();
    sessionByWebSockets = new HashMap<>();
    sessionByDatagrams = new HashMap<>();
    readonlySessionsList = new ArrayList<>();
    inboundQueueSize = DEFAULT_MAX_INBOUND_QUEUE_SIZE;
    outboundQueueSize = DEFAULT_MAX_OUTBOUND_QUEUE_SIZE;
    slowConsumingInboundQueueWarningThreshold = DEFAULT_SLOW_CONSUMING_INBOUND_QUEUE_WARNING_THRESHOLD;
    slowConsumingOutboundQueueWarningThreshold = DEFAULT_SLOW_CONSUMING_OUTBOUND_QUEUE_WARNING_THRESHOLD;
  }

  /**
   * Creates a new instance of the session manager.
   *
   * @param eventManager the instance of {@link EventManager}
   * @return a new instance of {@link SessionManager}
   */
  public static SessionManager newInstance(EventManager eventManager) {
    return new SessionManagerImpl(eventManager);
  }

  @Override
  public void computeSessions(Consumer<Iterator<Session>> onComputed) {
    synchronized (this) {
      onComputed.accept(sessionByIds.values().iterator());
    }
  }

  @Override
  public Session createSocketSession(SocketChannel socketChannel, SelectionKey selectionKey) {
    Session session = SessionImpl.newInstance();
    session.configureSocketChannel(socketChannel, selectionKey);
    configureSession(session);
    synchronized (this) {
      sessionByIds.put(session.getId(), session);
      sessionBySockets.put(session.fetchSocketChannel(), session);
      readonlySessionsList = sessionByIds.values().stream().toList();
      sessionCount = readonlySessionsList.size();
      session.activate();
    }
    return session;
  }

  @Override
  public void removeSessionBySocket(SocketChannel socketChannel) {
    Session session = getSessionBySocket(socketChannel);
    removeSession(session);
  }

  @Override
  public Session getSessionBySocket(SocketChannel socketChannel) {
    synchronized (sessionBySockets) {
      return sessionBySockets.get(socketChannel);
    }
  }

  @Override
  public void addDatagramForSession(DatagramChannel datagramChannel, int udpConvey, Session session) {
    if (!session.isTcp()) {
      throw new IllegalArgumentException(
          String.format("Unable to add UDP channel into a non-TCP session: %s", session));
    }
    synchronized (sessionByDatagrams) {
      session.configureDatagramChannel(datagramChannel, udpConvey);
      sessionByDatagrams.put(udpConvey, session);
    }
  }

  @Override
  public Session getSessionByDatagram(int udpConvey) {
    synchronized (sessionByDatagrams) {
      return sessionByDatagrams.get(udpConvey);
    }
  }

  @Override
  public void configureConnectionFilter(ConnectionFilter connectionFilter) {
    this.connectionFilter = connectionFilter;
  }

  @Override
  public Session createWebSocketSession(Channel webSocketChannel) {
    Session session = SessionImpl.newInstance();
    session.configureWebSocketChannel(webSocketChannel);
    configureSession(session);
    synchronized (this) {
      sessionByIds.put(session.getId(), session);
      sessionByWebSockets.put(webSocketChannel, session);
      readonlySessionsList = sessionByIds.values().stream().toList();
      sessionCount = readonlySessionsList.size();
      session.activate();
    }
    return session;
  }

  @Override
  public void removeSessionByWebSocket(Channel webSocketChannel) {
    Session session = getSessionByWebSocket(webSocketChannel);
    removeSession(session);
  }

  @Override
  public Session getSessionByWebSocket(Channel webSocketChannel) {
    synchronized (sessionByWebSockets) {
      return sessionByWebSockets.get(webSocketChannel);
    }
  }

  @Override
  public void configureOutboundQueuePolicy(OutboundQueuePolicy outboundQueuePolicy) {
    this.outboundQueuePolicy = outboundQueuePolicy;
  }

  @Override
  public void configureInboundQueueSize(int queueSize) {
    inboundQueueSize = queueSize;
  }

  @Override
  public void configureOutboundQueueSize(int queueSize) {
    outboundQueueSize = queueSize;
  }

  @Override
  public void configureSlowConsumingInboundQueueWarningThreshold(int threshold) {
    slowConsumingInboundQueueWarningThreshold = threshold;
  }

  @Override
  public void configureSlowConsumingOutboundQueueWarningThreshold(int threshold) {
    slowConsumingOutboundQueueWarningThreshold = threshold;
  }

  @Override
  public void removeSession(Session session) {
    synchronized (this) {
      switch (session.getTransportType()) {
        case TCP -> {
          if (session.containsUdp()) {
            sessionByDatagrams.remove(session.getUdpConveyId());
            session.configureDatagramChannel(null, Session.EMPTY_DATAGRAM_CONVEY_ID);
          }
          sessionBySockets.remove(session.fetchSocketChannel());
        }
        case WEB_SOCKET -> sessionByWebSockets.remove(session.fetchWebSocketChannel());
        default -> {
        }
      }
      sessionByIds.remove(session.getId());
      readonlySessionsList = sessionByIds.values().stream().toList();
      sessionCount = readonlySessionsList.size();
    }
  }

  @Override
  public List<Session> getReadonlySessionsList() {
    return readonlySessionsList;
  }

  @Override
  public int getSessionCount() {
    return sessionCount;
  }

  @Override
  public void configureMaxIdleTimeInSeconds(int seconds) {
    maxIdleTimeInSeconds = seconds;
  }

  @Override
  public void emitEvent(ServerEvent event, Object... params) {
    eventManager.emit(event, params);
  }

  private OutboundQueue configureNewOutboundQueue() {
    OutboundQueue outboundQueue = OutboundQueueImpl.newInstance();
    outboundQueue.configureMaxSize(outboundQueueSize);
    outboundQueue.configureOutboundQueuePolicy(outboundQueuePolicy);
    return outboundQueue;
  }

  private void configureSession(Session session) {
    session.configureSessionManager(this);
    session.configureMaxInboundQueueSize(inboundQueueSize);
    session.configureOutboundQueue(configureNewOutboundQueue());
    session.configureSlowConsumingInboundQueueWarningThreshold(slowConsumingInboundQueueWarningThreshold);
    session.configureSlowConsumingOutboundQueueWarningThreshold(slowConsumingOutboundQueueWarningThreshold);
    session.configureConnectionFilter(connectionFilter);
    session.configureMaxIdleTimeInSeconds(maxIdleTimeInSeconds);
  }
}
