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
package com.tenio.core.event.implement;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.event.Subscriber;

/**
 * This class for managing events and these subscribers.
 */
@NotThreadSafe
public final class EventManager extends SystemLogger {

	/**
	 * A list of subscribers
	 */
	private final List<EventSubscriber> __eventSubscribers;
	/**
	 * @see EventProducer
	 */
	private final EventProducer __producer;

	public static EventManager newInstance() {
		return new EventManager();
	}

	private EventManager() {
		__eventSubscribers = new ArrayList<EventSubscriber>();
		__producer = new EventProducer();
	}

	/**
	 * Emit an event with its parameters
	 * 
	 * @param event  see {@link ServerEvent}
	 * @param params a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see EventProducer#emit(ServerEvent, Object...)
	 */
	public Object emit(ServerEvent event, Object... params) {
		if (__isEventForTracing(event)) {
			trace(event.toString(), params);
		} else {
			debug(event.toString(), params);
		}
		return __producer.emit(event, params);
	}

	/**
	 * Add a subscriber's handler.
	 * 
	 * @param event      see {@link ServerEvent}
	 * @param subscriber see {@link Subscriber}
	 */
	public void on(ServerEvent event, Subscriber subscriber) {
		if (hasSubscriber(event)) {
			info("SERVER EVENT WARNING", "Duplicated", event);
		}

		__eventSubscribers.add(EventSubscriber.newInstance(event, subscriber));
	}

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe() {
		// clear the old first
		__producer.clear();

		// only for log recording
		var events = new ArrayList<ServerEvent>();
		// start handling
		__eventSubscribers.forEach(eventSubscriber -> {
			events.add(eventSubscriber.getEvent());
			__producer.getEventHandler().subscribe(eventSubscriber.getEvent(),
					eventSubscriber.getSubscriber()::dispatch);
		});
		info("SERVER EVENT SUBSCRIBERS", "Subscribers", events.toString());
	}

	/**
	 * Check if an event has any subscribers or not.
	 * 
	 * @param event see {@link ServerEvent}
	 * @return <b>true</b> if an event has any subscribers
	 */
	public boolean hasSubscriber(ServerEvent event) {
		for (var subscriber : __eventSubscribers) {
			if (subscriber.getEvent() == event) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clear all subscribers and these corresponding events.
	 */
	public void clear() {
		__eventSubscribers.clear();
		__producer.clear();
	}

	private boolean __isEventForTracing(ServerEvent event) {

		switch (event) {
		case HTTP_REQUEST_HANDLE:
		case HTTP_REQUEST_VALIDATION:
		case DATAGRAM_CHANNEL_READ_MESSAGE:
		case RECEIVED_MESSAGE_FROM_PLAYER:
		case SESSION_READ_MESSAGE:
		case SEND_MESSAGE_TO_PLAYER:
			return true;
		default:
			return false;
		}

	}

}
