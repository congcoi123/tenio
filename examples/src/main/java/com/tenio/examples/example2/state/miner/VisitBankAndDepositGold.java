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

package com.tenio.examples.example2.state.miner;

import com.tenio.engine.fsm.entity.State;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.define.Location;
import com.tenio.examples.example2.entity.Miner;

/**
 * Entity will go to a bank and deposit any nuggets he is carrying. If the miner is subsequently
 * wealthy enough he'll walk home, otherwise he'll keep going to get more gold.
 */
public final class VisitBankAndDepositGold extends State<Miner> {

  private static volatile VisitBankAndDepositGold instance;

  private VisitBankAndDepositGold() {
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static VisitBankAndDepositGold getInstance() {
    if (instance == null) {
      instance = new VisitBankAndDepositGold();
    }
    return instance;
  }

  @Override
  public void enter(Miner miner) {
    // on entry the miner makes sure he is located at the bank
    if (miner.getLocation() != Location.BANK) {
      miner.setMood(miner.getName() + ": " + "Goin' to the bank. Yes siree");
      System.out.println("\n" + miner.getMood());

      miner.changeLocation(Location.BANK);
    }
  }

  @Override
  public void execute(Miner miner) {

    // deposit the gold
    miner.addToMoneyInBank(miner.getNumberGoldCarried());

    miner.setNumberGoldCarried(0);

    miner.setMood(miner.getName() + ": " + "Depositing gold. Total savings now: " +
        miner.getNumberMoneyInBank());
    System.out.println("\n" + miner.getMood());

    // wealthy enough to have a well earned rest?
    if (miner.getNumberMoneyInBank() >= Miner.COMFORT_LEVEL) {
      miner.setMood(
          miner.getName() + ": " + "WooHoo! Rich enough for now. Back home to mah li'lle lady");
      System.out.println("\n" + miner.getMood());

      miner.getFsm().changeState(GoHomeAndSleepTilRested.getInstance());
    } // otherwise get more gold
    else {
      miner.getFsm().changeState(EnterMineAndDigForNugget.getInstance());
    }
  }

  @Override
  public void exit(Miner miner) {
    miner.setMood(miner.getName() + ": " + "Leavin' the bank");
    System.out.println("\n" + miner.getMood());
  }

  @Override
  public boolean onMessage(Miner miner, Telegram msg) {
    return false;
  }
}
