/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.examples.example11.command;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.entity.define.mode.ConnectionDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.utility.CommandUtility;
import java.util.List;

@SystemCommand(label = "player", usage = {
    "logout first"
}, description = "Logout the first player from the server")
public class PlayerCommand extends AbstractSystemCommandHandler {

  @Override
  public void execute(List<String> args) {
    var action = args.get(0);
    var param = args.get(1);

    if (action.equals("logout") && param.equals("first")) {
      var players = api().getReadonlyPlayersList();
      if (players.isEmpty()) {
        CommandUtility.INSTANCE.showConsoleMessage("Empty list of players.");
        return;
      }
      var firstPlayer = players.get(0);
      CommandUtility.INSTANCE.showConsoleMessage("Player {" + firstPlayer.getIdentity() + "} is " +
          "going to logout.");
      api().logout(firstPlayer, ConnectionDisconnectMode.DEFAULT, PlayerDisconnectMode.DEFAULT);
    } else {
      CommandUtility.INSTANCE.showConsoleMessage("Invalid action.");
    }
  }
}
