/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.core.event.internal;

import com.tenio.core.configuration.constant.LEvent;
import com.tenio.core.event.ISubscriber;

/**
 * An object which creates a mapping between an event type with a subscriber
 * 
 * @author kong
 * 
 */
public final class LSubscriber {

	/**
	 * @see LEvent
	 */
	private final LEvent __type;
	/**
	 * @see ISubscriber
	 */
	private final ISubscriber __sub;

	public static LSubscriber newInstance(final LEvent type, final ISubscriber sub) {
		return new LSubscriber(type, sub);
	}

	private LSubscriber(final LEvent type, final ISubscriber sub) {
		__type = type;
		__sub = sub;
	}

	/**
	 * @return see {@link LEvent}
	 */
	public LEvent getType() {
		return __type;
	}

	/**
	 * @return see {@link ISubscriber}
	 */
	public ISubscriber getSub() {
		return __sub;
	}

}
