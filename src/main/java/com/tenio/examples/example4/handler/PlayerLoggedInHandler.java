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

package com.tenio.examples.example4.handler;

import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.PlayerLoggedInResult;
import com.tenio.core.handler.AbstractHandler;
import com.tenio.core.handler.event.EventPlayerLoggedinResult;
import com.tenio.examples.server.SharedEventKey;
import com.tenio.examples.server.UdpEstablishedState;

@Component
public final class PlayerLoggedInHandler extends AbstractHandler
    implements EventPlayerLoggedinResult {

  @Override
  public void handle(Player player, PlayerLoggedInResult result) {
    if (result == PlayerLoggedInResult.SUCCESS) {
      var data =
          map().putByte(SharedEventKey.KEY_ALLOW_TO_ATTACH, UdpEstablishedState.ALLOW_TO_ATTACH)
              .putInteger(SharedEventKey.KEY_ALLOW_TO_ATTACH_PORT,
                  api().getCurrentAvailableUdpPort());

      response().setContent(data.toBinary()).setRecipientPlayer(player).write();
    }
  }
}
