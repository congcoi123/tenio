/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.engine.fsm.entity;

import com.tenio.common.utility.TimeUtility;
import com.tenio.engine.message.ExtraMessage;
import java.util.Objects;

/**
 * This object is used for communication between entities.
 */
@SuppressWarnings("rawtypes")
public class Telegram implements Comparable {

  /**
   * These telegrams will be stored in a priority queue. Therefore, the operator
   * needs to be overloaded so that the PQ can sort the telegrams by time
   * priority. Note how the times must be smaller than SmallestDelay apart before
   * two Telegrams are considered unique.
   */
  public static final double SMALLEST_DELAY = 0.25f;

  /**
   * The id of the sender.
   */
  private final String sender;
  /**
   * The id of the receiver.
   */
  private final String receiver;
  /**
   * The type of this message.
   */
  private final int type;
  /**
   * The creation time.
   */
  private double createdTime;
  /**
   * The message will be sent after an interval time.
   */
  private double delayTime;
  /**
   * The extra information.
   */
  private ExtraMessage info;

  /**
   * Initialization.
   */
  public Telegram() {
    createdTime = TimeUtility.currentTimeSeconds();
    delayTime = -1;
    sender = null;
    receiver = null;
    type = -1;
  }

  public Telegram(double delayTime, String sender, String receiver, int type) {
    this(delayTime, sender, receiver, type, null);
  }

  /**
   * Initialization.
   *
   * @param delayTime the delay time
   * @param sender    the sender
   * @param receiver  the receiver
   * @param type      the type
   * @param info      the information
   */
  public Telegram(double delayTime, String sender, String receiver, int type, ExtraMessage info) {
    this.delayTime = delayTime;
    this.sender = sender;
    this.receiver = receiver;
    this.type = type;
    this.info = info;
  }

  public String getSender() {
    return sender;
  }

  public String getReceiver() {
    return receiver;
  }

  public int getType() {
    return type;
  }

  public double getDelayTime() {
    return delayTime;
  }

  public void setDelayTime(double delay) {
    delayTime = TimeUtility.currentTimeSeconds() + delay;
  }

  public double getCreatedTime() {
    return createdTime;
  }

  public ExtraMessage getInfo() {
    return info;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Telegram)) {
      return false;
    }
    var t1 = this;
    var t2 = (Telegram) o;
    return (Math.abs(t1.getDelayTime() - t2.getDelayTime()) < SMALLEST_DELAY)
        && (Objects.equals(t1.getSender(), t2.getSender()))
        && (Objects.equals(t1.getReceiver(), t2.getReceiver())) && (t1.getType() == t2.getType());
  }

  /**
   * It is generally necessary to override the <b>hashCode</b> method whenever
   * equals method is overridden, to maintain the general contract for the
   * hashCode method, which states that equal objects must have equal hash codes.
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + sender.hashCode();
    hash = 89 * hash + receiver.hashCode();
    hash = 89 * hash + type;
    return hash;
  }

  @Override
  public int compareTo(Object o2) {
    var t1 = this;
    var t2 = (Telegram) o2;
    if (t1 == t2) {
      return 0;
    } else {
      return (t1.getDelayTime() > t2.getDelayTime()) ? -1 : 1;
    }
  }

  @Override
  public String toString() {
    return "Time: " +
        delayTime +
        ", Sender: " +
        sender +
        ", Receiver: " +
        receiver +
        ", MsgType: " +
        type +
        ", Info: " +
        info.toString();
  }
}
