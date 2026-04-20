/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.event.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.core.event.handler.implement.ChannelEventHandler;
import com.tenio.core.event.handler.implement.ConnectionEventHandler;
import com.tenio.core.event.handler.implement.MixinsEventHandler;
import com.tenio.core.event.handler.implement.PlayerEventHandler;
import com.tenio.core.event.handler.implement.RoomEventHandler;
import com.tenio.core.event.implement.EventManager;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For EventHandler")
class EventHandlerTest {

  @Test
  @DisplayName("initialize delegates to all five event handlers")
  void testInitializeDelegatesToAllHandlers() throws Exception {
    EventHandler eventHandler = new EventHandler();

    ConnectionEventHandler connectionEventHandler = mock(ConnectionEventHandler.class);
    PlayerEventHandler playerEventHandler = mock(PlayerEventHandler.class);
    RoomEventHandler roomEventHandler = mock(RoomEventHandler.class);
    ChannelEventHandler channelEventHandler = mock(ChannelEventHandler.class);
    MixinsEventHandler mixinsEventHandler = mock(MixinsEventHandler.class);

    setField(eventHandler, "connectionEventHandler", connectionEventHandler);
    setField(eventHandler, "playerEventHandler", playerEventHandler);
    setField(eventHandler, "roomEventHandler", roomEventHandler);
    setField(eventHandler, "channelEventHandler", channelEventHandler);
    setField(eventHandler, "mixinsEventHandler", mixinsEventHandler);

    EventManager eventManager = mock(EventManager.class);
    eventHandler.initialize(eventManager);

    verify(connectionEventHandler).initialize(eventManager);
    verify(playerEventHandler).initialize(eventManager);
    verify(roomEventHandler).initialize(eventManager);
    verify(channelEventHandler).initialize(eventManager);
    verify(mixinsEventHandler).initialize(eventManager);
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
