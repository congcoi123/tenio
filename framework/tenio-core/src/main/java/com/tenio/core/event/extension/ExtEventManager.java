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
package com.tenio.core.event.extension;

import java.util.ArrayList;
import java.util.List;

import com.tenio.common.logger.SystemAbstractLogger;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.event.ISubscriber;

/**
 * This class for managing events and these subscribers.
 * 
 * @author kong
 * 
 */
public final class ExtEventManager extends SystemAbstractLogger {

	/**
	 * A list of subscribers.
	 */
	private final List<ExtSubscriber> __subscribers = new ArrayList<ExtSubscriber>();
	/**
	 * @see ExtEventProducer
	 */
	private final ExtEventProducer __producer = new ExtEventProducer();

	/**
	 * Emit an event with its parameters.
	 * 
	 * @param type see {@link ExtEvent}
	 * @param args a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see ExtEventProducer#emit(ExtEvent, Object...)
	 */
	public Object emit(final ExtEvent type, final Object... args) {
		if (__canShowTraceLog(type)) {
			_trace(type.name(), args);
		} else {
			_debug(type.name(), args);
		}
		return __producer.emit(type, args);
	}

	/**
	 * Add a subscriber's handler.
	 * 
	 * @param type see {@link ExtEvent}
	 * @param sub  see {@link ISubscriber}
	 */
	public void on(final ExtEvent type, final ISubscriber sub) {
		if (hasSubscriber(type)) {
			_info("EXTERNAL EVENT WARNING", "Duplicated", type);
		}

		__subscribers.add(ExtSubscriber.newInstance(type, sub));
	}

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe() {
		__producer.clear(); // clear the old first

		// only for log recording
		var subs = new ArrayList<ExtEvent>();
		// start handling
		__subscribers.forEach(s -> {
			subs.add(s.getType());
			__producer.getEventHandler().subscribe(s.getType(), s.getSub()::dispatch);
		});
		_info("EXTERNAL EVENT UPDATED", "Subscribers", subs.toString());
	}

	/**
	 * Check if an event has any subscribers or not.
	 * 
	 * @param type see {@link ExtEvent}
	 * @return <b>true</b> if an event has any subscribers
	 */
	public boolean hasSubscriber(final ExtEvent type) {
		return __subscribers.stream().anyMatch(subscribe -> subscribe.isType(type));
	}

	/**
	 * Clear all subscribers and these corresponding events.
	 */
	public void clear() {
		__subscribers.clear();
		__producer.clear();
	}
	
	/**
	 * Special events will be traced by trace log
	 * @param type the event's type
	 * @return <b>true</b> if an event can be traced
	 */
	private boolean __canShowTraceLog(final ExtEvent type) {
		switch (type) {
		case RECEIVED_MESSAGE_FROM_CONNECTION:
		case RECEIVED_MESSAGE_FROM_PLAYER:
		case HTTP_REQUEST_VALIDATE:
		case HTTP_REQUEST_HANDLE:
			return true;
		default:
			return false;
		}
	}

}
