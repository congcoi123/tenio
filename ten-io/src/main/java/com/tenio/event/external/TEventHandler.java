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
package com.tenio.event.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tenio.configuration.constant.TEvent;
import com.tenio.event.IEvent;

/**
 * This class for handling events and these subscribers.
 * 
 * @param <T> the template
 * 
 * @author kong
 * 
 */
public final class TEventHandler<T> {

	/**
	 * An instance creates a mapping between an event with its list of event
	 * handlers.
	 */
	private final Map<TEvent, List<IEvent<T>>> __delegate = new HashMap<TEvent, List<IEvent<T>>>();

	/**
	 * Create a link between an event and its list of event handlers.
	 * 
	 * @param type  see {@link TEvent}
	 * @param event see {@link IEvent}
	 */
	public void subscribe(final TEvent type, final IEvent<T> event) {
		if (__delegate.containsKey(type)) {
			__delegate.get(type).add(event);
		} else {
			// create a new array of events
			var events = new ArrayList<IEvent<T>>();
			// add the first event
			events.add(event);
			__delegate.put(type, events);
		}
	}

	/**
	 * Emit an event with its parameters.
	 * 
	 * @param type see {@link TEvent}
	 * @param args a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 */
	public Object emit(final TEvent type, final @SuppressWarnings("unchecked") T... args) {
		if (!__delegate.isEmpty()) {
			Object obj = null;
			if (__delegate.containsKey(type)) {
				for (IEvent<T> event : __delegate.get(type)) {
					obj = event.emit(args);
				}
			}
			// return the last event's result
			return obj;
		}
		return null;
	}

	/**
	 * Clear all events and these handlers.
	 */
	public void clear() {
		if (!__delegate.isEmpty()) {
			__delegate.clear();
		}
	}

}
