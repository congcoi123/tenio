/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.entity.session.implement;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.packet.implement.PacketQueueImpl;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.SessionManager;
import io.netty.channel.Channel;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;

/**
 * The implementation for session manager.
 *
 * @see SessionManager
 */
public final class SessionManagerImpl extends AbstractManager implements SessionManager {

  private static final int DEFAULT_PACKET_QUEUE_SIZE = 100;

  @GuardedBy("this")
  private final Map<Long, Session> sessionByIds;
  @GuardedBy("this")
  private final Map<SocketChannel, Session> sessionBySockets;
  @GuardedBy("this")
  private final Map<Channel, Session> sessionByWebSockets;
  @GuardedBy("this")
  private final Map<SocketAddress, Session> sessionByDatagrams;

  private PacketQueuePolicy packetQueuePolicy;
  private int packetQueueSize;

  private volatile int sessionCount;

  private SessionManagerImpl(EventManager eventManager) {
    super(eventManager);

    sessionByIds = new HashMap<Long, Session>();
    sessionBySockets = new HashMap<SocketChannel, Session>();
    sessionByWebSockets = new HashMap<Channel, Session>();
    sessionByDatagrams = new HashMap<SocketAddress, Session>();

    sessionCount = 0;
    packetQueueSize = DEFAULT_PACKET_QUEUE_SIZE;
    packetQueuePolicy = null;
  }

  public static SessionManager newInstance(EventManager eventManager) {
    return new SessionManagerImpl(eventManager);
  }

  @Override
  public Session createSocketSession(SocketChannel socketChannel, SelectionKey selectionKey) {
    var session = SessionImpl.newInstance();
    session.setSocketChannel(socketChannel);
    session.setSelectionKey(selectionKey);
    session.setSessionManager(this);
    session.setPacketQueue(createNewPacketQueue());
    synchronized (this) {
      sessionByIds.put(session.getId(), session);
      sessionBySockets.put(session.getSocketChannel(), session);
      sessionCount = sessionByIds.size();
    }
    return session;
  }

  @Override
  public void removeSessionBySocket(SocketChannel socketChannel) {
    var session = getSessionBySocket(socketChannel);
    removeSession(session);
  }

  @Override
  public Session getSessionBySocket(SocketChannel socketChannel) {
    synchronized (sessionBySockets) {
      return sessionBySockets.get(socketChannel);
    }
  }

  @Override
  public void addDatagramForSession(DatagramChannel datagramChannel, SocketAddress remoteAddress,
                                    Session session) {
    if (!session.isTcp()) {
      throw new IllegalArgumentException(
          String.format("Unable to add datagram channel for the non-TCP session: %s",
              session));
    }
    synchronized (sessionByDatagrams) {
      session.setDatagramChannel(datagramChannel, remoteAddress);
      sessionByDatagrams.put(session.getDatagramRemoteSocketAddress(), session);
    }
  }

  @Override
  public Session getSessionByDatagram(SocketAddress remoteAddress) {
    synchronized (sessionByDatagrams) {
      return sessionByDatagrams.get(remoteAddress);
    }
  }

  @Override
  public Session createWebSocketSession(Channel webSocketChannel) {
    var session = SessionImpl.newInstance();
    session.setWebSocketChannel(webSocketChannel);
    session.setSessionManager(this);
    session.setPacketQueue(createNewPacketQueue());
    synchronized (this) {
      sessionByIds.put(session.getId(), session);
      sessionByWebSockets.put(webSocketChannel, session);
      sessionCount = sessionByIds.size();
    }
    return session;
  }

  @Override
  public void removeSessionByWebSocket(Channel webSocketChannel) {
    var session = getSessionByWebSocket(webSocketChannel);
    removeSession(session);
  }

  @Override
  public Session getSessionByWebSocket(Channel webSocketChannel) {
    synchronized (sessionByWebSockets) {
      return sessionByWebSockets.get(webSocketChannel);
    }
  }

  private PacketQueue createNewPacketQueue() {
    var packetQueue = PacketQueueImpl.newInstance();
    packetQueue.setMaxSize(packetQueueSize);
    packetQueue.setPacketQueuePolicy(packetQueuePolicy);

    return packetQueue;
  }

  @Override
  public void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException {
    packetQueuePolicy = clazz.getDeclaredConstructor().newInstance();
  }

  @Override
  public void setPacketQueueSize(int queueSize) {
    packetQueueSize = queueSize;
  }

  @Override
  public void removeSession(Session session) {
    synchronized (this) {
      switch (session.getTransportType()) {
        case TCP:
          if (session.containsUdp()) {
            sessionByDatagrams.remove(session.getDatagramRemoteSocketAddress());
            session.setDatagramChannel(null, null);
          }
          sessionBySockets.remove(session.getSocketChannel());
          break;

        case WEB_SOCKET:
          sessionByWebSockets.remove(session.getWebSocketChannel());
          break;

        default:
          break;
      }
      sessionByIds.remove(session.getId());
      sessionCount = sessionByIds.size();
    }
  }

  @Override
  public void emitEvent(ServerEvent event, Object... params) {
    eventManager.emit(event, params);
  }

  @Override
  public int getSessionCount() {
    return sessionCount;
  }
}
