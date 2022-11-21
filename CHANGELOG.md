# CHANGELOG

## 0.0.1 2021-10-13 Feature release
**Initial release**
- NIO mechanism  
- Server configuration  
- Events handling  
- Player and Room management  
- Session management  
- TCP, UDP and WebSocket support  
- Server monitoring  
- Schedule management  

## 0.2.0 2022-04-20 Feature release
- Fulfilled java-docs for all classes
- Optimization
- Refactoring the project's structure
- Bugs fixed  

## 0.3.0 2022-10-13 Feature release
- Supported multiple UDP channels
- Allowed fetching available UDP channel by using round-robin mechanism
- Refactoring: renamed package "extension" to "handler"
- Allowed declaring the server address in configuration file
- Supported KCP transportation
- Bugs fixed

## 0.3.1 2022-10-21 Feature release
- Multiple data serialization methods supported (MsgPack)
- Added new events to handle connection refused cases
- WebSocket handling issues fixed
- Allowed showing server's uptime 
- Refactoring the project's structure
- Bugs fixed  

## 0.4.0 2022-11-20 Feature release
- Adapted annotations' management from the [tenio-common](https://github.com/congcoi123/tenio-common) module
- Introduces more annotations
- Supports self-defined commands
- Refactoring the project's structure
- Bugs fixed  
