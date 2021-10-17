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

package com.tenio.core.network.entity.protocol;

import com.tenio.core.entity.Player;
import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;

/**
 * The response was formed when the server wants to send a message to clients.
 */
public interface Response {

  byte[] getContent();

  Response setContent(byte[] content);

  Collection<Player> getPlayers();

  Collection<Player> getNonSessionPlayers();

  Collection<Session> getRecipientSocketSessions();

  Collection<Session> getRecipientDatagramSessions();

  Collection<Session> getRecipientWebSocketSessions();

  Response setRecipients(Collection<Player> players);

  Response setRecipient(Player player);

  Response prioritizedUdp();

  Response encrypted();

  Response priority(ResponsePriority priority);

  boolean isEncrypted();

  ResponsePriority getPriority();

  void write();

  default void writeInDelay(int delayInSeconds) {
    throw new UnsupportedOperationException();
  }
}
