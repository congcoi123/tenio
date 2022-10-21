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
import com.tenio.examples.example2.entity.Miner;

/**
 * This is implemented as a state blip. The miner eats the stew, gives his wife
 * some compliments and then returns to his previous state.
 */
public final class EatStew extends State<Miner> {

  private static volatile EatStew instance;

  private EatStew() {
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static EatStew getInstance() {
    if (instance == null) {
      instance = new EatStew();
    }
    return instance;
  }

  @Override
  public void enter(Miner miner) {
    miner.setMood(miner.getName() + ": " + "Smells Reaaal goood Elsa!");
    System.out.println("\n" + miner.getMood());
  }

  @Override
  public void execute(Miner miner) {
    miner.setMood(miner.getName() + ": " + "Tastes real good too!");
    System.out.println("\n" + miner.getMood());
    miner.getFsm().revertToPreviousState();
  }

  @Override
  public void exit(Miner miner) {
    miner.setMood(miner.getName() + ": " +
        "Thankya li'lle lady. Ah better get back to whatever ah wuz doin'");
    System.out.println("\n" + miner.getMood());
  }

  @Override
  public boolean onMessage(Miner miner, Telegram msg) {
    return false;
  }
}
