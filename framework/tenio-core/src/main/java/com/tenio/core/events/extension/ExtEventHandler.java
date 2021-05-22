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
package com.tenio.core.events.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import com.tenio.core.configuration.defines.ExtensionEvent;
import com.tenio.core.events.Emitter;

/**
 * This class for handling events and these subscribers.
 * 
 * @param <T> the template
 * 
 * @author kong
 * 
 */
@NotThreadSafe
public final class ExtEventHandler<T> {

	/**
	 * An instance creates a mapping between an event with its list of event
	 * handlers.
	 */
	private final Map<ExtensionEvent, List<Emitter<T>>> __delegate;

	public ExtEventHandler() {
		__delegate = new HashMap<ExtensionEvent, List<Emitter<T>>>();
	}

	/**
	 * Create a link between an event and its list of event handlers.
	 * 
	 * @param event  see {@link ExtensionEvent}
	 * @param emitter see {@link Emitter}
	 */
	public void subscribe(ExtensionEvent event, Emitter<T> emitter) {
		if (__delegate.containsKey(event)) {
			__delegate.get(event).add(emitter);
		} else {
			// create a new array of event processes
			var emitters = new ArrayList<Emitter<T>>();
			// add the first event
			emitters.add(emitter);
			__delegate.put(event, emitters);
		}
	}

	/**
	 * Emit an event with its parameters.
	 * 
	 * @param event see {@link ExtensionEvent}
	 * @param params a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 */
	public Object emit(ExtensionEvent event, @SuppressWarnings("unchecked") T... params) {
		if (!__delegate.isEmpty()) {
			Object object = null;
			if (__delegate.containsKey(event)) {
				for (Emitter<T> emitter : __delegate.get(event)) {
					object = emitter.emit(params);
				}
			}
			// return the last event's result
			return object;
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