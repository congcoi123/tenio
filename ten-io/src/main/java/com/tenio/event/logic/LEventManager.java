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
package com.tenio.event.logic;

import java.util.ArrayList;
import java.util.List;

import com.tenio.configuration.constant.LogicEvent;
import com.tenio.event.ISubscriber;
import com.tenio.logger.AbstractLogger;

/**
 * This class for managing events and these subscribers.
 * 
 * @author kong
 * 
 */
public final class LEventManager extends AbstractLogger {

	/**
	 * A list of subscribers
	 */
	private final List<LSubscriber> __subscribers = new ArrayList<LSubscriber>();
	/**
	 * @see LEventProducer
	 */
	private final LEventProducer __producer = new LEventProducer();

	/**
	 * @see LEventProducer#emit(LogicEvent, Object...)
	 */
	public Object emit(final LogicEvent type, final Object... args) {
		return __producer.emit(type, args);
	}

	/**
	 * Add a subscriber's handler.
	 * 
	 * @param type @see {@link LogicEvent}
	 * @param sub  @see {@link ISubscriber}
	 */
	public void on(final LogicEvent type, final ISubscriber sub) {
		if (hasSubscriber(type)) {
			info("EVENT WARNING", "Duplicated", type);
		}

		__subscribers.add(LSubscriber.newInstance(type, sub));
	}

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe() {
		__producer.clear(); // clear the old first

		// only for log recording
		var subs = new ArrayList<LogicEvent>();
		// start handling
		__subscribers.forEach(s -> {
			subs.add(s.getType());
			__producer.getEventHandler().subscribe(s.getType(), s.getSub()::dispatch);
		});
		info("LOGIC EVENT UPDATED", "Subscribers", subs.toString());
	}

	/**
	 * Check if an event has any subscribers or not.
	 * 
	 * @param type @see {@link LogicEvent}
	 * @return Returns <code>true</code> if an event has any subscribers
	 */
	public boolean hasSubscriber(final LogicEvent type) {
		for (var subscriber : __subscribers) {
			if (subscriber.getType() == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clear all subscribers and these corresponding events.
	 */
	public void clear() {
		__subscribers.clear();
		__producer.clear();
	}

}
