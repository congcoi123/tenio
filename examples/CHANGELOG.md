# CHANGELOG

### 0.0.1 2021-10-15 Feature release
**Initial release**
- Login example  
- FSM example  
- UDP attaching example  
- Stress test  
- Websocket example  
- HTTP example  

### 0.2.0 2022-04-20 Feature release
- Updated the framework dependencies  

### 0.3.0 2022-10-14 Feature release
- Supported multiple UDP channels
- Allowed fetching available UDP channel by using round-robin mechanism
- Refactoring: renamed package "extension" to "handler"
- Allowed declaring the server address in configuration file
- Supported KCP transportation
- Bugs fixed

### 0.3.1 2022-10-21 Feature release
- Added new example to show how to use MsgPack
- Added new events to handle connection-refused cases
- WebSocket handling issues fixed
- Allowed showing server's uptime
- Bugs fixed

## 0.4.0 2022-11-20 Feature release
- Introduces more annotations
- Supports self-defined commands
- Refactoring the project's structure
- Bugs fixed  

## 0.5.0 2023-08-30 Feature release
- Upgraded to JDK 17
- Upgraded dependencies
- Allowed checking logging configuration before writing logs
- configuration.xml file changed
- Introduced new annotations: @RestController and @RestMapping to work with Restful, @ClientCommand
- Added a new configuration value that allows limiting the player's IDLE time in case of non-deported state enabled
- Methods changed/updated/enhanced in classes ServerApi, Injector, ServerEvent
- Annotations enhanced: @Bean, @AutowiredQualifier
- Set names for all necessary threads
- Resolved the deadlock issue while writing packets
- Player and Room are now opened for custom classes
- Removed redundant constant, enum values
- Fixed test cases' issues
- Bugs fixed 

## 0.6.0 2024-10-09 Feature release
- Upgraded dependencies
- Enhanced performances and addressed the issue related to "100% CPU"
- Reworked on MsgPack classes and methods
- Bugs fixed

## 0.6.2 2025-05-11 Feature release
- Upgraded dependencies
- Declared behaviors for packets processing with annotations
- Bugs fixed

## 0.6.3 2025-06-01 Feature release
- Upgraded dependencies

## 0.6.4 2025-07-01 Feature release
- Upgraded dependencies

## 0.6.5 2025-08-01 Feature release
- Upgraded dependencies

## 0.6.6 2025-09-01 Feature release
- Upgraded dependencies
- Removed unnecessary wrapper methods

## 0.6.7 2025-09-02 Feature release
- Upgraded dependencies
- Adapted new APIs

## 0.6.8 2026-03-10 Feature release
- Upgraded dependencies

## 0.6.9 2026-03-23 Feature release
- Upgraded dependencies

## 0.6.10 2026-03-26 Feature release
- Upgraded dependencies
- Allowed default configurations

## 0.6.11 2026-04-03 Feature release
- Upgraded dependencies
- Stopped supporting KCP transportation

## 0.6.12 2026-04-04 Feature release
- Upgraded dependencies
- Simplified setting of the configuration file

## 0.7.0 2026-04-12 Feature release
- Supports Java 21 Features

## 0.7.1 2026-04-19 Feature release
- Upgraded dependencies

## 0.7.2 2026-04-25 Feature release
- Upgraded dependencies