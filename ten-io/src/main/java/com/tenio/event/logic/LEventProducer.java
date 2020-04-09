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

import com.tenio.configuration.constant.LogicEvent;

/**
 * Only for creating an event handler object, see {@link LEventHandler}
 * 
 * @author kong
 * 
 */
public final class LEventProducer {

	/**
	 * @see LEventHandler
	 */
	private final LEventHandler<Object> __eventHandler = new LEventHandler<Object>();

	/**
	 * Retrieves an event handler
	 * 
	 * @return see {@link LEventHandler}
	 */
	public LEventHandler<Object> getEventHandler() {
		return __eventHandler;
	}

	/**
	 * Emit an event with its parameters
	 * 
	 * @param type see {@link LogicEvent}
	 * @param args a list parameters of this event
	 * @return the event result (the response of its subscribers), see
	 *         {@link Object} or <b>null</b>
	 * @see LEventHandler#emit(LogicEvent, Object...)
	 */
	public Object emit(final LogicEvent type, final Object... args) {
		return __eventHandler.emit(type, args);
	}

	/**
	 * Clear all events and these handlers
	 * 
	 * @see LEventHandler#clear()
	 */
	public void clear() {
		__eventHandler.clear();
	}

}
