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
package com.tenio.core.task.schedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.schedule.ITask;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.event.IEventManager;
import com.tenio.core.monitoring.system.SystemMonitoring;

/**
 * To retrieve the current system information in period time. You can configure
 * this time in your own configurations, see {@link CoreConfiguration}
 * 
 * @author kong
 * 
 */
public final class SystemMonitoringTask extends AbstractLogger implements ITask {

	private final SystemMonitoring __monitoring;

	private final IEventManager __eventManager;
	/**
	 * The period time for retrieving system information
	 */
	private final int __monitoringPeriod;

	public SystemMonitoringTask(IEventManager eventManager, int ccuScanPeriod) {
		__eventManager = eventManager;
		__monitoringPeriod = ccuScanPeriod;

		__monitoring = new SystemMonitoring();
	}

	@Override
	public ScheduledFuture<?> run() {
		_info("SYSTEM MONITORING TASK", "Running ...");
		return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			__eventManager.getExtension().emit(ExtEvent.MONITORING_SYSTEM, __monitoring.getCpuUsage(),
					__monitoring.getTotalMemory(), __monitoring.getUsedMemory(), __monitoring.getFreeMemory());
		}, 0, __monitoringPeriod, TimeUnit.SECONDS);
	}

}
