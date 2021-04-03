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

import javax.annotation.concurrent.NotThreadSafe;

import com.tenio.common.logger.SystemAbstractLogger;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.event.ISubscriber;

/**
 * This class for managing events and these subscribers.
 * 
 * @author kong
 * 
 */
@NotThreadSafe
public final class ExtEventManager extends SystemAbstractLogger {

	/**
	 * A list of event and subscribers.
	 */
	private final List<ExtEventSubscriber> __eventSubscribers;
	/**
	 * @see ExtEventProducer
	 */
	private final ExtEventProducer __producer;

	public ExtEventManager() {
		__eventSubscribers = new ArrayList<ExtEventSubscriber>();
		__producer = new ExtEventProducer();
	}

	/**
	 * Emit an event with its parameters.
	 * 
	 * @param event  see {@link ExtEvent}
	 * @param params a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see ExtEventProducer#emit(ExtEvent, Object...)
	 */
	public Object emit(final ExtEvent event, final Object... params) {
		if (__isOnlyShownInTracedLog(event)) {
			_trace(event.name(), params);
		} else {
			_debug(event.name(), params);
		}
		return __producer.emit(event, params);
	}

	/**
	 * Add a subscriber's handler.
	 * 
	 * @param event      see {@link ExtEvent}
	 * @param subscriber see {@link ISubscriber}
	 */
	public void on(final ExtEvent event, final ISubscriber subscriber) {
		if (hasSubscriber(event)) {
			_info("EXTERNAL EVENT WARNING", "Duplicated", event);
		}

		__eventSubscribers.add(ExtEventSubscriber.newInstance(event, subscriber));
	}

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe() {
		// clear the old first
		__producer.clear();

		// only for log recording
		var events = new ArrayList<ExtEvent>();
		// start handling
		__eventSubscribers.forEach(eventSubscriber -> {
			events.add(eventSubscriber.getEvent());
			__producer.getEventHandler().subscribe(eventSubscriber.getEvent(),
					eventSubscriber.getSubscriber()::dispatch);
		});
		_info("EXTERNAL EVENT UPDATED", "Subscribers", events.toString());
	}

	/**
	 * Check if an event has any subscribers or not.
	 * 
	 * @param event see {@link ExtEvent}
	 * @return <b>true</b> if an event has any subscribers
	 */
	public boolean hasSubscriber(final ExtEvent event) {
		return __eventSubscribers.stream().anyMatch(eventSubscriber -> eventSubscriber.hasEvent(event));
	}

	/**
	 * Clear all subscribers and these corresponding events.
	 */
	public void clear() {
		__eventSubscribers.clear();
		__producer.clear();
	}

	/**
	 * Special events will be traced by trace log
	 * 
	 * @param event the event's type
	 * @return <b>true</b> if an event can be traced
	 */
	private boolean __isOnlyShownInTracedLog(final ExtEvent event) {
		switch (event) {
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
