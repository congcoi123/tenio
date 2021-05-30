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
package com.tenio.engine.fsm.entity;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.engine.message.EMessage;

/**
 * This object is used for communication between entities.
 */
@SuppressWarnings("rawtypes")
public class Telegram implements Comparable {

	/**
	 * These telegrams will be stored in a priority queue. Therefore the operator
	 * needs to be overloaded so that the PQ can sort the telegrams by time
	 * priority. Note how the times must be smaller than SmallestDelay apart before
	 * two Telegrams are considered unique.
	 */
	public final static double SMALLEST_DELAY = 0.25f;

	/**
	 * The id of the sender
	 */
	private String __sender;
	/**
	 * The id of the receiver
	 */
	private String __receiver;
	/**
	 * The type of this message
	 */
	private int __type;
	/**
	 * The creation time
	 */
	private double __createdTime;
	/**
	 * The message will be sent after an interval time
	 */
	private double __delayTime;
	/**
	 * The extra information
	 */
	private EMessage __info;

	public Telegram() {
		__createdTime = TimeUtility.currentTimeSeconds();
		__delayTime = -1;
		__sender = null;
		__receiver = null;
		__type = -1;
	}

	public Telegram(double delayTime, String sender, String receiver, int type) {
		this(delayTime, sender, receiver, type, null);
	}

	public Telegram(double delayTime, String sender, String receiver, int type, EMessage info) {
		__delayTime = delayTime;
		__sender = sender;
		__receiver = receiver;
		__type = type;
		__info = info;
	}

	public String getSender() {
		return __sender;
	}

	public String getReceiver() {
		return __receiver;
	}

	public int getType() {
		return __type;
	}

	public double getDelayTime() {
		return __delayTime;
	}

	public void setDelayTime(double delay) {
		__delayTime = TimeUtility.currentTimeSeconds() + delay;
	}

	public double getCreatedTime() {
		return __createdTime;
	}

	public EMessage getInfo() {
		return __info;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Telegram)) {
			return false;
		}
		var t1 = this;
		var t2 = (Telegram) o;
		return (Math.abs(t1.getDelayTime() - t2.getDelayTime()) < SMALLEST_DELAY) && (t1.getSender() == t2.getSender())
				&& (t1.getReceiver() == t2.getReceiver()) && (t1.getType() == t2.getType());
	}

	/**
	 * It is generally necessary to override the <b>hashCode</b> method whenever
	 * equals method is overridden, so as to maintain the general contract for the
	 * hashCode method, which states that equal objects must have equal hash codes.
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + __sender.hashCode();
		hash = 89 * hash + __receiver.hashCode();
		hash = 89 * hash + __type;
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
		var builder = new StringBuilder();
		builder.append("Time: ");
		builder.append(__delayTime);
		builder.append(", Sender: ");
		builder.append(__sender);
		builder.append(", Receiver: ");
		builder.append(__receiver);
		builder.append(", MsgType: ");
		builder.append(__type);
		builder.append(", Info: ");
		builder.append(__info.toString());
		return builder.toString();
	}

}
