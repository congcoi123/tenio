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
package com.tenio.core.configuration.defines;

/**
 * This Enum defines all logic events in the main thread. All the process should
 * be handled in <b>ServerLogic</b> class.
 * 
 * @author kong
 */
// FIXME: Fix me
public enum InternalEvent {

	SERVER_STARTED,

	SESSION_WAS_CREATED,

	SESSION_WAS_CONNECTED,
	
	SESSION_OCCURED_EXCEPTION,

	SESSION_WAS_CLOSED,

	SESSION_READ_BINARY,

	SESSION_EXCEPTION,
	
	DATAGRAM_CHANNEL_READ_BINARY,

	FORCE_PLAYER_TO_LEAVE_ROOM;

}
