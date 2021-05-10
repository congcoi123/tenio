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
package com.tenio.core.network.zero.handler.implement;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.handler.BaseIOHandler;

/**
 * @author kong
 */
// TODO: Add description
public abstract class BaseZeroHandler extends SystemLogger implements BaseIOHandler {

	private InternalEventManager __internalEventManager;
	private SessionManager __sessionManager;
	private NetworkReaderStatistic __networkReaderStatistic;

	@Override
	public InternalEventManager getInternalEventManager() {
		return __internalEventManager;
	}

	@Override
	public void setInternalEventManager(InternalEventManager internalEventManager) {
		__internalEventManager = internalEventManager;
	}

	@Override
	public SessionManager getSessionManager() {
		return __sessionManager;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		__sessionManager = sessionManager;
	}

	@Override
	public NetworkReaderStatistic getNetworkReaderStatistic() {
		return __networkReaderStatistic;
	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
		__networkReaderStatistic = networkReaderStatistic;
	}

}
