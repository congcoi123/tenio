/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

package com.tenio.core.schedule.task.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.schedule.task.AbstractSystemTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Collecting the traffic data like the amount of reader and writer binary.
 */
public final class TrafficCounterTask extends AbstractSystemTask {

  private NetworkReaderStatistic networkReaderStatistic;
  private NetworkWriterStatistic networkWriterStatistic;

  private TrafficCounterTask(EventManager eventManager) {
    super(eventManager);
  }

  /**
   * Creates a new task instance.
   *
   * @param eventManager an instance of {@link EventManager}
   * @return a new instance of {@link TrafficCounterTask}
   */
  public static TrafficCounterTask newInstance(EventManager eventManager) {
    return new TrafficCounterTask(eventManager);
  }

  @Override
  public ScheduledFuture<?> run() {
    var threadFactoryTask =
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("traffic-counter-task-%d").build();
    return Executors.newSingleThreadScheduledExecutor(threadFactoryTask).scheduleAtFixedRate(
        () -> eventManager.emit(ServerEvent.FETCHED_BANDWIDTH_INFO,
            networkReaderStatistic.getReadBytes(),
            networkReaderStatistic.getReadPackets(),
            networkReaderStatistic.getReadDroppedPackets(),
            networkWriterStatistic.getWrittenBytes(), networkWriterStatistic.getWrittenPackets(),
            networkWriterStatistic.getWrittenDroppedPacketsByPolicy(),
            networkWriterStatistic.getWrittenDroppedPacketsByFull()),
        initialDelay, interval, TimeUnit.SECONDS);
  }

  /**
   * Sets the network reader statistic.
   *
   * @param networkReaderStatistic the {@link NetworkReaderStatistic} instance
   */
  public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
    this.networkReaderStatistic = networkReaderStatistic;
  }

  /**
   * Sets the network writer statistic.
   *
   * @param networkWriterStatistic the {@link NetworkWriterStatistic} instance
   */
  public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
    this.networkWriterStatistic = networkWriterStatistic;
  }
}
