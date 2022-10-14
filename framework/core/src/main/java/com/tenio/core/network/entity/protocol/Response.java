/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.network.entity.protocol;

import com.tenio.core.entity.Player;
import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.entity.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;

/**
 * The response is created when the server wants to send a message to client side.
 */
public interface Response {

  /**
   * Retrieves an array of binaries data that is carried by the response.
   *
   * @return an array of {@code byte} data that is carried by the response
   */
  byte[] getContent();

  /**
   * Sets content for the response.
   *
   * @param content an array of {@code byte} data that is carried by the response
   * @return the pointer of response
   */
  Response setContent(byte[] content);

  /**
   * Retrieves a collection of recipient players.
   *
   * @return a collection of recipient {@link Player}s
   * @see Collection
   */
  Collection<Player> getRecipientPlayers();

  /**
   * Sets a collection of recipient players.
   *
   * @param players a collection of recipient {@link Player}s
   * @return the pointer of this instance
   * @see Collection
   */
  Response setRecipientPlayers(Collection<Player> players);

  /**
   * Retrieves a collection of non session recipient players.
   *
   * @return a collection of recipient {@link Player}s
   * @see Collection
   */
  Collection<Player> getNonSessionRecipientPlayers();

  /**
   * Retrieves a collection of recipient socket sessions.
   *
   * @return a collection of recipient {@link Session}s
   * @see Collection
   */
  Collection<Session> getRecipientSocketSessions();

  /**
   * Retrieves a collection of recipient socket sessions which receives packets by datagram channel.
   *
   * @return a collection of recipient {@link Session}s which receives packets by datagram channel
   * @see Collection
   */
  Collection<Session> getRecipientDatagramSessions();

  /**
   * Retrieves a collection of recipient WebSocket sessions.
   *
   * @return a collection of recipient WebSocket {@link Session}s
   * @see Collection
   */
  Collection<Session> getRecipientWebSocketSessions();

  /**
   * Sets a recipient player.
   *
   * @param player a recipient {@link Player}
   * @return the pointer of this instance
   */
  Response setRecipientPlayer(Player player);

  /**
   * Sets a collection of recipient sessions.
   *
   * @param sessions a collection of recipient {@link Session}s
   * @return the pointer of this instance
   * @see Collection
   */
  Response setRecipientSessions(Collection<Session> sessions);

  /**
   * Sets a recipient session.
   *
   * @param session a recipient {@link Session}
   * @return the pointer of this instance
   */
  Response setRecipientSession(Session session);

  /**
   * Sets the higher priority for sending packets via the Datagram channel. In case the session in
   * use is WebSocket, then this setting should be ignored.
   *
   * @return the pointer of response
   */
  Response prioritizedUdp();

  /**
   * Allows the sending content to be encrypted.
   *
   * @return the pointer of response
   */
  Response encrypted();

  /**
   * Sets priority for the response.
   *
   * @param priority the {@link ResponsePriority}
   * @return the pointer of response
   * @see PacketQueuePolicy
   */
  Response priority(ResponsePriority priority);

  /**
   * Determines whether the response's content is encrypted.
   *
   * @return {@code true} if the response's content is encrypted, otherwise returns {@code false}
   */
  boolean isEncrypted();

  /**
   * Retrieves the current priority of response.
   *
   * @return the current {@link ResponsePriority} of response
   * @see PacketQueuePolicy
   */
  ResponsePriority getPriority();

  /**
   * Writes down the content data to sessions for sending to client sides.
   */
  void write();

  /**
   * Writes down the content data to sessions for sending to client sides.
   *
   * @param delayInMilliseconds allows delaying in the number of milliseconds
   */
  void writeInDelay(long delayInMilliseconds);
}
