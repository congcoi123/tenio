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
package com.tenio.core.schedule.tasks;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;

public final class TrafficCounterTask extends AbstractTask {

	private NetworkReaderStatistic __networkReaderStatistic;
	private NetworkWriterStatistic __networkWriterStatistic;

	public static TrafficCounterTask newInstance(EventManager eventManager) {
		return new TrafficCounterTask(eventManager);
	}

	private TrafficCounterTask(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public ScheduledFuture<?> run() {
		return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			__eventManager.emit(ServerEvent.FETCHED_BANDWIDTH_INFO, __networkReaderStatistic.getReadBytes(),
					__networkReaderStatistic.getReadPackets(), __networkReaderStatistic.getReadDroppedPackets(),
					__networkWriterStatistic.getWrittenBytes(), __networkWriterStatistic.getWrittenPackets(),
					__networkWriterStatistic.getWrittenDroppedPacketsByPolicy(),
					__networkWriterStatistic.getWrittenDroppedPacketsByFull());
		}, 0, __interval, TimeUnit.SECONDS);
	}

	public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
		__networkReaderStatistic = networkReaderStatistic;
	}

	public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
		__networkWriterStatistic = networkWriterStatistic;
	}

}
