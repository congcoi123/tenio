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

package com.tenio.examples.example2.state.wife;

import com.tenio.engine.fsm.entity.State;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.entity.Wife;

/**
 * The miner's wife start checking the bathroom.
 */
public final class VisitBathroom extends State<Wife> {

  private static volatile VisitBathroom instance;

  private VisitBathroom() {
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static VisitBathroom getInstance() {
    if (instance == null) {
      instance = new VisitBathroom();
    }
    return instance;
  }

  @Override
  public void enter(Wife wife) {
    wife.setMood(wife.getName() + ": Walkin' to the can. Need to powda mah pretty li'lle nose");
    System.out.println("\n" + wife.getMood());
  }

  @Override
  public void execute(Wife wife) {
    wife.setMood(wife.getName() + ": Ahhhhhh! Sweet relief!");
    System.out.println("\n" + wife.getMood());
    wife.getFsm().revertToPreviousState();
  }

  @Override
  public void exit(Wife wife) {
    wife.setMood(wife.getName() + ": Leavin' the Jon");
    System.out.println("\n" + wife.getMood());
  }

  @Override
  public boolean onMessage(Wife wife, Telegram msg) {
    return false;
  }
}
