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
package com.tenio.examples.example2.entities;

import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.engine.fsm.entity.FSMComponent;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.defines.EntityName;
import com.tenio.examples.example2.defines.Location;
import com.tenio.examples.example2.states.miner.GoHomeAndSleepTilRested;

public final class Miner extends BaseEntity {
	/**
	 * The amount of gold a miner must have before he feels comfortable
	 */
	public static final int COMFORT_LEVEL = 5;
	/**
	 * The amount of nuggets a miner can carry
	 */
	public static final int MAX_NUGGETS = 3;
	/**
	 * A miner is thirsty (level)
	 */
	public static final int THIRSTY_LEVEL = 5;
	/**
	 * A miner is sleepy
	 */
	public static final int TIREDNESS_THRESHOLD = 5;
	/**
	 * see FSMComponent
	 */
	private FSMComponent<Miner> __state;
	/**
	 * see Location
	 */
	private Location __location;
	/**
	 * The number of nuggets the miner has in his pockets
	 */
	private int __numberGoldCarried;
	/**
	 * The amount of money the miner has in his bank account
	 */
	private int __numberMoneyInBank;
	/**
	 * The higher the value, the thirstier the miner
	 */
	private int __numberThirst;
	/**
	 * The higher the value, the more tired the miner
	 */
	private int __numberFatigue;

	public Miner(MessageDispatcher dispatcher, EntityName name) {
		super(name);
		__location = Location.SHACK;
		__numberGoldCarried = 0;
		__numberMoneyInBank = 0;
		__numberThirst = 0;
		__numberFatigue = 0;

		__state = new FSMComponent<Miner>(dispatcher, this);
		__state.setCurrentState(GoHomeAndSleepTilRested.getInstance());

	}

	@Override
	public void update(float delta) {
		__numberThirst += 1;
		__state.update(delta);
	}

	@Override
	public boolean handleMessage(Telegram msg) {
		return __state.handleMessage(msg);
	}

	public FSMComponent<Miner> getFSM() {
		return __state;
	}

	public void addToGoldCarried(int val) {
		__numberGoldCarried += val;

		if (__numberGoldCarried < 0) {
			__numberGoldCarried = 0;
		}
	}

	public void addToMoneyInBank(int val) {
		__numberMoneyInBank += val;

		if (__numberMoneyInBank < 0) {
			__numberMoneyInBank = 0;
		}
	}

	public boolean isThirsty() {
		if (__numberThirst >= THIRSTY_LEVEL) {
			return true;
		}

		return false;
	}

	public boolean isFatigued() {
		if (__numberFatigue > TIREDNESS_THRESHOLD) {
			return true;
		}

		return false;
	}

	public Location getLocation() {
		return __location;
	}

	public void changeLocation(Location location) {
		__location = location;
	}

	public int getNumberGoldCarried() {
		return __numberGoldCarried;
	}

	public void setNumberGoldCarried(int val) {
		__numberGoldCarried = val;
	}

	public boolean isPocketsFull() {
		return __numberGoldCarried >= MAX_NUGGETS;
	}

	public void decreaseFatigue() {
		__numberFatigue -= 1;
	}

	public void increaseFatigue() {
		__numberFatigue += 1;
	}

	public int getNumberMoneyInBank() {
		return __numberMoneyInBank;
	}

	public void setNumberMoneyInBank(int val) {
		__numberMoneyInBank = val;
	}

	public void buyAndDrinkAWhiskey() {
		__numberThirst = 0;
		__numberMoneyInBank -= 2;
	}

}
