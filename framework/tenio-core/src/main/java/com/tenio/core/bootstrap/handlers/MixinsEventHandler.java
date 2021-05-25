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
import com.tenio.core.configuration.defines.ExtensionEvent;
import com.tenio.core.event.Subscriber;
import com.tenio.core.exceptions.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventServerException;
import com.tenio.core.extension.events.EventFetchedBandwidthInfo;
import com.tenio.core.extension.events.EventFetchedCcuInfo;
import com.tenio.core.extension.events.EventSystemMonitoring;

/**
 * @author kong
 */
@Component
//TODO: Add description
public final class MixinsEventHandler extends AbstractExtension {

	@AutowiredAcceptNull
	private EventServerException __eventException;

	@AutowiredAcceptNull
	private EventFetchedBandwidthInfo __eventFetchedBandwidthInfo;

	@AutowiredAcceptNull
	private EventFetchedCcuInfo __eventFetchedCcuNumber;

	@AutowiredAcceptNull
	private EventSystemMonitoring __eventMonitoringSystem;

	public void initialize() {
		Optional<EventServerException> eventExceptionOp = Optional.ofNullable(__eventException);
		Optional<EventFetchedBandwidthInfo> eventFetchedBandwidthInfoOp = Optional
				.ofNullable(__eventFetchedBandwidthInfo);
		Optional<EventFetchedCcuInfo> eventFetchedCcuNumberOp = Optional.ofNullable(__eventFetchedCcuNumber);
		Optional<EventSystemMonitoring> eventMonitoringSystemOp = Optional.ofNullable(__eventMonitoringSystem);

		eventExceptionOp.ifPresent(new Consumer<EventServerException>() {

			@Override
			public void accept(EventServerException event) {
				_on(ExtensionEvent.EXCEPTION, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Throwable throwable = _getThrowable(params[0]);

						event.handle(throwable);

						return null;
					}
				});
			}
		});

		eventFetchedBandwidthInfoOp.ifPresent(new Consumer<EventFetchedBandwidthInfo>() {

			@Override
			public void accept(EventFetchedBandwidthInfo event) {
				_on(ExtensionEvent.FETCHED_BANDWIDTH_INFO, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						long lastReadThroughput = _getLong(params[0]);
						long lastWriteThroughput = _getLong(params[1]);
						long realWriteThroughput = _getLong(params[2]);
						long currentReadBytes = _getLong(params[3]);
						long currentWrittenBytes = _getLong(params[4]);
						long realWrittenBytes = _getLong(params[5]);
						String counterName = _getString(params[6]);

						event.handle(lastReadThroughput, lastWriteThroughput, realWriteThroughput, currentReadBytes,
								currentWrittenBytes, realWrittenBytes, counterName);

						return null;
					}
				});
			}
		});

		eventFetchedCcuNumberOp.ifPresent(new Consumer<EventFetchedCcuInfo>() {

			@Override
			public void accept(EventFetchedCcuInfo event) {
				_on(ExtensionEvent.FETCHED_CCU_NUMBER, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						int numberPlayers = _getInteger(params[0]);
						int numberAlls = _getInteger(params[1]);

						event.handle(numberPlayers, numberAlls);

						return null;
					}
				});
			}
		});

		eventMonitoringSystemOp.ifPresent(new Consumer<EventSystemMonitoring>() {

			@Override
			public void accept(EventSystemMonitoring event) {
				_on(ExtensionEvent.MONITORING_SYSTEM, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						double cpuUsage = _getDouble(params[0]);
						long totalMemory = _getLong(params[1]);
						long usedMemory = _getLong(params[2]);
						long freeMemory = _getLong(params[3]);
						int countRunningThreads = _getInteger(params[4]);

						event.handle(cpuUsage, totalMemory, usedMemory, freeMemory, countRunningThreads);

						return null;
					}
				});
			}
		});
	}

}
