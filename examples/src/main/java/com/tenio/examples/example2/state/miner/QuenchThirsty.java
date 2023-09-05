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
 * Miner changes location to the saloon and keeps buying Whiskey until his thirst is quenched.
 * When satisfied he returns to the goldmine and resumes his quest for nuggets.
 */
public final class QuenchThirsty extends State<Miner> {

  private static volatile QuenchThirsty instance;

  private QuenchThirsty() {
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static QuenchThirsty getInstance() {
    if (instance == null) {
      instance = new QuenchThirsty();
    }
    return instance;
  }

  @Override
  public void enter(Miner miner) {
    if (miner.getLocation() != Location.SALOON) {
      miner.changeLocation(Location.SALOON);

      miner.setMood(miner.getName() + ": " + "Boy, ah sure is thusty! Walking to the saloon");
      System.out.println("\n" + miner.getMood());
    }
  }

  @Override
  public void execute(Miner miner) {

    miner.buyAndDrinkAWhiskey();

    miner.setMood(miner.getName() + ": " + "That's mighty fine sippin liquer");
    System.out.println("\n" + miner.getMood());

    miner.getFsm().changeState(EnterMineAndDigForNugget.getInstance());
  }

  @Override
  public void exit(Miner miner) {
    miner.setMood(miner.getName() + ": " + "Leaving the saloon, feelin' good");
    System.out.println("\n" + miner.getMood());
  }

  @Override
  public boolean onMessage(Miner miner, Telegram msg) {
    return false;
  }
}
