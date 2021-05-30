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
package com.tenio.examples.example2.states.miner;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.engine.constant.EngineConstant;
import com.tenio.engine.fsm.entity.State;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.defines.EntityName;
import com.tenio.examples.example2.defines.Location;
import com.tenio.examples.example2.defines.MessageType;
import com.tenio.examples.example2.entities.Miner;

/**
 * Miner will go home and sleep until his fatigue is decreased sufficiently
 */
public final class GoHomeAndSleepTilRested extends State<Miner> {

	private static volatile GoHomeAndSleepTilRested __instance;

	private GoHomeAndSleepTilRested() {
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static GoHomeAndSleepTilRested getInstance() {
		if (__instance == null) {
			__instance = new GoHomeAndSleepTilRested();
		}
		return __instance;
	}

	@Override
	public void enter(Miner miner) {
		if (miner.getLocation() != Location.SHACK) {
			miner.setMood(miner.getName() + ": " + "Walkin' home");
			System.out.println("\n" + miner.getMood());
			miner.changeLocation(Location.SHACK);
		}

		// let the wife know I'm home
		miner.getFSM().getDispatcher().dispatchMessage(EngineConstant.SEND_MSG_IMMEDIATELY, // time delay
				miner.getId(), // ID of sender
				EntityName.WIFE.get(), // ID of recipient
				MessageType.HI_HONEY_IM_HOME.get(), // the message
				null);
	}

	@Override
	public void execute(Miner miner) {
		// if miner is not fatigued start to dig for nuggets again.
		if (!miner.isFatigued()) {
			miner.setMood(miner.getName() + ": " + "What a God darn fantastic nap! Time to find more gold");
			System.out.println("\n" + miner.getMood());

			miner.getFSM().changeState(EnterMineAndDigForNugget.getInstance());
		} else {
			// sleep
			miner.decreaseFatigue();

			miner.setMood(miner.getName() + ": " + "ZZZZ... ");
			System.out.println("\n" + miner.getMood());
		}
	}

	@Override
	public void exit(Miner miner) {
		miner.setMood(miner.getName() + ": " + "Leaving the house");
		System.out.println("\n" + miner.getMood());
	}

	@Override
	public boolean onMessage(Miner miner, Telegram msg) {
		if (msg.getType() == MessageType.STEW_READY.get()) {
			System.out.println("\nMessage handled by " + miner.getName() + " at time: "
					+ (TimeUtility.currentTimeSeconds() - msg.getCreatedTime()));

			miner.setMood(miner.getName() + ": Okay Hun, ahm a comin'!");
			System.out.println("\n" + miner.getMood());

			miner.getFSM().changeState(EatStew.getInstance());

			return true;

		}

		return false; // send message to global message handler
	}

}
