/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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

import java.util.concurrent.atomic.AtomicInteger;

import com.tenio.entities.element.TObject;
import com.tenio.utils.TimeUtility;

/**
 * The message which is used for communication between one heart-beat and
 * outside
 * 
 * @author kong
 * 
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
	 * For creating a unique id value
	 */
	private static AtomicInteger __atomic = new AtomicInteger(0);
	/**
	 * The unique id of message
	 */
	private int __id;
	/**
	 * The message will be sent after an interval time
	 */
	private double __delayTime;
	/**
	 * The main information
	 */
	private TObject __message;
	
	public static HMessage newInstance(TObject message, double delayTime) {
		return new HMessage(message, delayTime);
	}

	private HMessage(TObject message, double delayTime) {
		__id = __atomic.incrementAndGet();
		__setDelayTime(delayTime);
		__message = message;
	}

	public double getDelayTime() {
		return __delayTime;
	}

	/**
	 * @param delayTime the delay time in seconds
	 */
	private void __setDelayTime(double delayTime) {
		__delayTime = TimeUtility.currentTimeSeconds() + delayTime;
	}

	public int getId() {
		return __id;
	}

	public TObject getMessage() {
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
	 * It is generally necessary to override the <code>hashCode</code> method
	 * whenever equals method is overridden, so as to maintain the general contract
	 * for the hashCode method, which states that equal objects must have equal hash
	 * codes.
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + __id;
		return hash;
	}

	/**
	 * "overloads" < and > operators
	 */
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
