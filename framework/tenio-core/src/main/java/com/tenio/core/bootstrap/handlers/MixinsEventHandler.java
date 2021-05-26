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
package com.tenio.core.bootstrap.handlers;

import java.util.Optional;
import java.util.function.Consumer;

import com.tenio.core.bootstrap.annotations.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotations.Component;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.event.Subscriber;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.extension.events.EventFetchedBandwidthInfo;
import com.tenio.core.extension.events.EventFetchedCcuInfo;
import com.tenio.core.extension.events.EventServerException;
import com.tenio.core.extension.events.EventSystemMonitoring;

/**
 * @author kong
 */
@Component
//TODO: Add description
public final class MixinsEventHandler {

	@AutowiredAcceptNull
	private EventServerException __eventServerException;

	@AutowiredAcceptNull
	private EventFetchedBandwidthInfo __eventFetchedBandwidthInfo;

	@AutowiredAcceptNull
	private EventFetchedCcuInfo __eventFetchedCcuInfo;

	@AutowiredAcceptNull
	private EventSystemMonitoring __eventSystemMonitoring;

	public void initialize(EventManager eventManager) {
		
		Optional<EventServerException> eventServerExceptionOp = Optional.ofNullable(__eventServerException);
		Optional<EventFetchedBandwidthInfo> eventFetchedBandwidthInfoOp = Optional
				.ofNullable(__eventFetchedBandwidthInfo);
		Optional<EventFetchedCcuInfo> eventFetchedCcuInfoOp = Optional.ofNullable(__eventFetchedCcuInfo);
		Optional<EventSystemMonitoring> eventSystemMonitoringOp = Optional.ofNullable(__eventSystemMonitoring);

		eventServerExceptionOp.ifPresent(new Consumer<EventServerException>() {

			@Override
			public void accept(EventServerException event) {
				eventManager.on(ServerEvent.SERVER_EXCEPTION, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Throwable throwable = (Throwable) params[0];

						event.handle(throwable);

						return null;
					}
				});
			}
		});

		eventFetchedBandwidthInfoOp.ifPresent(new Consumer<EventFetchedBandwidthInfo>() {

			@Override
			public void accept(EventFetchedBandwidthInfo event) {
				eventManager.on(ServerEvent.FETCHED_BANDWIDTH_INFO, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						long readBytes = (long) params[0];
						long readPackets = (long) params[1];
						long readDroppedPackets = (long) params[2];
						long writtenBytes = (long) params[3];
						long writtenPackets = (long) params[4];
						long writtenDroppedPacketsByPolicy = (long) params[5];
						long writtenDroppedPacketsByFull = (long) params[6];

						event.handle(readBytes, readPackets, readDroppedPackets, writtenBytes, writtenPackets,
								writtenDroppedPacketsByPolicy, writtenDroppedPacketsByFull);

						return null;
					}
				});
			}
		});

		eventFetchedCcuInfoOp.ifPresent(new Consumer<EventFetchedCcuInfo>() {

			@Override
			public void accept(EventFetchedCcuInfo event) {
				eventManager.on(ServerEvent.FETCHED_CCU_INFO, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						int numberPlayers = (int) params[0];

						event.handle(numberPlayers);

						return null;
					}
				});
			}
		});

		eventSystemMonitoringOp.ifPresent(new Consumer<EventSystemMonitoring>() {

			@Override
			public void accept(EventSystemMonitoring event) {
				eventManager.on(ServerEvent.SYSTEM_MONITORING, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						double cpuUsage = (double) params[0];
						long totalMemory = (long) params[1];
						long usedMemory = (long) params[2];
						long freeMemory = (long) params[3];
						int countRunningThreads = (int) params[4];

						event.handle(cpuUsage, totalMemory, usedMemory, freeMemory, countRunningThreads);

						return null;
					}
				});
			}
		});
	}

}
