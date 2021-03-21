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
package com.tenio.core.event;

import com.tenio.core.event.extension.ExtEventManager;
import com.tenio.core.event.internal.InternalEventManager;

/**
 * Manage all events in the server
 * 
 * @see IEventManager
 * 
 * @author kong
 *
 */
public final class EventManager implements IEventManager {

	/**
	 * @see ExtEventManager
	 */
	private ExtEventManager __extEventManager = new ExtEventManager();
	/**
	 * @see InternalEventManager
	 */
	private InternalEventManager __internalEventManager = new InternalEventManager();

	@Override
	public ExtEventManager getExtension() {
		return __extEventManager;
	}

	@Override
	public InternalEventManager getInternal() {
		return __internalEventManager;
	}

	@Override
	public void subscribe() {
		__extEventManager.subscribe();
		__internalEventManager.subscribe();
	}

	@Override
	public void clear() {
		__extEventManager.clear();
		__internalEventManager.clear();
	}

}
