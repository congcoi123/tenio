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
package com.tenio.core.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.core.configuration.defines.ExtensionEvent;
import com.tenio.core.configuration.defines.InternalEvent;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.implement.PlayerManagerImpl;
import com.tenio.core.events.EventManager;
import com.tenio.core.events.implement.EventManagerImpl;
import com.tenio.core.exceptions.DuplicatedPlayerException;
import com.tenio.core.model.PlayerModel;

/**
 * @author kong
 */
public final class EventTest {

	private EventManager __eventManager;
	private PlayerManager __playerManager;
	private int[] __testCCU;

	@BeforeEach
	public void initialize() {
		__eventManager = new EventManagerImpl();
		__playerManager = new PlayerManagerImpl(__eventManager);
		__testCCU = new int[2];

		// Create new player
		var player = new PlayerModel("kong");
		try {
			__playerManager.add(player);
		} catch (DuplicatedPlayerException e) {
			e.printStackTrace();
		}

		// Handle events
		__eventManager.getExtension().on(ExtensionEvent.FETCHED_CCU_NUMBER, args -> {
			__testCCU[0] = (int) args[0];
			__testCCU[1] = (int) args[1];
			return null;
		});

		__eventManager.getInternal().on(InternalEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, args -> {

			return null;
		});

		// Start to subscribe
		__eventManager.subscribe();

		// Make events listener
		__eventManager.getExtension().emit(ExtensionEvent.FETCHED_CCU_NUMBER, __playerManager.countPlayers(), __playerManager.count());

	}

	@AfterEach
	public void tearDown() {
		__playerManager.clear();
		__eventManager.clear();
	}

	@Test
	public void hasTEventSubscribeShouldReturnTrue() {
		assertTrue(__eventManager.getExtension().hasSubscriber(ExtensionEvent.FETCHED_CCU_NUMBER));
	}

	@Test
	public void getCCUEventShouldReturnTrueResult() {
		assertAll("CCU", () -> assertEquals(0, __testCCU[0]), () -> assertEquals(1, __testCCU[1]));
	}

	@Test
	public void clearAllTEventShouldReturnZero() {
		__eventManager.getExtension().clear();

		assertFalse(__eventManager.getExtension().hasSubscriber(ExtensionEvent.FETCHED_CCU_NUMBER));
	}

}
