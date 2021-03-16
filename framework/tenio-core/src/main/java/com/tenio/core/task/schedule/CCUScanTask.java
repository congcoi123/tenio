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
package com.tenio.core.task.schedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.schedule.ITask;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.event.IEventManager;

/**
 * To retrieve the CCU in period time. You can configure this time in your own
 * configurations, see {@link CoreConfiguration}
 * 
 * @author kong
 * 
 */
public final class CCUScanTask extends AbstractLogger implements ITask {

	private final IEventManager __eventManager;
	/**
	 * The period time for retrieving CCU
	 */
	private final int __ccuScanPeriod;
	private final PlayerApi __playerApi;

	public CCUScanTask(IEventManager eventManager, PlayerApi playerApi, int ccuScanPeriod) {
		__eventManager = eventManager;
		__playerApi = playerApi;
		__ccuScanPeriod = ccuScanPeriod;
	}

	@Override
	public ScheduledFuture<?> run() {
		_info("CCU SCAN TASK", "Running ...");
		return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			__eventManager.getExtension().emit(ExtEvent.FETCHED_CCU_INFO, __playerApi.countPlayers(), __playerApi.count());
		}, 0, __ccuScanPeriod, TimeUnit.SECONDS);
	}

}
