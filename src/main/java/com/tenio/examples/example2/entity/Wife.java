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

package com.tenio.examples.example2.entity;

import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.engine.fsm.entity.FsmComponent;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.define.EntityName;
import com.tenio.examples.example2.define.Location;
import com.tenio.examples.example2.state.wife.DoHouseWork;
import com.tenio.examples.example2.state.wife.WifesGlobalState;

public final class Wife extends BaseEntity {

  private final FsmComponent<Wife> state;
  private Location location;
  /**
   * Is she cooking?
   */
  private boolean flagCooking = false;

  public Wife(MessageDispatcher dispatcher, EntityName name) {
    super(name);
    location = Location.SHACK;
    state = new FsmComponent<>(dispatcher, this);
    state.setCurrentState(DoHouseWork.getInstance());
    state.setGlobalState(WifesGlobalState.getInstance());
  }

  public FsmComponent<Wife> getFsm() {
    return state;
  }

  public Location getLocation() {
    return location;
  }

  public void changeLocation(Location location) {
    this.location = location;
  }

  public boolean isCooking() {
    return flagCooking;
  }

  public void setCooking(boolean flagCooking) {
    this.flagCooking = flagCooking;
  }

  @Override
  public void update(float deltaTime) {
    state.update(deltaTime);
  }

  @Override
  public boolean handleMessage(Telegram msg) {
    return state.handleMessage(msg);
  }
}
