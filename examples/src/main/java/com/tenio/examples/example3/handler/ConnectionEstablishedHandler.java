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

package com.tenio.examples.example3.handler;

import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.common.data.ZeroMap;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventConnectionEstablishedResult;
import com.tenio.core.network.entity.session.Session;
import com.tenio.examples.server.SharedEventKey;

@Component
public final class ConnectionEstablishedHandler extends AbstractExtension
    implements EventConnectionEstablishedResult {

  @Override
  public void handle(Session session, ServerMessage message, ConnectionEstablishedResult result) {
    if (result == ConnectionEstablishedResult.SUCCESS) {
      var data = (ZeroMap) message.getData();

      api().login(data.getString(SharedEventKey.KEY_PLAYER_LOGIN), session);
    }
  }
}
