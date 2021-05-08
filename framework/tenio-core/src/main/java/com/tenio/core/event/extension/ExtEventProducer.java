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
package com.tenio.core.event.extension;

import javax.annotation.concurrent.NotThreadSafe;

import com.tenio.core.configuration.define.ExtensionEvent;

/**
 * Only for creating an event handler object, see {@link ExtEventHandler}
 * 
 * @author kong
 * 
 */
@NotThreadSafe
public final class ExtEventProducer {

	/**
	 * @see ExtEventHandler
	 */
	private final ExtEventHandler<Object> __eventHandler;

	public ExtEventProducer() {
		__eventHandler = new ExtEventHandler<Object>();
	}

	/**
	 * Retrieves an event handler
	 * 
	 * @return see {@link ExtEventHandler}
	 */
	public ExtEventHandler<Object> getEventHandler() {
		return __eventHandler;
	}

	/**
	 * Emit an event with its parameters.
	 * 
	 * @param event see {@link ExtensionEvent}
	 * @param params a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see ExtEventHandler#emit(ExtensionEvent, Object...)
	 */
	public Object emit(ExtensionEvent event, Object... params) {
		return __eventHandler.emit(event, params);
	}

	/**
	 * Clear all events and these handlers.
	 * 
	 * @see ExtEventHandler#clear()
	 */
	public void clear() {
		__eventHandler.clear();
	}

}
