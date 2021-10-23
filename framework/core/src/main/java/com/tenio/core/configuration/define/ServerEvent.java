/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.core.configuration.define;

/**
 * All events could be emitted in the server.
 */
public enum ServerEvent {

  SESSION_CREATED,

  SESSION_REQUEST_CONNECTION,

  SESSION_OCCURRED_EXCEPTION,

  SESSION_WILL_BE_CLOSED,

  SESSION_READ_MESSAGE,

  DATAGRAM_CHANNEL_READ_MESSAGE,

  SERVER_INITIALIZATION,

  CONNECTION_ESTABLISHED_RESULT,

  PLAYER_LOGGEDIN_RESULT,

  PLAYER_RECONNECT_REQUEST_HANDLE,

  PLAYER_RECONNECTED_RESULT,

  SEND_MESSAGE_TO_PLAYER,

  RECEIVED_MESSAGE_FROM_PLAYER,

  ROOM_CREATED_RESULT,

  ROOM_WILL_BE_REMOVED,

  PLAYER_JOINED_ROOM_RESULT,

  PLAYER_BEFORE_LEAVE_ROOM,

  PLAYER_AFTER_LEFT_ROOM,

  SWITCH_PLAYER_TO_SPECTATOR,

  SWITCH_SPECTATOR_TO_PLAYER,

  DISCONNECT_PLAYER,

  DISCONNECT_CONNECTION,

  ATTACH_CONNECTION_REQUEST_VALIDATION,

  ATTACHED_CONNECTION_RESULT,

  FETCHED_CCU_INFO,

  FETCHED_BANDWIDTH_INFO,

  SYSTEM_MONITORING,

  HTTP_REQUEST_VALIDATION,

  HTTP_REQUEST_HANDLE,

  SERVER_EXCEPTION,

  SERVER_TEARDOWN;

  @Override
  public String toString() {
    return this.name();
  }
}
