<p align="center">
    <img src="assets/tenio-github-logo.png">
</p>
<p align="center">
    <a href="https://mvnrepository.com/artifact/io.github.congcoi123/tenio">
        <img src="https://img.shields.io/maven-central/v/io.github.congcoi123/tenio.svg"
             alt="maven-central">
    </a>
    <a href="https://javadoc.io/doc/io.github.congcoi123/tenio">
        <img src="https://javadoc.io/badge2/io.github.congcoi123/tenio/javadoc.svg"
             alt="javadoc">
    </a>
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/license-MIT-blue.svg"
             alt="license">
    </a>
    <a href="https://travis-ci.org/github/congcoi123/tenio">
        <img src="https://travis-ci.org/congcoi123/tenio.svg?branch=master"
             alt="build">
    </a>
    <a href="https://coveralls.io/github/congcoi123/tenio">
        <img src="https://coveralls.io/repos/github/congcoi123/tenio/badge.svg?branch=master"
             alt="coverage">
    </a>
    <a href="#">
        <img src="https://img.shields.io/github/last-commit/congcoi123/tenio"
             alt="last-commit">
    </a>
    <a href="https://github.com/congcoi123/tenio/issues">
        <img src="https://img.shields.io/github/issues/congcoi123/tenio"
             alt="issues">
    </a>
    <a href="CONTRIBUTING.md">
        <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg"
             alt="contributing">
    </a>
    <a href="https://gitter.im/ten-io/community?source=orgpage">
        <img src="https://badges.gitter.im/Join%20Chat.svg"
             alt="join-chat">
    </a>
</p>

# TenIO
`TenIO` is a java NIO (Non-blocking I/O) based server specifically designed for multiplayer games. It supports UDP and TCP transports which are handled by [Netty](https://netty.io/) for high-speed network transmission. It uses [MsgPack](https://msgpack.org/index.html) for compressing data so that can be transferred quickly through the network. This framework can help you quickly create a game server or integrate it into your system.

## Features
- Easy-to-use, OOP design.
- Based on standard Java development, ensuring cross-platform support.
- Simple event handlers implementation.
- Simple physic simulator and debugger.
- Have simple existing game clients for rapid development.

## Showcases
<p align="center">
    <a href="https://www.youtube.com/watch?v=BBv5IQFHLjc">
        <img src="assets/gold-miner-online-logo.png" alt="gold miner online"><br/>
        <p><b>Gold Miner Online</b></p>
    </a>
    <a href="https://www.youtube.com/watch?v=nojkJMAfG6Y">
        <img src="assets/retro-brick-online-logo.png" alt="retro brick game online"><br/>
        <p><b>Retro Brick Game Online</b></p>
    </a>
</p>

## First glimpse
- Simple Movement Simulation  
![Simple Movement Simulation](assets/movement-simulation-example-4.gif)
- Communication Simulation  
![Communication](assets/login-example-1.gif)

## Wiki
The [wiki](https://github.com/congcoi123/tenio/wiki) provides implementation level details and answers to general questions that a developer starting to use `TenIO` might have about it.

## Clients
<p align="center">
    <a href="https://github.com/congcoi123/tenio-cocos2dx.git">
        <img src="assets/cocos2dx-logo.png" alt="tenio cocos2dx"><br/>
        <p><b>TenIO Cocos2dx</b></p>
    </a>
    <a href="https://github.com/congcoi123/tenio-libgdx.git">
        <img src="assets/libgdx-logo.png" alt="tenio libgdx"><br/>
        <p><b>TenIO Libgdx</b></p>
    </a>
    <a href="https://github.com/congcoi123/tenio-unity.git">
        <img src="assets/unity-logo.png" alt="tenio unity"><br/>
        <p><b>TenIO Unity</b></p>
    </a>
    <a href="https://github.com/congcoi123/tenio-phaserjs.git">
        <img src="assets/phaserjs-logo.png" alt="tenio phaserjs"><br/>
        <p><b>TenIO Phaserjs</b></p>
    </a>
</p>

## Dependencies
- guava 29.0-jre
- netty-all 4.1.50.Final
- servlet-api 2.5
- log4j-core 2.13.3
- jetty-server 9.4.29.v20200521
- jetty-servlet 9.4.29.v20200521
- msgpack 0.6.12

## Requirements
- Java 11

## License
The `TenIO` project is currently available under the [MIT](LICENSE) License.

## Changelog
Please check out the [changelog](CHANGELOG.md) for more details.

## Contributing
Please check out the [contributing guideline](CONTRIBUTING.md) for more details.

## Installation
Maven
```xml
<dependency>
    <groupId>io.github.congcoi123</groupId>
    <artifactId>tenio</artifactId>
    <version>3.1.1</version>
</dependency>
```
Or you can get the sources
```sh
$ git clone https://github.com/congcoi123/tenio.git
```

## Examples
Please start the server before its corresponding client in each example package.

```txt
|-- example
    |-- example1
    |   |-- TestClientLogin.java
    |   |-- TestServerLogin.java
    |-- example2
    |   |-- TestClientFSM.java
    |   |-- TestFSM.java
    |   |-- TestServerFSM.java
    |-- example3
    |   |-- TestClientAttach.java
    |   |-- TestServerAttach.java
    |-- example4
    |   |-- TestClientMovement.java
    |   |-- TestMovement.java
    |   |-- TestServerMovement.java
    |-- example5
    |   |-- TestECS.java
    |-- example6
    |   |-- TestClientStress.java
    |   |-- TestServerStress.java
    |-- example7
    |   |-- TestServerPhaserjs.java
```

> Happy coding !
