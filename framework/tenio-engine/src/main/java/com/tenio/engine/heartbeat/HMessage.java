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
package com.tenio.engine.heartbeat;

import java.util.UUID;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.engine.message.EMessage;

/**
 * The message which is used for communication between one heart-beat and
 * outside
 */
@SuppressWarnings("rawtypes")
final class HMessage implements Comparable {

	/**
	 * These messages will be stored in a priority queue. Therefore the operator
	 * needs to be overloaded so that the PQ can sort the messages by time priority.
	 * Note how the times must be smaller than SmallestDelay apart before two
	 * messages are considered unique.
	 */
	public final static double SMALLEST_DELAY = 0.25f;

	/**
	 * The unique id of message
	 */
	private String __id;
	/**
	 * The message will be sent after an interval time
	 */
	private double __delayTime;
	/**
	 * The main information
	 */
	private EMessage __message;

	public static HMessage newInstance(EMessage message, double delayTime) {
		return new HMessage(message, delayTime);
	}

	private HMessage(EMessage message, double delayTime) {
		__id = UUID.randomUUID().toString();
		__setDelayTime(delayTime);
		__message = message;
	}

	/**
	 * Retrieves the delay time
	 * 
	 * @return the delay time
	 */
	public double getDelayTime() {
		return __delayTime;
	}

	/**
	 * Set the delay time
	 * 
	 * @param delayTime the delay time in seconds
	 */
	private void __setDelayTime(double delayTime) {
		__delayTime = TimeUtility.currentTimeSeconds() + delayTime;
	}

	/**
	 * Retrieves the message id
	 * 
	 * @return the message id
	 */
	public String getId() {
		return __id;
	}

	/**
	 * Retrieves the message
	 * 
	 * @return see {@link EMessage}
	 */
	public EMessage getMessage() {
		return __message;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HMessage)) {
			return false;
		}
		var t1 = this;
		var t2 = (HMessage) o;
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
		hash = 89 * hash + __id.hashCode();
		return hash;
	}

	@Override
	public int compareTo(Object o2) {
		var t1 = this;
		var t2 = (HMessage) o2;
		if (t1 == t2) {
			return 0;
		} else {
			return (t1.getDelayTime() > t2.getDelayTime()) ? -1 : 1;
		}
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder.append("Id: ");
		builder.append(__id);
		builder.append(", Time: ");
		builder.append(__delayTime);
		builder.append(", Message: ");
		builder.append(__message);
		return builder.toString();
	}

}
