/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

import com.tenio.core.bootstrap.annotation.EventHandler;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.handler.AbstractHandler;
import com.tenio.core.handler.event.EventPlayerJoinedRoomResult;
import com.tenio.examples.example7.constant.Example7Constant;
import com.tenio.examples.server.SharedEventKey;

@EventHandler
public final class PlayerJoinedRoomHandler extends AbstractHandler
    implements EventPlayerJoinedRoomResult<Player, Room> {

  @Override
  public void handle(Player player, Room room, PlayerJoinedRoomResult result) {
    if (result == PlayerJoinedRoomResult.SUCCESS) {
      var players = room.getReadonlyPlayersList();
      var iterator = players.iterator();

      var users = new String[players.size()];
      var positionXs = new int[players.size()];
      var positionYs = new int[players.size()];
      int index = 0;
      while (iterator.hasNext()) {
        var rplayer = iterator.next();

        users[index] = rplayer.getIdentity();
        positionXs[index] = (int) rplayer.getProperty(Example7Constant.PLAYER_POSITION_X);
        positionYs[index] = (int) rplayer.getProperty(Example7Constant.PLAYER_POSITION_Y);

        index++;
      }

      var parcel = msgmap()
          .putString(SharedEventKey.KEY_COMMAND, "i")
          .putStringArray(SharedEventKey.KEY_USER, users)
          .putIntegerArray(SharedEventKey.KEY_DATA_1, positionXs)
          .putIntegerArray(SharedEventKey.KEY_DATA_2, positionYs);

      response().setRecipientPlayers(players).setContent(parcel).write();
    }
  }
}
