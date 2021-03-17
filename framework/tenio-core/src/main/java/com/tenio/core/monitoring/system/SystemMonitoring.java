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
