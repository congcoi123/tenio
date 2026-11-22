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

package com.tenio.core.bootstrap.profiles.commands;

import com.tenio.common.data.DataCollection;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.client.AbstractClientCommandHandler;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.entity.Player;
import java.util.List;

/** Disjoint profiles intentionally reuse the same external command registrations. */
public final class CommandFixtures {

  public static int worldConstructions;
  public static int gameConstructions;

  @Component
  public static class Dependency {}

  @ClientCommand(value = 42, profile = "world")
  public static class WorldClient extends AbstractClientCommandHandler<Player, DataCollection> {
    @Autowired
    public Dependency dependency;

    public WorldClient() {
      worldConstructions++;
    }

    @Override
    public void execute(Player player, DataCollection message) {}
  }

  @ClientCommand(value = 42, profile = "game")
  public static class GameClient extends AbstractClientCommandHandler<Player, DataCollection> {
    @Autowired
    public Dependency dependency;

    public GameClient() {
      gameConstructions++;
    }

    @Override
    public void execute(Player player, DataCollection message) {}
  }

  @SystemCommand(label = "profile-status", profile = "world")
  public static class WorldSystem extends AbstractSystemCommandHandler {
    @Autowired
    public Dependency dependency;

    public WorldSystem() {
      worldConstructions++;
    }

    @Override
    public void execute(List<String> arguments) {}
  }

  @SystemCommand(label = "profile-status", profile = "game")
  public static class GameSystem extends AbstractSystemCommandHandler {
    @Autowired
    public Dependency dependency;

    public GameSystem() {
      gameConstructions++;
    }

    @Override
    public void execute(List<String> arguments) {}
  }

  private CommandFixtures() {}
}
