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

package com.tenio.examples.example7.handler;

import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.common.data.ZeroObject;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventReceivedMessageFromPlayer;
import com.tenio.examples.example7.constant.Example7Constant;
import com.tenio.examples.server.SharedEventKey;

@Component
public final class ReceivedMessageFromPlayerHandler extends AbstractExtension
    implements EventReceivedMessageFromPlayer {

  @Override
  public void handle(Player player, ServerMessage message) {
    var position =
        ((ZeroObject) message.getData()).getIntegerArray(SharedEventKey.KEY_PLAYER_POSITION)
            .toArray();

    player.setProperty(Example7Constant.PLAYER_POSITION_X, position[0]);
    player.setProperty(Example7Constant.PLAYER_POSITION_Y, position[1]);

    var players = player.getCurrentRoom().getAllPlayersList();

    var pack = array();
    var parray = array();
    parray.addString(player.getName());
    parray.addInteger((int) position[0]);
    parray.addInteger((int) position[1]);

    pack.addZeroArray(parray);

    var request = object().putZeroArray(SharedEventKey.KEY_PLAYER_POSITION, pack);

    response().setRecipients(players).setContent(request.toBinary()).write();
  }
}
