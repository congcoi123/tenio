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

package com.tenio.examples.example2.state.miner;

import com.tenio.engine.fsm.entity.State;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.define.Location;
import com.tenio.examples.example2.entity.Miner;

/**
 * In this state the miner will walk to a goldmine and pick up a nugget of gold. If the miner
 * already has a nugget of gold he'll change state to VisitBankAndDepositGold. If he gets thirsty
 * he'll change state to QuenchThirst.
 */
public final class EnterMineAndDigForNugget extends State<Miner> {

  private static volatile EnterMineAndDigForNugget instance;

  private EnterMineAndDigForNugget() {
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static EnterMineAndDigForNugget getInstance() {
    if (instance == null) {
      instance = new EnterMineAndDigForNugget();
    }
    return instance;
  }

  @Override
  public void enter(Miner miner) {
    // if the miner is not already located at the goldmine, he must
    // change location to the gold mine
    if (miner.getLocation() != Location.GOLD_MINE) {
      miner.setMood(miner.getName() + ": " + "Walkin' to the goldmine");
      System.out.println("\n" + miner.getMood());
      miner.changeLocation(Location.GOLD_MINE);
    }
  }

  @Override
  public void execute(Miner miner) {
    // if the miner is at the goldmine he digs for gold until he
    // is carrying in excess of MaxNuggets. If he gets thirsty during
    // his digging he packs up work for a while and changes state to
    // gp to the saloon for a whiskey.
    miner.addToGoldCarried(1);

    miner.increaseFatigue();

    miner.setMood(miner.getName() + ": " + "Pickin' up a nugget");
    System.out.println("\n" + miner.getMood());

    // if enough gold mined, go and put it in the bank
    if (miner.isPocketsFull()) {
      miner.getFsm().changeState(VisitBankAndDepositGold.getInstance());
    }

    if (miner.isThirsty()) {
      miner.getFsm().changeState(QuenchThirsty.getInstance());
    }

  }

  @Override
  public void exit(Miner miner) {
    miner.setMood(
        miner.getName() + ": " + "Ah'm leavin' the goldmine with mah pockets full o' sweet gold");
    System.out.println("\n" + miner.getMood());
  }

  @Override
  public boolean onMessage(Miner miner, Telegram msg) {
    return false;
  }
}
