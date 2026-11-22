# CHANGELOG

## 0.0.1 2021-10-14 Feature release
**Initial release**
- Entity Component System mechanism  
- Finite State Machine mechanism  
- Simple physic 2D support  
- Simple 2D Server Debugger support  
- GameLoop Management mechanism  

## 0.2.0 2022-04-20 Feature release
- Fulfilled java-docs for all classes
- Optimization

## 0.5.0 2023-08-30 Feature release
- Upgraded to JDK 17
- Upgraded dependencies
- Allowed checking logging configuration before writing logs

## 0.6.0 2024-10-09 Feature release
- Upgraded dependencies

## 0.6.1 2025-01-15 Bugfix release
- Addressed minor issues

## 0.6.2 2025-05-11 Feature release
- Upgraded dependencies
- Bugs fixed 

## 0.6.3 2025-08-01 Feature release
- Upgraded dependencies

## 0.6.4 2025-09-01 Bugfix release
- Upgraded dependencies
- Removed unnecessary wrapper methods

## 0.6.5 2025-09-02 Bugfix release
- Upgraded dependencies

## 0.6.8 2026-03-10 Bugfix release
- Upgraded dependencies

## 0.6.9 2026-03-23 Chore release
- Updated License

## 0.6.10 2026-03-26 Bugfix release
- Upgraded dependencies

## 0.7.0 2026-04-12 Feature release
- Supports Java 21 Features

## 1.0.0 2026-11-11 Feature release
- Updated the `tenio-common` dependency to 1.0.0.
- Synchronized ECS context operations, added entity-map snapshots and component-array copies, and reworked entity destruction, component reclamation, and context reset.
- Fixed entity and component pool expansion returning an already borrowed element. Pool construction now fails explicitly for invalid types or constructors, duplicate returns are rejected, and cleanup resets pools for reuse.
- Changed ECS system lists to support concurrent iteration and made running-state changes visible across threads.
- Made FSM entity registration atomic, returned entity-map snapshots, synchronized message dispatch, and handled removed message recipients and initial or missing previous FSM states.
- Gave telegrams and heartbeat messages identity-based equality and ordering tie-breakers so messages scheduled at the same time remain distinct.
- Reworked heartbeat pause, resume, interruption, and disposal handling. Added validation for initialization, duplicate IDs, missing heartbeats, and message delays, with cleanup after failed startup or removal.
- Corrected movement speed checks, normalized headings, and preserved rotated heading and velocity updates.
- Added defensive waypoint copies and explicit handling of empty paths, invalid bounds, and waypoint counts.
- Corrected circle overlap, containment, intersection, and intersection-area calculations, and handled coincident entity positions in non-penetration constraints.
- Restricted cell-space neighbor searches to relevant cells and validated partition dimensions, query radii, and vector operations.
- Replaced numeric and vector smoothing scans with running sums. Vector smoothing now copies input samples and returns a separate result.
- Changed world wrapping to use modulo arithmetic and return a new vector, and handled single-whisker generation.
- Fixed pixel, arrowhead, rectangle, and background rendering, and added an explicit error for missing icon resources.
- Updated tests for pool construction failures, message identity, movement checks, path copying, validation, smoothing, and world wrapping.
