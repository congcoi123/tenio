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
package com.tenio.event.main;

import java.util.ArrayList;
import java.util.List;

import com.tenio.configuration.constant.TEvent;
import com.tenio.event.ISubscriber;
import com.tenio.logger.AbstractLogger;

/**
 * This class for managing events and these subscribers.
 * 
 * @author kong
 * 
 */
public final class TEventManager extends AbstractLogger {

	/**
	 * A list of subscribers.
	 */
	private final List<TSubscriber> __subscribers = new ArrayList<TSubscriber>();
	/**
	 * @see TEventProducer
	 */
	private final TEventProducer __producer = new TEventProducer();

	/**
	 * Emit an event with its parameters.
	 * 
	 * @param type see {@link TEvent}
	 * @param args a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see TEventProducer#emit(TEvent, Object...)
	 */
	public Object emit(final TEvent type, final Object... args) {
		return __producer.emit(type, args);
	}

	/**
	 * Add a subscriber's handler.
	 * 
	 * @param type see {@link TEvent}
	 * @param sub  see {@link ISubscriber}
	 */
	public void on(final TEvent type, final ISubscriber sub) {
		if (hasSubscriber(type)) {
			info("EVENT WARNING", "Duplicated", type);
		}

		__subscribers.add(TSubscriber.newInstance(type, sub));
	}

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe() {
		__producer.clear(); // clear the old first

		// only for log recording
		var subs = new ArrayList<TEvent>();
		// start handling
		__subscribers.forEach(s -> {
			subs.add(s.getType());
			__producer.getEventHandler().subscribe(s.getType(), s.getSub()::dispatch);
		});
		info("EVENT UPDATED", "Subscribers", subs.toString());
	}

	/**
	 * Check if an event has any subscribers or not.
	 * 
	 * @param type see {@link TEvent}
	 * @return <b>true</b> if an event has any subscribers
	 */
	public boolean hasSubscriber(final TEvent type) {
		return __subscribers.stream().anyMatch(subscribe -> subscribe.isType(type));
	}

	/**
	 * Clear all subscribers and these corresponding events.
	 */
	public void clear() {
		__subscribers.clear();
		__producer.clear();
	}

}
