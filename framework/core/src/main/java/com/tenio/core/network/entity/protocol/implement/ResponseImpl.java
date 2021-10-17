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

package com.tenio.core.network.entity.protocol.implement;

import com.tenio.core.entity.Player;
import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.server.ServerImpl;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The implementation for response.
 *
 * @see Response
 */
public final class ResponseImpl implements Response {

  private byte[] content;
  private Collection<Player> players;
  private Collection<Player> nonSessionPlayers;
  private Collection<Session> socketSessions;
  private Collection<Session> datagramSessions;
  private Collection<Session> webSocketSessions;
  private ResponsePriority priority;
  private boolean isPrioritizedUdp;
  private boolean isEncrypted;

  private ResponseImpl() {
    players = null;
    socketSessions = null;
    datagramSessions = null;
    webSocketSessions = null;
    nonSessionPlayers = null;
    priority = ResponsePriority.NORMAL;
    isPrioritizedUdp = false;
    isEncrypted = false;
  }

  public static Response newInstance() {
    return new ResponseImpl();
  }

  @Override
  public byte[] getContent() {
    return content;
  }

  @Override
  public Response setContent(byte[] content) {
    this.content = content;

    return this;
  }

  @Override
  public Collection<Player> getPlayers() {
    return players;
  }

  @Override
  public Collection<Player> getNonSessionPlayers() {
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
  public Response setRecipients(Collection<Player> players) {
    if (this.players == null) {
      this.players = players;
    } else {
      this.players.addAll(players);
    }

    return this;
  }

  @Override
  public Response setRecipient(Player player) {
    if (players == null) {
      players = new ArrayList<Player>();
    }
    players.add(player);

    return this;
  }

  @Override
  public Response prioritizedUdp() {
    isPrioritizedUdp = true;
    return this;
  }

  @Override
  public Response encrypted() {
    isEncrypted = true;
    return this;
  }

  @Override
  public Response priority(ResponsePriority priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public boolean isEncrypted() {
    return isEncrypted;
  }

  @Override
  public ResponsePriority getPriority() {
    return priority;
  }

  @Override
  public void write() {
    if (players == null || players.isEmpty()) {
      return;
    }

    construct();
    ServerImpl.getInstance().write(this);
  }

  private void construct() {
    // if udp is using in use in case of websocket, use websocket instead
    players.stream().forEach(player -> {
      if (player.containsSession()) {
        var session = player.getSession();
        if (session.isTcp()) {
          // when the session contains an UDP connection and the response requires it, add
          // its session to the list
          if (isPrioritizedUdp && session.containsUdp()) {
            if (datagramSessions == null) {
              datagramSessions = new ArrayList<Session>();
            }
            datagramSessions.add(session);
          } else {
            if (socketSessions == null) {
              socketSessions = new ArrayList<Session>();
            }
            socketSessions.add(session);
          }
        } else if (session.isWebSocket()) {
          if (webSocketSessions == null) {
            webSocketSessions = new ArrayList<Session>();
          }
          webSocketSessions.add(session);
        }
      } else {
        if (nonSessionPlayers == null) {
          nonSessionPlayers = new ArrayList<Player>();
        }
        nonSessionPlayers.add(player);
      }
    });
  }

  @Override
  public String toString() {
    return String.format(
        "{ content: bytes[%d], players: %s, socket: %s, datagram: %s, "
            + "websocket: %s, non-session: %s, priority: %s, udp: %b, encrypted: %b}",
        content.length, players != null ? players.toString() : "null",
        socketSessions != null ? socketSessions.toString() : "null",
        datagramSessions != null ? datagramSessions.toString() : "null",
        webSocketSessions != null ? webSocketSessions.toString() : "null",
        nonSessionPlayers != null ? nonSessionPlayers.toString() : "null",
        priority.toString(),
        isPrioritizedUdp, isEncrypted);
  }
}
