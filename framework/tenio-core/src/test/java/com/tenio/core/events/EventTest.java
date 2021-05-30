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
package com.tenio.core.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.implement.PlayerImpl;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.implement.PlayerManagerImpl;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedPlayerException;

public final class EventTest {

	private EventManager __eventManager;
	private PlayerManager __playerManager;
	private int __testCCU;

	@BeforeEach
	public void initialize() {
		__eventManager = EventManager.newInstance();
		__playerManager = PlayerManagerImpl.newInstance(__eventManager);

		// Create new player
		var player = PlayerImpl.newInstance("kong");
		try {
			__playerManager.addPlayer(player);
		} catch (AddedDuplicatedPlayerException e) {
			e.printStackTrace();
		}

		// Handle events
		__eventManager.on(ServerEvent.FETCHED_CCU_INFO, params -> {
			__testCCU = (int) params[0];
			return null;
		});

		// Start to subscribe
		__eventManager.subscribe();

		// Make events listener
		__eventManager.emit(ServerEvent.FETCHED_CCU_INFO, __playerManager.getPlayerCount());

	}

	@AfterEach
	public void tearDown() {
		__playerManager.clear();
		__eventManager.clear();
	}

	@Test
	public void hasTEventSubscribeShouldReturnTrue() {
		assertTrue(__eventManager.hasSubscriber(ServerEvent.FETCHED_CCU_INFO));
	}

	@Test
	public void getCCUEventShouldReturnTrueResult() {
		assertAll("CCU", () -> assertEquals(1, __testCCU));
	}

	@Test
	public void clearAllTEventShouldReturnZero() {
		__eventManager.clear();

		assertFalse(__eventManager.hasSubscriber(ServerEvent.FETCHED_CCU_INFO));
	}

}
