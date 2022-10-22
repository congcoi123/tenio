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

package com.tenio.examples.example0;

import com.tenio.common.bootstrap.annotation.Bootstrap;
import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.zero.ZeroMap;
import com.tenio.core.ApplicationLauncher;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.data.ServerMessage;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.entity.define.result.PlayerLoggedInResult;
import com.tenio.core.handler.AbstractHandler;
import com.tenio.core.handler.event.EventConnectionEstablishedResult;
import com.tenio.core.handler.event.EventPlayerLoggedinResult;
import com.tenio.core.handler.event.EventReceivedMessageFromPlayer;
import com.tenio.core.network.entity.session.Session;
import com.tenio.examples.server.ExampleConfigurationType;
import com.tenio.examples.server.SharedEventKey;
import java.util.Map;

/**
 * This class shows how a simple server handle messages that came from a client.
 */
@Bootstrap
public final class TestSimpleServer {

  public static void main(String[] params) {
    ApplicationLauncher.run(TestSimpleServer.class, params);
  }

  /**
   * Create your own configurations.
   */
  @Component
  public static class TestConfiguration extends CoreConfiguration implements Configuration {

    @Override
    protected void extend(Map<String, String> extProperties) {
      for (Map.Entry<String, String> entry : extProperties.entrySet()) {
        var paramName = entry.getKey();
        push(ExampleConfigurationType.getByValue(paramName), String.valueOf(entry.getValue()));
      }
    }
  }

  /**
   * Define your handlers.
   */

  @Component
  public static class ConnectionEstablishedHandler extends AbstractHandler
      implements EventConnectionEstablishedResult {

    @Override
    public void handle(Session session, ServerMessage message, ConnectionEstablishedResult result) {
      if (result == ConnectionEstablishedResult.SUCCESS) {
        var data = (ZeroMap) message.getData();

        api().login(data.getString(SharedEventKey.KEY_PLAYER_LOGIN), session);
      }
    }
  }

  @Component
  public static class PlayerLoggedInHandler extends AbstractHandler implements EventPlayerLoggedinResult {

    @Override
    public void handle(Player player, PlayerLoggedInResult result) {
      if (result == PlayerLoggedInResult.SUCCESS) {
        var data = map().putString(SharedEventKey.KEY_PLAYER_LOGIN,
            String.format("Welcome to server: %s", player.getName()));

        response().setContent(data.toBinary()).setRecipientPlayer(player).write();
      }
    }
  }

  @Component
  public static class ReceivedMessageFromPlayerHandler extends AbstractHandler
      implements EventReceivedMessageFromPlayer {

    @Override
    public void handle(Player player, ServerMessage message) {
      var data =
          map().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, String.format("Echo(%s): %s",
              player.getName(),
              ((ZeroMap) message.getData()).getString(SharedEventKey.KEY_CLIENT_SERVER_ECHO)));

      response().setContent(data.toBinary()).setRecipientPlayer(player).write();
    }
  }
}
