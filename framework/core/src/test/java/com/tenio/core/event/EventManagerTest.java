/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.event;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.implement.DefaultPlayer;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.implement.PlayerManagerImpl;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.AddedDuplicatedPlayerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventManagerTest {

  private EventManager eventManager;
  private PlayerManager playerManager;
  private int testCCU;

  @BeforeEach
  public void initialize() {
    eventManager = EventManager.newInstance();
    playerManager = PlayerManagerImpl.newInstance(eventManager);

    // Create new player
    var player = DefaultPlayer.newInstance("kong");
    try {
      playerManager.addPlayer(player);
    } catch (AddedDuplicatedPlayerException exception) {
      exception.printStackTrace();
    }

    // Handle events
    eventManager.on(ServerEvent.FETCHED_CCU_INFO, params -> {
      testCCU = (int) params[0];
      return null;
    });

    // Start to subscribe
    eventManager.subscribe();

    // Make events listener
    eventManager.emit(ServerEvent.FETCHED_CCU_INFO, playerManager.getPlayerCount());

  }

  @AfterEach
  public void tearDown() {
    playerManager.clear();
    eventManager.clear();
  }

  @Test
  public void hasTEventSubscribeShouldReturnTrue() {
    Assertions.assertTrue(eventManager.hasSubscriber(ServerEvent.FETCHED_CCU_INFO));
  }

  @Test
  public void getCCUEventShouldReturnTrueResult() {
    assertAll("CCU", () -> assertEquals(1, testCCU));
  }

  @Test
  public void clearAllTEventShouldReturnZero() {
    eventManager.clear();

    Assertions.assertFalse(eventManager.hasSubscriber(ServerEvent.FETCHED_CCU_INFO));
  }
}
