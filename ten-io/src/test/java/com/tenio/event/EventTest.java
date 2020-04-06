/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.event;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.configuration.constant.LogicEvent;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.manager.PlayerManager;
import com.tenio.models.PlayerModel;

/**
 * @author kong
 */
public final class EventTest {

	private final PlayerManager __playerManager = new PlayerManager();
	private final int __testCCU[] = new int[2];
	private PlayerModel __testPlayer = null;

	@BeforeEach
	public void initialize() {
		// Create new player
		var player = new PlayerModel("kong");
		__playerManager.add(player);

		// Handle events
		EventManager.getEvent().on(TEvent.CCU, args -> {
			__testCCU[0] = (int) args[0];
			__testCCU[1] = (int) args[1];
			return null;
		});

		EventManager.getLogic().on(LogicEvent.GET_PLAYER, args -> {
			return (AbstractPlayer) args[0];
		});

		// Start to subscribe
		EventManager.subscribe();

		// Make events listener
		EventManager.getEvent().emit(TEvent.CCU, __playerManager.countPlayers(), __playerManager.count());
		__testPlayer = (PlayerModel) EventManager.getLogic().emit(LogicEvent.GET_PLAYER,
				__playerManager.get(player.getName()));

	}

	@AfterEach
	public void tearDown() {
		EventManager.clear();
	}

	@Test
	public void hasTEventSubscribeShouldReturnTrue() {
		assertEquals(true, EventManager.getEvent().hasSubscriber(TEvent.CCU));
	}

	@Test
	public void hasLogicEventSubscribeShouldReturnTrue() {
		assertEquals(true, EventManager.getLogic().hasSubscriber(LogicEvent.GET_PLAYER));
	}

	@Test
	public void getCCUEventShouldReturnTrueResult() {
		assertAll("CCU", () -> assertEquals(0, __testCCU[0]), () -> assertEquals(1, __testCCU[1]));
	}

	@Test
	public void getPlayerShouldReturnTrueResult() {
		assertEquals(__testPlayer, __playerManager.get("kong"));
	}

	@Test
	public void clearAllTEventShouldReturnZero() {
		EventManager.getEvent().clear();
		assertEquals(false, EventManager.getEvent().hasSubscriber(TEvent.CCU));
	}

	@Test
	public void clearAllLogicEventShouldReturnZero() {
		EventManager.getLogic().clear();
		assertEquals(false, EventManager.getLogic().hasSubscriber(LogicEvent.GET_PLAYER));
	}

}
