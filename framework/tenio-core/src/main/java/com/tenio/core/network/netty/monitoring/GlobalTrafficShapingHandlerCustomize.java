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
package com.tenio.core.network.netty.monitoring;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.event.EventManager;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

/**
 * This implementation of the {@link GlobalTrafficShapingHandler} is for global
 * traffic shaping, that is to say a global limitation of the bandwidth,
 * whatever the number of opened channels.
 * 
 * @author kong
 *
 */
@ThreadSafe
public final class GlobalTrafficShapingHandlerCustomize extends GlobalTrafficShapingHandler {

	private final String __name;
	private final EventManager __eventManager;

	public GlobalTrafficShapingHandlerCustomize(String name, EventManager eventManager,
			ScheduledExecutorService executor, long writeLimit, long readLimit, long checkInterval) {
		super(executor, writeLimit, readLimit, checkInterval);
		__name = name;
		__eventManager = eventManager;
	}

	@Override
	protected void doAccounting(TrafficCounter counter) {
		super.doAccounting(counter);

		long lastReadThroughput = counter.lastReadThroughput() >> 10;
		long lastWriteThroughput = counter.lastWriteThroughput() >> 10;
		long realWriteThroughput = counter.getRealWriteThroughput() >> 10;
		long currentReadBytes = counter.currentReadBytes() >> 10;
		long currentWrittenBytes = counter.currentWrittenBytes() >> 10;
		long realWrittenBytes = counter.getRealWrittenBytes().get() >> 10;

		__eventManager.getExtension().emit(ExtensionEvent.FETCHED_BANDWIDTH_INFO, lastReadThroughput, lastWriteThroughput,
				realWriteThroughput, currentReadBytes, currentWrittenBytes, realWrittenBytes, __name);

	}

}
