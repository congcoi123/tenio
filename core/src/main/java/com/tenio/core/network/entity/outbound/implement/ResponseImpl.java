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

package com.tenio.core.network.entity.outbound.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.data.DataType;
import com.tenio.common.logger.SystemLogger;
import com.tenio.core.entity.Player;
import com.tenio.core.network.define.ResponseGuarantee;
import com.tenio.core.network.entity.outbound.Response;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.server.ServerImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The implementation for response.
 *
 * @see Response
 */
public final class ResponseImpl extends SystemLogger implements Response {

  private DataCollection content;
  private Collection<Player> players;
  private Collection<Player> nonSessionPlayers;
  private Collection<Session> socketSessions;
  private Collection<Session> datagramSessions;
  private Collection<Session> webSocketSessions;
  /**
   * The session that owned each player when the response was addressed. Keeping this association
   * prevents an already-created response from following a player to a replacement connection.
   */
  private Map<Player, Session> addressedPlayerSessions;
  private ResponseGuarantee guarantee;
  private boolean prioritizedUdp;
  private boolean onlyUdp;
  private boolean encrypted;

  private ResponseImpl() {
    players = null;
    socketSessions = null;
    datagramSessions = null;
    webSocketSessions = null;
    nonSessionPlayers = null;
    addressedPlayerSessions = new IdentityHashMap<>();
    guarantee = ResponseGuarantee.NORMAL;
    prioritizedUdp = false;
    onlyUdp = false;
    encrypted = false;
  }

  /**
   * Creates a new response instance.
   *
   * @return a new instance of {@link Response}
   */
  public static Response newInstance() {
    return new ResponseImpl();
  }

  @Override
  public DataCollection getContent() {
    return content;
  }

  @Override
  public Response setContent(DataCollection content) {
    this.content = content;
    return this;
  }

  @Override
  public DataType getDataType() {
    return content.getType();
  }

  @Override
  public Collection<Player> getRecipientPlayers() {
    return players;
  }

  @Override
  public Collection<Player> getNonSessionRecipientPlayers() {
    return nonSessionPlayers;
  }

  @Override
  public Collection<Session> getRecipientSocketSessions() {
    return socketSessions;
  }

  @Override
  public Collection<Session> getRecipientDatagramSessions() {
    return datagramSessions;
  }

  @Override
  public Collection<Session> getRecipientWebSocketSessions() {
    return webSocketSessions;
  }

  @Override
  public Response setRecipientPlayers(Collection<Player> players) {
    players.forEach(this::setRecipientPlayer);
    return this;
  }

  @Override
  public Response setRecipientPlayer(Player player) {
    if (players == null) {
      players = new ArrayList<>();
    }
    players.add(player);
    // Address the response to the connection that exists now. Resolving this only in write()
    // lets a delayed response for an old connection be delivered to a newly logged-in device.
    player.getSession().ifPresent(session -> addressedPlayerSessions.put(player, session));
    return this;
  }

  @Override
  public Response setRecipientSessions(Collection<Session> sessions) {
    sessions.forEach(this::checksAndAddsSession);
    return this;
  }

  @Override
  public Response setRecipientSession(Session session) {
    checksAndAddsSession(session);
    return this;
  }

  @Override
  public Response prioritizedUdp() {
    prioritizedUdp = true;
    return this;
  }

  @Override
  public Response onlyUdp() {
    onlyUdp = true;
    return this;
  }

  @Override
  public Response encrypted() {
    encrypted = true;
    return this;
  }

  @Override
  public Response guarantee(ResponseGuarantee guarantee) {
    this.guarantee = guarantee;
    return this;
  }

  @Override
  public boolean needsEncrypted() {
    return encrypted;
  }

  @Override
  public ResponseGuarantee getGuarantee() {
    return guarantee;
  }

  @Override
  public void write() {
    constructRecipientPlayers();
    ServerImpl.getInstance().write(this, false);
  }

  @Override
  public void writeInDelay(long delayInMilliseconds) {
    try {
      TimeUnit.MILLISECONDS.sleep(delayInMilliseconds);
      write();
    } catch (InterruptedException exception) {
      error(exception);
    }
  }

  @Override
  public void writeThenClose() {
    constructRecipientPlayers();
    markRecipientsForCloseAfterPendingWrite(socketSessions);
    markRecipientsForCloseAfterPendingWrite(webSocketSessions);
    ServerImpl.getInstance().write(this, true);
  }

  private void markRecipientsForCloseAfterPendingWrite(Collection<Session> sessions) {
    if (sessions != null) {
      sessions.forEach(Session::markCloseAfterPendingWrite);
    }
  }

  private void constructRecipientPlayers() {
    if (players == null || players.isEmpty()) {
      return;
    }

    // If the player had a session when it was addressed, retain that exact session. A player may
    // have moved to a replacement connection before this response is finally written.
    //
    // If it had no session then, retain the existing non-session behavior and resolve it here.
    // This supports responses that are intentionally addressed while a player is offline.
    //
    // If UDP is set to the highest priority in use but the session type is WebSocket, then use the
    // WebSocket channel instead. When UDP is required exclusively, skip recipients without it.
    Iterator<Player> iterator = players.iterator();
    while (iterator.hasNext()) {
      Player player = iterator.next();
      Session addressedSession = addressedPlayerSessions.get(player);
      if (addressedSession != null) {
        if (!checksAndAddsSession(addressedSession)) {
          iterator.remove();
        }
      } else if (player.containsSession()) {
        if (!player.getSession().filter(this::checksAndAddsSession).isPresent()) {
          iterator.remove();
        }
      } else if (onlyUdp) {
        iterator.remove();
      } else {
        if (nonSessionPlayers == null) {
          nonSessionPlayers = new ArrayList<>();
        }
        nonSessionPlayers.add(player);
      }
    }
  }

  private boolean checksAndAddsSession(Session session) {
    if (session.isTcp()) {
      // When the session contains UDP and the response requires it, add it to the UDP list.
      if ((prioritizedUdp || onlyUdp) && session.containsUdp()) {
        if (datagramSessions == null) {
          datagramSessions = new ArrayList<>();
        }
        datagramSessions.add(session);
        return true;
      }
      if (onlyUdp) {
        return false;
      }
      if (socketSessions == null) {
        socketSessions = new ArrayList<>();
      }
      socketSessions.add(session);
      return true;
    } else if (session.isWebSocket()) {
      if (onlyUdp) {
        return false;
      }
      if (webSocketSessions == null) {
        webSocketSessions = new ArrayList<>();
      }
      webSocketSessions.add(session);
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "Response{" +
        "contentType=" + (content == null ? null : content.getClass().getName()) +
        ", playersCount=" + (players == null ? 0 : players.size()) +
        ", nonSessionPlayersCount=" + (nonSessionPlayers == null ? 0 : nonSessionPlayers.size()) +
        ", socketSessionsCount=" + (socketSessions == null ? 0 : socketSessions.size()) +
        ", datagramSessionsCount=" + (datagramSessions == null ? 0 : datagramSessions.size()) +
        ", webSocketSessionsCount=" + (webSocketSessions == null ? 0 : webSocketSessions.size()) +
        ", guarantee=" + guarantee +
        ", prioritizedUdp=" + prioritizedUdp +
        ", onlyUdp=" + onlyUdp +
        ", encrypted=" + encrypted +
        '}';
  }
}
