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

package com.tenio.core.handler.event;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.entity.session.Session;

/**
 * When a connection requests to establish itself on the server, and the server returns a result.
 */
@FunctionalInterface
public interface EventConnectionEstablishedResult {

  /**
   * When a connection requests to establish itself on the server, and the server returns a result.
   *
   * @param session when the first request from client side passes the filter process and get on
   *                the server, then an instance of {@link Session} is created.
   * @param message the {@link ServerMessage} sent by client side. The information it carries is
   *                used to decide the following actions. In case the connection does not fulfill
   *                any condition, then the session should be closed manually, otherwise, let the
   *                client login the server and becomes a player.
   * @param result  the returned {@link ConnectionEstablishedResult} from the server. In any case
   *                if the result does not equal to {@link ConnectionEstablishedResult#SUCCESS}
   *                then the established session should close. However, before it is closed, the
   *                session could be used to send responses to the client side.
   * @see ServerEvent#CONNECTION_ESTABLISHED_RESULT
   * @see Response#setRecipientSession(Session)
   * @see Session#close()
   */
  void handle(Session session, ServerMessage message, ConnectionEstablishedResult result);
}
