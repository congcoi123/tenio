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
package com.tenio.core.manager;

import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.events.EventManager;
import com.tenio.core.events.extension.ExtEventManager;
import com.tenio.core.events.internal.InternalEventManager;

/**
 * @author kong
 */
public abstract class AbstractManager extends SystemLogger {

	private EventManager __eventManager;

	public AbstractManager(EventManager eventManager) {
		__eventManager = eventManager;
	}

	protected InternalEventManager __getInternalEvent() {
		return __eventManager.getInternal();
	}

	protected ExtEventManager __getExtensionEvent() {
		return __eventManager.getExtension();
	}

}
