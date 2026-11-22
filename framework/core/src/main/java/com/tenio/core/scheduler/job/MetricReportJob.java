/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.core.scheduler.job;

import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.Scheduled;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.scheduler.task.CcuReportTask;
import com.tenio.core.scheduler.task.DeadlockScanTask;
import com.tenio.core.scheduler.task.SystemMonitoringTask;
import com.tenio.core.scheduler.task.TrafficCounterTask;
import com.tenio.core.server.ServerImpl;

/**
 * Collects server metrics on the injector-owned scheduler.
 */
@Component(profile = CoreConstant.METRICS_PROFILE)
public final class MetricReportJob {

    private static final int INITIAL_DELAY_SECONDS = 60;
    private static final int METRICS_INTERVAL_SECONDS = 60;

    private final CcuReportTask ccuReport = CcuReportTask.newInstance(ServerImpl.getInstance().getEventManager());
    private final DeadlockScanTask deadlockScan = DeadlockScanTask.newInstance();
    private final SystemMonitoringTask systemMonitoring = SystemMonitoringTask.newInstance(ServerImpl.getInstance().getEventManager());
    private final TrafficCounterTask trafficCounter = TrafficCounterTask.newInstance(ServerImpl.getInstance().getEventManager());

    /**
     * Connects counters created during network startup.
     */
    public void configure(NetworkReaderStatistic reader, NetworkWriterStatistic writer) {
        ccuReport.setPlayerManager(ServerImpl.getInstance().getPlayerManager());
        trafficCounter.setNetworkReaderStatistic(reader);
        trafficCounter.setNetworkWriterStatistic(writer);
    }

    @Scheduled(threadName = "REPORT_CONCURRENT_USERS", intervalSeconds = METRICS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void ccuReport() {
        ccuReport.sample();
    }

    @Scheduled(threadName = "SCAN_FOR_DEADLOCKS", intervalSeconds = METRICS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void deadlockScan() {
        deadlockScan.sample();
    }

    @Scheduled(threadName = "REPORT_SYSTEM_METRICS", intervalSeconds = METRICS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void systemMonitoring() {
        systemMonitoring.sample();
    }

    @Scheduled(threadName = "REPORT_NETWORK_TRAFFIC", intervalSeconds = METRICS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void trafficCounter() {
        trafficCounter.sample();
    }
}
