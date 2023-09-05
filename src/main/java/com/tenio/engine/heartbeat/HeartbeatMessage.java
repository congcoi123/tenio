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

package com.tenio.engine.heartbeat;

import com.tenio.common.utility.TimeUtility;
import com.tenio.engine.message.ExtraMessage;
import java.util.UUID;

/**
 * The message which is used for communication between one heart-beat and
 * outside.
 */
@SuppressWarnings("rawtypes")
final class HeartbeatMessage implements Comparable {

  /**
   * These messages will be stored in a priority queue. Therefore the operator
   * needs to be overloaded so that the PQ can sort the messages by time priority.
   * Note how the times must be smaller than SmallestDelay apart before two
   * messages are considered unique.
   */
  public static final double SMALLEST_DELAY = 0.25f;

  /**
   * The unique id of message.
   */
  private final String id;
  /**
   * The main information.
   */
  private final ExtraMessage message;
  /**
   * The message will be sent after an interval time.
   */
  private double delayTime;

  private HeartbeatMessage(ExtraMessage message, double delayTime) {
    id = UUID.randomUUID().toString();
    setDelayTime(delayTime);
    this.message = message;
  }

  public static HeartbeatMessage newInstance(ExtraMessage message, double delayTime) {
    return new HeartbeatMessage(message, delayTime);
  }

  /**
   * Retrieves the delay time.
   *
   * @return the delay time
   */
  public double getDelayTime() {
    return delayTime;
  }

  /**
   * Set the delay time.
   *
   * @param delayTime the delay time in seconds
   */
  private void setDelayTime(double delayTime) {
    this.delayTime = TimeUtility.currentTimeSeconds() + delayTime;
  }

  /**
   * Retrieves the message id.
   *
   * @return the message id
   */
  public String getId() {
    return id;
  }

  /**
   * Retrieves the message.
   *
   * @return see {@link ExtraMessage}
   */
  public ExtraMessage getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof HeartbeatMessage)) {
      return false;
    }
    var t1 = this;
    var t2 = (HeartbeatMessage) o;
    return (Math.abs(t1.getDelayTime() - t2.getDelayTime()) < SMALLEST_DELAY);
  }

  /**
   * It is generally necessary to override the <b>hashCode</b> method whenever
   * equals method is overridden, so as to maintain the general contract for the
   * hashCode method, which states that equal objects must have equal hash codes.
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + id.hashCode();
    return hash;
  }

  @Override
  public int compareTo(Object o2) {
    var t1 = this;
    var t2 = (HeartbeatMessage) o2;
    if (t1 == t2) {
      return 0;
    } else {
      return (t1.getDelayTime() > t2.getDelayTime()) ? -1 : 1;
    }
  }

  @Override
  public String toString() {
    return "Id: " +
        id +
        ", Time: " +
        delayTime +
        ", Message: " +
        message;
  }
}
