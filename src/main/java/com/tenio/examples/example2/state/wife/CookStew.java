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

import com.tenio.engine.constant.EngineConstant;
import com.tenio.engine.fsm.entity.State;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.examples.example2.define.EntityName;
import com.tenio.examples.example2.define.MessageType;
import com.tenio.examples.example2.entity.Wife;

/**
 * The miner's wife start cooking.
 */
public final class CookStew extends State<Wife> {

  private static volatile CookStew instance;

  private CookStew() {
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static CookStew getInstance() {
    if (instance == null) {
      instance = new CookStew();
    }
    return instance;
  }

  @Override
  public void enter(Wife wife) {
    // if not already cooking put the stew in the oven
    if (!wife.isCooking()) {
      System.out.println("\n" + wife.getName() + ": Putting the stew in the oven");

      // send a delayed message myself so that I know when to take the stew
      // out of the oven
      wife.getFsm().getDispatcher().dispatchMessage(1.5, // time delay
          wife.getId(), // sender ID
          wife.getId(), // receiver ID
          MessageType.STEW_READY.get(), // msg
          null);

      wife.setCooking(true);
    }
  }

  @Override
  public void execute(Wife wife) {
    wife.setMood(wife.getName() + ": Fussin' over food");
    System.out.print("\n" + wife.getMood());
  }

  @Override
  public void exit(Wife wife) {
    wife.setMood(wife.getName() + ": Puttin' the stew on the table");
    System.out.print("\n" + wife.getMood());
  }

  @Override
  public boolean onMessage(Wife wife, Telegram msg) {

    if (msg.getType() == MessageType.STEW_READY.get()) {
      System.out.print(
          "\nMessage received by " + wife.getName() + " at time: " + System.currentTimeMillis());

      wife.setMood(wife.getName() + ": StewReady! Lets eat");
      System.out.print("\n" + wife.getMood());

      // let hubby know the stew is ready
      wife.getFsm().getDispatcher()
          .dispatchMessage(EngineConstant.SEND_MSG_IMMEDIATELY, wife.getId(),
              EntityName.MINER.get(), MessageType.STEW_READY.get(), null);

      wife.setCooking(false);

      wife.getFsm().changeState(DoHouseWork.getInstance());

      return true;
    }
    return false;
  }
}
