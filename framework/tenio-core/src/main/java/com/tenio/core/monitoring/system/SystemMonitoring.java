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
package com.tenio.core.monitoring.system;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * For showing the system information
 * 
 * <h1>CPU</h1>
 * <ul>
 * <li>Usage</li>
 * </ul>
 * <h1>Memory</h1>
 * <ul>
 * <li>Total</li>
 * <li>Used</li>
 * <li>Free</li>
 * </ul>
 * 
 * @see <a href="https://i.stack.imgur.com/GjuwM.png">Explained Image</a>
 * @author kong
 *
 */
public final class SystemMonitoring {

	private final OperatingSystemMXBean __osMxBean;
	private long __lastSystemTime;
	private long __lastProcessCpuTime;

	public SystemMonitoring() {
		__osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		__lastSystemTime = 0L;
		__lastProcessCpuTime = 0L;
	}

	public synchronized double getCpuUsage() {
		if (__lastSystemTime == 0L) {
			__updateBaselineCounters();
			return 0.0D;
		} else {
			long systemTime = System.nanoTime();
			long processCpuTime = __osMxBean.getProcessCpuTime();
			double cpuUsage = (double) (processCpuTime - __lastProcessCpuTime)
					/ (double) (systemTime - __lastSystemTime);
			__lastSystemTime = systemTime;
			__lastProcessCpuTime = processCpuTime;

			return cpuUsage / (double) __osMxBean.getAvailableProcessors();
		}
	}

	private void __updateBaselineCounters() {
		__lastSystemTime = System.nanoTime();
		__lastProcessCpuTime = __osMxBean.getProcessCpuTime();
	}

	public final long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	public final long getFreeMemory() {
		return Runtime.getRuntime().maxMemory() - getUsedMemory();
	}

	public final long getUsedMemory() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

}