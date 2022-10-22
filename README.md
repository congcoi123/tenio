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
    <a href="https://gitter.im/ten-io/community?source=orgpage">
        <img src="https://badges.gitter.im/Join%20Chat.svg">
    </a>
</p>

# TenIO Examples Project [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=TenIO%20is%20a%20java%20NIO%20based%20server%20specifically%20designed%20for%20multiplayer%20games.%0D%0A&url=https://github.com/congcoi123/tenio%0D%0A&hashtags=tenio,java,gameserver,multiplayer,nio,netty,jetty,msgpack,cocos2dx,unity,libgdx,phaserjs%0D%0A&via=congcoi123)
[`TenIO`](https://github.com/congcoi123/tenio) is an open-source project to create multiplayer online games that includes a java NIO (Non-blocking I/O) 
based server specifically designed for multiplayer games, which supports UDP, TCP, Websocket, HTTP transports, and available simple client projects for quick development.

This project contains a collection of examples that show you how to manipulate the framework.

## Dependencies
```txt
- tenio-core 0.3.1
- tenio-engine 0.2.0
```

## Requirements
- Java 11

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
Establishes a simple server with only a single Java class.
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
    |   |-- TestClientAttach
    |   |-- TestServerAttach
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
```

> Happy coding !
