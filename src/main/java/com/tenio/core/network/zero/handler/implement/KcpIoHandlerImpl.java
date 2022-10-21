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

package com.tenio.core.network.zero.handler.implement;

import com.tenio.common.data.DataUtility;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.zero.handler.KcpIoHandler;

/**
 * The implementation of {@link KcpIoHandler}.
 *
 * @since 0.3.0
 */
public final class KcpIoHandlerImpl extends AbstractIoHandler implements KcpIoHandler {

  private KcpIoHandlerImpl(EventManager eventManager) {
    super(eventManager);
  }

  public static KcpIoHandlerImpl newInstance(EventManager eventManager) {
    return new KcpIoHandlerImpl(eventManager);
  }

  @Override
  public void sessionRead(Session session, byte[] binary) {
    var data = DataUtility.binaryToCollection(dataType, binary);
    var message = ServerMessage.newInstance().setData(data);

    eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
  }

  @Override
  public void sessionException(Session session, Exception exception) {
    eventManager.emit(ServerEvent.SESSION_OCCURRED_EXCEPTION, session, exception);
  }

  @Override
  public void channelActiveIn(Session session) {
    debug("KCP CHANNEL", "Activated", session.getUkcp());
  }

  @Override
  public void channelInactiveIn(Session session) {
    debug("KCP CHANNEL", "Inactivated", session.getUkcp());
  }
}
