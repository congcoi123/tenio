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

import java.util.ArrayList;
import java.util.List;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.event.ISubscriber;

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
	 * Emit an event with its parameters
	 * 
	 * @param type see {@link InternalEvent}
	 * @param args a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see LEventProducer#emit(InternalEvent, Object...)
	 */
	public Object emit(final InternalEvent type, final Object... args) {
		return __producer.emit(type, args);
	}

	/**
	 * Add a subscriber's handler.
	 * 
	 * @param type see {@link InternalEvent}
	 * @param sub  see {@link ISubscriber}
	 */
	public void on(final InternalEvent type, final ISubscriber sub) {
		if (hasSubscriber(type)) {
			info("INTERNAL EVENT WARNING", "Duplicated", type);
		}

		__subscribers.add(LSubscriber.newInstance(type, sub));
	}

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe() {
		__producer.clear(); // clear the old first

		// only for log recording
		var subs = new ArrayList<InternalEvent>();
		// start handling
		__subscribers.forEach(s -> {
			subs.add(s.getType());
			__producer.getEventHandler().subscribe(s.getType(), s.getSub()::dispatch);
		});
		info("INTERNAL EVENT UPDATED", "Subscribers", subs.toString());
	}

	/**
	 * Check if an event has any subscribers or not.
	 * 
	 * @param type see {@link InternalEvent}
	 * @return <b>true</b> if an event has any subscribers
	 */
	public boolean hasSubscriber(final InternalEvent type) {
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
