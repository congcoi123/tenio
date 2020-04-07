# TenIO
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://travis-ci.org/congcoi123/tenio.svg?branch=master)](https://travis-ci.org/github/congcoi123/tenio)

TenIO is a java NIO (Non-blocking I/O) based server specifically designed for multiplayer games. It supports UDP and TCP transports which are handled by [Netty](https://netty.io/) for high-speed network transmission. It uses [MsgPack](https://msgpack.org/index.html) for compressing data so that can be transferred quickly through the network. This framework can help you quickly create a game server or integrate it into your system.

## Features
- Easy-to-use, OOP design.
- Based on standard Java development, ensuring cross-platform support.
- Simple event handlers implementation.
- Simple physic simulator and debugger.
- Have simple existing game clients for rapid development.

## First glimpse
- Simple Movement Simulation
![Simple Movement Simulation](https://github.com/congcoi123/tenio/blob/master/assets/movement-simulation-example-4.gif)
- Communication Simulation
![Communication](https://github.com/congcoi123/tenio/blob/master/assets/login-example-1.gif)
- Simple ECS (Entity Component System) Simulation
![ECS](https://github.com/congcoi123/tenio/blob/master/assets/ecs-example-5.gif)

## Wiki
The [wiki](https://github.com/congcoi123/tenio/wiki) provides implementation level details and answers to general questions that a developer starting to use TenIO might have about it.

### Clients
- [TenIOCocos2dx](https://github.com/congcoi123/tenio-cocos2dx.git)
- [TenIOLibgdx](https://github.com/congcoi123/tenio-libgdx.git)
- [TenIOJs](https://github.com/congcoi123/tenio-js.git)

## Dependencies
- netty-all 4.1.42.Final
- msgpack 0.6.12
- guava 25.1-jre
- log4j-core 2.11.1

## License
The TenIO project is currently available under the [MIT](https://github.com/congcoi123/tenio/blob/master/LICENSE) License.

## Changelog
Please check out the [changelog](https://github.com/congcoi123/tenio/blob/master/CHANGELOG.md) for more details.

## Installation
The maven repository will come soon, now you can get the sources:
```
git clone https://github.com/congcoi123/tenio.git
```

## Examples
Please start the server before its corresponding client in each example package.
![Examples](https://github.com/congcoi123/tenio/blob/master/assets/tenio-examples.png)

> Happy coding !
