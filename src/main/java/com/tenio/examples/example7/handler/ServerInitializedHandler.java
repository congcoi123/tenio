/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.examples.example7.handler;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.bootstrap.annotation.EventHandler;
import com.tenio.core.handler.AbstractHandler;
import com.tenio.core.handler.event.EventServerInitialization;
import java.text.SimpleDateFormat;
import java.util.Date;

@EventHandler
public final class ServerInitializedHandler extends AbstractHandler
    implements EventServerInitialization {

  private final String pattern = "yyyy-MM-dd HH:mm:ss";
  private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

  @Override
  public void handle(String serverName, Configuration configuration) {
    info("SERVER INITIALIZATION",
        String.format("Started: %s", simpleDateFormat.format(new Date(api().getStartedTime()))));

    var roomSetting =
        roomSetting().setActivated(true).setMaxParticipants(2).setName("test-room").build();

    var room = api().createRoom(roomSetting);
    info("SERVER INITIALIZATION", String.format("Created room: %s", room.toString()));
  }
}
