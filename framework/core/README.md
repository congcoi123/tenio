<p align="center">
    <a href="#">
        <img src="https://github.com/congcoi123/tenio/blob/master/assets/tenio-github-logo.png">
    </a>
</p>
<p align="center">
    <a href="https://mvnrepository.com/artifact/io.github.congcoi123/tenio-core">
        <img src="https://img.shields.io/maven-central/v/io.github.congcoi123/tenio-core.svg">
    </a>
    <a href="https://javadoc.io/doc/io.github.congcoi123/tenio-core">
        <img src="https://javadoc.io/badge2/io.github.congcoi123/tenio-core/javadoc.svg">
    </a>
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/license-MIT-blue.svg">
    </a>
    <a href="https://github.com/congcoi123/tenio-core/actions">
        <img src="https://github.com/congcoi123/tenio-core/actions/workflows/maven.yml/badge.svg">
    </a>    
    <a href="https://coveralls.io/github/congcoi123/tenio-core">
        <img src="https://coveralls.io/repos/github/congcoi123/tenio-core/badge.svg?branch=master">
    </a>
    <a href="#">
        <img src="https://img.shields.io/github/last-commit/congcoi123/tenio-core">
    </a>
    <a href="https://github.com/congcoi123/tenio-core/issues">
        <img src="https://img.shields.io/github/issues/congcoi123/tenio-core">
    </a>
    <a href="CONTRIBUTING.md">
        <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg">
    </a>
    <a href="https://discord.gg/ybkNU87Psy">
        <img src="https://img.shields.io/discord/1146091189456613407?logo=discord&logoColor=white">
    </a>
</p>

# TenIO Core Module [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=TenIO%20is%20a%20java%20NIO%20based%20server%20specifically%20designed%20for%20multiplayer%20games.%0D%0A&url=https://github.com/congcoi123/tenio%0D%0A&hashtags=tenio,java,gameserver,multiplayer,nio,netty,jetty,msgpack,cocos2dx,unity,libgdx,phaserjs%0D%0A&via=congcoi123)
[`TenIO`](https://github.com/congcoi123/tenio) is an open-source project to create multiplayer online games that includes a java NIO (Non-blocking I/O) 
based server specifically designed for multiplayer games, which supports UDP, TCP, Websocket, HTTP transports, and available simple client projects for quick development.

This module provides all the main features for the framework to operate. It contains an NIO mechanism, server configuration solution, events handling, and other necessary functions that you may find helpful.

## All supported events
```txt
EventAccessDatagramChannelRequestValidation
EventAccessDatagramChannelRequestValidationResult
EventAccessKcpChannelRequestValidation
EventAccessKcpChannelRequestValidationResult
EventConnectionEstablishedResult
EventDisconnectConnection
EventDisconnectPlayer
EventFetchedBandwidthInfo
EventFetchedCcuInfo
EventPlayerAfterLeftRoom
EventPlayerBeforeLeaveRoom
EventPlayerJoinedRoomResult
EventPlayerLoggedinResult
EventPlayerReconnectedResult
EventPlayerReconnectRequestHandle
EventReceivedMessageFromPlayer
EventRoomCreatedResult
EventRoomWillBeRemoved
EventSendMessageToPlayer
EventServerException
EventServerInitialization
EventServerTeardown
EventSocketConnectionRefused
EventSwitchParticipantToSpectatorResult
EventSwitchSpectatorToParticipantResult
EventSystemMonitoring
EventWebSocketConnectionRefused
EventWriteMessageToConnection
```

## Requirements
- Java 17

## Dependencies
- HTTP [Jetty](https://eclipse.dev/jetty)
- Websocket [Netty](https://netty.io)
- KCP [KCP Java](https://github.com/l42111996/java-Kcp)

## License
The [`TenIO`](https://github.com/congcoi123/tenio) project is currently available under the [MIT](LICENSE) License.

## Changelog
Please check out the [changelog](CHANGELOG.md) for more details.

## Contributing
Please check out the [checklist](CHECKLIST.md) and [contributing guideline](CONTRIBUTING.md) for more details.

## Installation
```sh
$ git clone https://github.com/congcoi123/tenio-core.git
```

> Happy coding !
