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

package com.tenio.core.command.system.implement;

import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.utility.CommandUtility;
import java.util.List;

/**
 * Provides brief information about players and rooms on the server.
 *
 * @since 0.4.0
 */
@SystemCommand(label = "info", usage = {
    "player",
    "room"
}, description = "Provides brief information about players and rooms on the server")
public final class InfoCommand extends AbstractSystemCommandHandler {

  @Override
  public void execute(List<String> args) {
    var action = args.get(0);

    switch (action) {
      case "player":
        CommandUtility.INSTANCE.showConsoleMessage(
            "There are " + api().getPlayerCount() + " players > The first 10 entities > " +
                api().getReadonlyPlayersList().subList(0, Math.min(api().getPlayerCount(), 10)));
        break;

      case "room":
        var rooms = api().getReadonlyRoomsList();
        CommandUtility.INSTANCE.showConsoleMessage(
            "There are " + rooms.size() + " rooms > The first 10 entities > " +
                rooms.subList(0, Math.min(rooms.size(), 10)));
        break;

      default:
        CommandUtility.INSTANCE.showConsoleMessage("The feature is not available at the moment.");
        break;
    }
  }
}
