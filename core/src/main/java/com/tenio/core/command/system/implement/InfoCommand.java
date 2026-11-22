/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.utility.CommandUtility;
import java.util.List;
import java.util.Locale;

/**
 * Provides brief information about players and rooms on the server.
 *
 * @since 0.4.0
 */
@SystemCommand(label = "info", profile = CoreConstant.MONITOR_PROFILE,
    usage = {"player [all]", "room [all]"},
    description = "Provides brief information about players and rooms on the server")
public final class InfoCommand extends AbstractSystemCommandHandler {

  @Override
  public void execute(List<String> arguments) {
    String action = arguments.size() == 1 ? arguments.getFirst().toLowerCase(Locale.ROOT) : "";
    switch (action) {
      case "player" -> showSnapshot("players", api().getSnapshotPlayersList());
      case "room" -> showSnapshot("rooms", api().getSnapshotRoomsList());
      default -> CommandUtility.INSTANCE.showConsoleMessage("Usage: info player [all] | info room [all]");
    }
  }

  private void showSnapshot(String entity, List<?> snapshot) {
    int shown = Math.min(snapshot.size(), 10);
    String output = "Showing " + shown + " of " + snapshot.size() + " " + entity + " > " + snapshot.subList(0, shown);
    if (shown < snapshot.size()) {
      output += "\nUse info " + (entity.equals("players") ? "player" : "room") + " all for the full table.";
    }
    CommandUtility.INSTANCE.showConsoleMessage(output);
  }
}
