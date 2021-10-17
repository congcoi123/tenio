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

package com.tenio.core.bootstrap.event;

import com.tenio.common.bootstrap.annotation.Autowired;
import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.event.handlers.ConnectionEventHandler;
import com.tenio.core.bootstrap.event.handlers.HttpEventHandler;
import com.tenio.core.bootstrap.event.handlers.MixinsEventHandler;
import com.tenio.core.bootstrap.event.handlers.PlayerEventHandler;
import com.tenio.core.bootstrap.event.handlers.RoomEventHandler;
import com.tenio.core.event.implement.EventManager;

/**
 * Dispatching all events in the server.
 */
@Component
public final class EventHandler {

  @Autowired
  private ConnectionEventHandler connectionEventHandler;

  @Autowired
  private PlayerEventHandler playerEventHandler;

  @Autowired
  private RoomEventHandler roomEventHandler;

  @Autowired
  private HttpEventHandler httpEventHandler;

  @Autowired
  private MixinsEventHandler mixinsEventHandler;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {
    connectionEventHandler.initialize(eventManager);
    playerEventHandler.initialize(eventManager);
    roomEventHandler.initialize(eventManager);
    httpEventHandler.initialize(eventManager);
    mixinsEventHandler.initialize(eventManager);
  }
}
