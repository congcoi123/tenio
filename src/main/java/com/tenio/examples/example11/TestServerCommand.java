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

package com.tenio.examples.example11;

import com.tenio.common.data.zero.ZeroMap;
import com.tenio.core.ApplicationLauncher;
import com.tenio.core.bootstrap.annotation.Bootstrap;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.bootstrap.annotation.EventHandler;
import com.tenio.core.bootstrap.annotation.Setting;
import com.tenio.core.command.client.AbstractClientCommandHandler;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.handler.AbstractHandler;
import com.tenio.core.handler.event.EventConnectionEstablishedResult;
import com.tenio.core.handler.event.EventPlayerLogin;
import com.tenio.core.handler.event.EventReceivedMessageFromPlayer;
import com.tenio.core.network.entity.session.Session;
import com.tenio.examples.server.AbstractExampleConfiguration;
import com.tenio.examples.server.SharedEventKey;

/**
 * This class shows how a simple server handle messages that came from a client.
 */
@Bootstrap
public final class TestServerCommand {

  private static final short CREATE_ROOM = 1;
  private static final short CHAT = 2;

  public static void main(String[] params) {
    ApplicationLauncher.run(TestServerCommand.class, params);
  }

  /**
   * Create your own configurations.
   */
  @Setting
  public static class TestConfiguration extends AbstractExampleConfiguration {
  }

  /**
   * Define your handlers.
   */

  @EventHandler
  public static class ConnectionEstablishedHandler extends AbstractHandler
      implements EventConnectionEstablishedResult<ZeroMap> {

    @Override
    public void onConnectionEstablishedResult(Session session, ZeroMap message, ConnectionEstablishedResult result) {
      if (result == ConnectionEstablishedResult.SUCCESS) {
        api().login(message.getString(SharedEventKey.KEY_PLAYER_LOGIN), session);
      }
    }
  }

  @EventHandler
  public static class PlayerLoginHandler extends AbstractHandler
      implements EventPlayerLogin<Player> {

    @Override
    public void onPlayerLogin(Player player) {
      var data = map().putString(SharedEventKey.KEY_PLAYER_LOGIN,
          String.format("Welcome to server: %s", player.getIdentity()));

      response().setContent(data).setRecipientPlayer(player).write();
    }
  }

  @ClientCommand(value = CREATE_ROOM, name = "Create Room", description = "Create a new room", requestsPerSecond = 1, burst = 2)
  public static final class CreateRoom extends AbstractClientCommandHandler<Player, ZeroMap> {

    @Override
    public void execute(Player player, ZeroMap message) {
      var setting = com.tenio.core.entity.setting.InitialRoomSetting.Builder.newInstance()
          .setName(message.getString("name")).setMaxParticipants(2).build();
      api().createRoom(setting, player);
    }
  }

  @ClientCommand(value = CHAT, requestsPerSecond = 2, burst = 4)
  public static final class Chat extends AbstractClientCommandHandler<Player, ZeroMap> {

    @Override
    public void execute(Player player, ZeroMap message) {
      player.getCurrentRoom().ifPresent(room -> response()
          .setContent(map().putString("chat", message.getString("chat")))
          .setRecipientPlayers(room.getSnapshotPlayersList()).write());
    }
  }

  @EventHandler
  public static class ReceivedMessageFromPlayerHandler extends AbstractHandler
      implements EventReceivedMessageFromPlayer<Player, ZeroMap> {

    @Override
    public void onReceivedMessageFromPlayer(Player player, ZeroMap message) {
      if (message.containsKey("op")) {
        clientCommand().invoke(message.getShort("op"), player, message);
        return;
      }
      var data = map().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, String.format("Echo(%s): %s",
              player.getIdentity(),
              message.getString(SharedEventKey.KEY_CLIENT_SERVER_ECHO)));

      response().setContent(data).setRecipientPlayer(player).write();
    }
  }
}
