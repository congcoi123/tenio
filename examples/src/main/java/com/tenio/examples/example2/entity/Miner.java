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

package com.tenio.examples.example2.entity;

import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.engine.fsm.entity.FsmComponent;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.define.EntityName;
import com.tenio.examples.example2.define.Location;
import com.tenio.examples.example2.state.miner.GoHomeAndSleepTilRested;

public final class Miner extends BaseEntity {
  /**
   * The amount of gold a miner must have before he feels comfortable.
   */
  public static final int COMFORT_LEVEL = 5;
  /**
   * The amount of nuggets a miner can carry.
   */
  public static final int MAX_NUGGETS = 3;
  /**
   * A miner is thirsty (level).
   */
  public static final int THIRSTY_LEVEL = 5;
  /**
   * A miner is sleepy.
   */
  public static final int TIREDNESS_THRESHOLD = 5;
  /**
   * see FSMComponent.
   */
  private final FsmComponent<Miner> state;
  /**
   * see Location.
   */
  private Location location;
  /**
   * The number of nuggets the miner has in his pockets.
   */
  private int numberGoldCarried;
  /**
   * The amount of money the miner has in his bank account.
   */
  private int numberMoneyInBank;
  /**
   * The higher the value, the thirstier the miner.
   */
  private int numberThirst;
  /**
   * The higher the value, the more tired the miner.
   */
  private int numberFatigue;

  public Miner(MessageDispatcher dispatcher, EntityName name) {
    super(name);
    location = Location.SHACK;
    numberGoldCarried = 0;
    numberMoneyInBank = 0;
    numberThirst = 0;
    numberFatigue = 0;

    state = new FsmComponent<>(dispatcher, this);
    state.setCurrentState(GoHomeAndSleepTilRested.getInstance());
  }

  @Override
  public void update(float delta) {
    numberThirst += 1;
    state.update(delta);
  }

  @Override
  public boolean handleMessage(Telegram msg) {
    return state.handleMessage(msg);
  }

  public FsmComponent<Miner> getFsm() {
    return state;
  }

  public void addToGoldCarried(int val) {
    numberGoldCarried += val;

    if (numberGoldCarried < 0) {
      numberGoldCarried = 0;
    }
  }

  public void addToMoneyInBank(int val) {
    numberMoneyInBank += val;

    if (numberMoneyInBank < 0) {
      numberMoneyInBank = 0;
    }
  }

  public boolean isThirsty() {
    return numberThirst >= THIRSTY_LEVEL;
  }

  public boolean isFatigued() {
    return numberFatigue > TIREDNESS_THRESHOLD;
  }

  public Location getLocation() {
    return location;
  }

  public void changeLocation(Location location) {
    this.location = location;
  }

  public int getNumberGoldCarried() {
    return numberGoldCarried;
  }

  public void setNumberGoldCarried(int val) {
    numberGoldCarried = val;
  }

  public boolean isPocketsFull() {
    return numberGoldCarried >= MAX_NUGGETS;
  }

  public void decreaseFatigue() {
    numberFatigue -= 1;
  }

  public void increaseFatigue() {
    numberFatigue += 1;
  }

  public int getNumberMoneyInBank() {
    return numberMoneyInBank;
  }

  public void setNumberMoneyInBank(int val) {
    numberMoneyInBank = val;
  }

  public void buyAndDrinkAWhiskey() {
    numberThirst = 0;
    numberMoneyInBank -= 2;
  }
}
