/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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

import com.tenio.configuration.constant.TEvent;

/**
 * Only for creating an event handler object @see {@link TEventHandler}
 * 
 * @author kong
 * 
 */
public final class TEventProducer {

	/**
	 * @see TEventHandler
	 */
	private TEventHandler<Object> __eventHandler = new TEventHandler<Object>();

	/**
	 * @return Returns @see {@link TEventHandler}
	 */
	public TEventHandler<Object> getEventHandler() {
		return __eventHandler;
	}

	/**
	 * @see TEventHandler#emit(Object, TEvent, Object...)
	 */
	public Object emit(final TEvent type, final Object... args) {
		return __eventHandler.emit(type, args);
	}

	/**
	 * @see TEventHandler#clear()
	 */
	public void clear() {
		__eventHandler.clear();
	}

}
