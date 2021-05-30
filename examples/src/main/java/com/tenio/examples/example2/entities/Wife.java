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
import com.tenio.examples.example2.states.wife.DoHouseWork;
import com.tenio.examples.example2.states.wife.WifesGlobalState;

public final class Wife extends BaseEntity {

	private FSMComponent<Wife> __state;
	private Location __location;
	/**
	 * Is she cooking?
	 */
	private boolean __flagCooking = false;

	public Wife(MessageDispatcher dispatcher, EntityName name) {
		super(name);
		__location = Location.SHACK;
		__state = new FSMComponent<Wife>(dispatcher, this);
		__state.setCurrentState(DoHouseWork.getInstance());
		__state.setGlobalState(WifesGlobalState.getInstance());
	}

	public FSMComponent<Wife> getFSM() {
		return __state;
	}

	public Location getLocation() {
		return __location;
	}

	public void changeLocation(Location location) {
		__location = location;
	}

	public boolean isCooking() {
		return __flagCooking;
	}

	public void setCooking(boolean flagCooking) {
		__flagCooking = flagCooking;
	}

	@Override
	public void update(float deltaTime) {
		__state.update(deltaTime);
	}

	@Override
	public boolean handleMessage(Telegram msg) {
		return __state.handleMessage(msg);
	}

}
