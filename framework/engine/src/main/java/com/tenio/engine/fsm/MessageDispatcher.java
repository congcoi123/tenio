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

package com.tenio.engine.fsm;

import com.tenio.common.utility.TimeUtility;
import com.tenio.engine.constant.EngineConstant;
import com.tenio.engine.fsm.entity.AbstractEntity;
import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.message.ExtraMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * This class is used for sending messages between entities.
 */
public final class MessageDispatcher {

  /**
   * A Set is used as the container for the delayed messages because of the
   * benefit of automatic sorting and avoidance of duplicates. Messages are sorted
   * by their dispatch time. See {@link Telegram}
   */
  private final TreeSet<Telegram> telegrams;

  private final EntityManager entityManager;

  private final List<MessageListener> messageListeners;

  /**
   * Initialization.
   *
   * @param manager the entity manager
   */
  public MessageDispatcher(EntityManager manager) {
    entityManager = manager;
    telegrams = new TreeSet<>();
    messageListeners = new ArrayList<>();
  }

  /**
   * Given a message, a receiver, a sender and any time delay , this function
   * routes the message to the correct agent (if no delay) or stores in the
   * message queue to be dispatched at the correct time.
   *
   * @param delay    the interval time for waiting the sending
   * @param sender   the sender's id
   * @param receiver the receiver's id
   * @param msgType  the message's type
   * @param info     the message extra information
   */
  public void dispatchMessage(double delay, String sender, String receiver, int msgType,
                              ExtraMessage info) {

    // get pointers to the receiver
    var preceiver = entityManager.get(receiver);

    // make sure the receiver is valid
    if (preceiver == null) {
      return;
    }

    // create the telegram
    var telegram = new Telegram(0, sender, receiver, msgType, info);

    // if there is no delay, route telegram immediately
    if (delay <= EngineConstant.SEND_MSG_IMMEDIATELY) {
      // send the telegram to the recipient
      discharge(preceiver, telegram);
    } else {
      // else calculate the time when the telegram should be dispatched
      telegram.setDelayTime(delay);

      // and put it in the queue
      telegrams.add(telegram);
    }
  }

  /**
   * This method is called each time through the main game loop.
   *
   * @param deltaTime the time between consecutive frames
   */
  public void update(float deltaTime) {

    // get current time
    double currentTime = TimeUtility.currentTimeSeconds();

    // now peek at the queue to see if any telegrams need dispatching.
    // remove all telegrams from the front of the queue that have gone
    // past their sell by date
    while (!telegrams.isEmpty() && (telegrams.last().getDelayTime() < currentTime)
        && (telegrams.last().getDelayTime() > 0)) {
      // read the telegram from the front of the queue

      var telegram = telegrams.last();

      // find the recipient
      var preceiver = entityManager.get(telegram.getReceiver());

      // send the telegram to the recipient
      discharge(preceiver, telegram);

      // remove it from the queue
      telegrams.remove(telegrams.last());
    }
  }

  /**
   * This method calls the message handling member function of the receiving
   * entity, with the newly created telegram.
   *
   * @param receiver the receiver, see {@link AbstractEntity}
   * @param message  the message content, see {@link Telegram}
   */
  private void discharge(AbstractEntity receiver, Telegram message) {
    if (!receiver.handleMessage(message)) {
      // Telegram could not be handled
      messageListeners.forEach(listener -> listener.onListen(message, false));
    } else {
      messageListeners.forEach(listener -> listener.onListen(message, true));
    }
  }

  public void listen(MessageListener listener) {
    messageListeners.add(listener);
  }

  public void clear() {
    messageListeners.clear();
    telegrams.clear();
  }
}
