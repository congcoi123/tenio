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
package com.tenio.event.logic;

import java.util.HashMap;
import java.util.Map;

import com.tenio.configuration.constant.LogicEvent;
import com.tenio.event.IEvent;

/**
 * 
 * @author kong
 * 
 */
public final class LEventHandler<T> {

	/**
	 * An instance creates a mapping between an event with its list of event
	 * handlers.
	 */
	private final Map<LogicEvent, IEvent<T>> __delegate = new HashMap<LogicEvent, IEvent<T>>();

	/**
	 * Create a link between an event and its list of event handlers.
	 * 
	 * @param type  @see {@link LogicEvent}
	 * @param event @see {@link IEvent}
	 */
	public void subscribe(final LogicEvent type, final IEvent<T> event) {
		__delegate.put(type, event);
	}

	/**
	 * Emit an event with its parameters
	 * 
	 * @param type @see {@link LogicEvent}
	 * @param args a list parameters of this event
	 * @return Returns the event result (the response of its subscribers) @see
	 *         {@link Object} or <code>null</code>
	 */
	public Object emit(final LogicEvent type, final @SuppressWarnings("unchecked") T... args) {
		if (__delegate.containsKey(type)) {
			return __delegate.get(type).emit(args);
		}
		return null;
	}

	/**
	 * Clear all events and these handlers
	 */
	public void clear() {
		if (!__delegate.isEmpty()) {
			__delegate.clear();
		}
	}

}
