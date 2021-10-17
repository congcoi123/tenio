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

package com.tenio.core.monitoring.system;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * For showing the system information.
 *
 * @see <a href="https://i.stack.imgur.com/GjuwM.png">Explained Image</a>
 */
@ThreadSafe
public final class SystemMonitoring {

  @GuardedBy("this")
  private final OperatingSystemMXBean operatingSystemMxBean;
  @GuardedBy("this")
  private long lastSystemTime;
  @GuardedBy("this")
  private long lastProcessCpuTime;

  private SystemMonitoring() {
    operatingSystemMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    lastSystemTime = 0L;
    lastProcessCpuTime = 0L;
  }

  public static SystemMonitoring newInstance() {
    return new SystemMonitoring();
  }

  /**
   * Retrieves the CPU usage.
   *
   * @return the CPU usage in percentage
   */
  public synchronized double getCpuUsage() {
    if (lastSystemTime == 0L) {
      updateBaselineCounters();
      return 0.0D;
    } else {
      long systemTime = System.nanoTime();
      long processCpuTime = operatingSystemMxBean.getProcessCpuTime();
      double cpuUsage = (double) (processCpuTime - lastProcessCpuTime)
          / (double) (systemTime - lastSystemTime);
      lastSystemTime = systemTime;
      lastProcessCpuTime = processCpuTime;

      return cpuUsage / (double) operatingSystemMxBean.getAvailableProcessors();
    }
  }

  private void updateBaselineCounters() {
    lastSystemTime = System.nanoTime();
    lastProcessCpuTime = operatingSystemMxBean.getProcessCpuTime();
  }

  /**
   * Retrieves the number of running threads.
   *
   * @return the number of running threads
   */
  public int countRunningThreads() {
    int countRunning = 0;
    for (var thread : Thread.getAllStackTraces().keySet()) {
      if (thread.getState() == Thread.State.RUNNABLE) {
        countRunning++;
      }
    }
    return countRunning;
  }

  public long getTotalMemory() {
    return Runtime.getRuntime().totalMemory();
  }

  public long getFreeMemory() {
    return Runtime.getRuntime().maxMemory() - getUsedMemory();
  }

  public long getUsedMemory() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }
}
