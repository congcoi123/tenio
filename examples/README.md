<p align="center">
    <a href="#">
        <img src="https://github.com/congcoi123/tenio/blob/master/assets/tenio-github-logo.png">
    </a>
</p>
<p align="center">
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/license-MIT-blue.svg">
    </a>
    <a href="#">
        <img src="https://img.shields.io/github/last-commit/congcoi123/tenio-examples">
    </a>
    <a href="https://github.com/congcoi123/tenio-examples/issues">
        <img src="https://img.shields.io/github/issues/congcoi123/tenio-examples">
    </a>
    <a href="CONTRIBUTING.md">
        <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg">
    </a>
    <a href="https://discord.gg/MGCxEwUR">
        <img src="https://img.shields.io/discord/1146091189456613407?logo=discord&logoColor=white">
    </a>
</p>

# TenIO Examples Project [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=TenIO%20is%20a%20java%20NIO%20based%20server%20specifically%20designed%20for%20multiplayer%20games.%0D%0A&url=https://github.com/congcoi123/tenio%0D%0A&hashtags=tenio,java,gameserver,multiplayer,nio,netty,jetty,msgpack,cocos2dx,unity,libgdx,phaserjs%0D%0A&via=congcoi123)
[`TenIO`](https://github.com/congcoi123/tenio) is an open-source project to create multiplayer online games that includes a java NIO (Non-blocking I/O) 
based server specifically designed for multiplayer games, which supports UDP, TCP, Websocket, HTTP transports, and available simple client projects for quick development.

This project contains a collection of examples that show you how to manipulate the framework.

## Dependencies
```txt
- tenio-core 0.5.1
- tenio-engine 0.5.0
```

## Requirements
- Java 17

## License
The [`TenIO`](https://github.com/congcoi123/tenio) project is currently available under the [MIT](LICENSE) License.

## Changelog
Please check out the [changelog](CHANGELOG.md) for more details.

## Contributing
Please check out the [contributing guideline](CONTRIBUTING.md) for more details.

## Installation
```sh
$ git clone https://github.com/congcoi123/tenio-examples.git
```

## Simple Implementation
- Establishes a simple server with only a single Java class

```Java
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
  @Setting
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

  @EventHandler
  public static class ConnectionEstablishedHandler extends AbstractHandler
      implements EventConnectionEstablishedResult {

    @Override
    public void handle(Session session, DataCollection message,
                       ConnectionEstablishedResult result) {
      if (result == ConnectionEstablishedResult.SUCCESS) {
        var request = (ZeroMap) message;

        api().login(request.getString(SharedEventKey.KEY_PLAYER_LOGIN), session);
      }
    }
  }

  @EventHandler
  public static class PlayerLoggedInHandler extends AbstractHandler
      implements EventPlayerLoggedinResult<Player> {

    @Override
    public void handle(Player player, PlayerLoggedInResult result) {
      if (result == PlayerLoggedInResult.SUCCESS) {
        var parcel = map().putString(SharedEventKey.KEY_PLAYER_LOGIN,
            String.format("Welcome to server: %s", player.getName()));

        response().setContent(parcel.toBinary()).setRecipientPlayer(player).write();
      }
    }
  }

  @EventHandler
  public static class ReceivedMessageFromPlayerHandler extends AbstractHandler
      implements EventReceivedMessageFromPlayer<Player> {

    @Override
    public void handle(Player player, DataCollection message) {
      var parcel =
          map().putString(SharedEventKey.KEY_CLIENT_SERVER_ECHO, String.format("Echo(%s): %s",
              player.getName(),
              ((ZeroMap) message).getString(SharedEventKey.KEY_CLIENT_SERVER_ECHO)));

      response().setContent(parcel.toBinary()).setRecipientPlayer(player).write();
    }
  }
}
```

- Supports self-defined commands to interact with the server conveniently
1) Usage
  
```text
2022-11-20 05:20:38,256 [main] INFO  com.tenio.core.server.ServerImpl - [SERVER][Example] Started
$ help
help - Shows all supporting commands
	[<command>,<command>,<command>]
info - Provides brief information about players and rooms on the server
	player
	room
player - Logout the first player from the server
	logout first
server - Allows stopping or restarting the server
	stop
	restart
unban - Allows removing banned Ip addresses from the ban list
	[<address>,<command>,<command>]
$ info player
> There are 1 players > The first 10 entities > [Player{name='IkjvI', properties={}, session=Session{id=0, name='IkjvI', transportType=TCP, createdTime=1668918078524, lastReadTime=1668918078524, lastWriteTime=1668918078524, lastActivityTime=1668918078524, readBytes=75, writtenBytes=120, droppedPackets=0, inactivatedTime=0, datagramRemoteSocketAddress=null, clientAddress='127.0.0.1', clientPort=60659, serverPort=8032, serverAddress='127.0.0.1', maxIdleTimeInSecond=0, activated=true, connected=true, hasUdp=false, enabledKcp=false, hasKcp=false}, currentRoom=null, state=null, roleInRoom=SPECTATOR, lastLoginTime=1668918078589, lastJoinedRoomTime=1668918078588, playerSlotInCurrentRoom=-1, loggedIn=true, activated=true, hasSession=true}]
$ 
```

2) Make sure to set the command usage flag in _**setting.json**_ file to be **enabled**

```JSON
{
	"command": {
		"enabled": true
	},
	"plugin": {
		"enabled": false,
		"path": "/plugin"
	}
}
``` 

3) Simple implementation
```Java
@Command(label = "player", usage = {
    "logout first"
}, description = "Logout the first player from the server")
public class PlayerCommand extends AbstractCommandHandler {

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
      CommandUtility.INSTANCE.showConsoleMessage("Player {" + firstPlayer.getName() + "} is " +
          "going to logout.");
      api().logout(firstPlayer);
    } else {
      CommandUtility.INSTANCE.showConsoleMessage("Invalid action.");
    }
  }
}
``` 

## Examples
Please start the server before its corresponding client in each example package.
```code
$ java <server_main_class> <server_configuration_file.xml>
```
For instance:
```code
$ java TestServerLogin configuration.example1.xml
```

```txt
|-- example
    |-- example0
    |   |-- TestSimpleClient
    |   |-- TestSimpleServer
    |-- example1
    |   |-- TestClientLogin
    |   |-- TestServerLogin
    |-- example2
    |   |-- (*)TestFsmMechanism
    |-- example3
    |   |-- TestClientAccessDatagramChannel
    |   |-- TestServerAccessDatagramChannel
    |-- example4
    |   |-- TestClientMovement
    |   |-- TestServerMovement
    |   |-- (*)TestMovementMechanism
    |-- example5
    |   |-- (*)TestEcsMechanism
    |-- example6
    |   |-- TestClientEchoStress
    |   |-- TestServerEchoStress
    |-- example7
    |   |-- TestServerWebsocket
    |-- example8
    |   |-- TestClientRestful
    |   |-- TestServerRestful
    |-- example9
    |   |-- TestClientKcpEcho
    |   |-- TestServerKcpEcho
    |-- example10
    |   |-- TestClientMsgPackEcho
    |   |-- TestServerMsgPackEcho
    |-- example11
    |   |-- TestClientCommand
    |   |-- TestServerCommand
```

> Happy coding !
