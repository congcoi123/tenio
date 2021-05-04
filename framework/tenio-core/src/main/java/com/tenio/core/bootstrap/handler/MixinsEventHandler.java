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
package com.tenio.core.bootstrap.handler;

import java.util.Optional;
import java.util.function.Consumer;

import com.tenio.core.bootstrap.annotation.ExtAutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventException;
import com.tenio.core.extension.event.IEventFetchedBandwidthInfo;
import com.tenio.core.extension.event.IEventFetchedCcuNumber;
import com.tenio.core.extension.event.IEventMonitoringSystem;

/**
 * @author kong
 */
@ExtComponent
public final class MixinsEventHandler extends AbstractExtensionHandler {

	@ExtAutowiredAcceptNull
	private IEventException __eventException;

	@ExtAutowiredAcceptNull
	private IEventFetchedBandwidthInfo __eventFetchedBandwidthInfo;

	@ExtAutowiredAcceptNull
	private IEventFetchedCcuNumber __eventFetchedCcuNumber;

	@ExtAutowiredAcceptNull
	private IEventMonitoringSystem __eventMonitoringSystem;

	public void initialize() {
		Optional<IEventException> eventExceptionOp = Optional.ofNullable(__eventException);
		Optional<IEventFetchedBandwidthInfo> eventFetchedBandwidthInfoOp = Optional
				.ofNullable(__eventFetchedBandwidthInfo);
		Optional<IEventFetchedCcuNumber> eventFetchedCcuNumberOp = Optional.ofNullable(__eventFetchedCcuNumber);
		Optional<IEventMonitoringSystem> eventMonitoringSystemOp = Optional.ofNullable(__eventMonitoringSystem);

		eventExceptionOp.ifPresent(new Consumer<IEventException>() {

			@Override
			public void accept(IEventException event) {
				_on(ExtEvent.EXCEPTION, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Throwable throwable = _getThrowable(params[0]);

						event.handle(throwable);

						return null;
					}
				});
			}
		});

		eventFetchedBandwidthInfoOp.ifPresent(new Consumer<IEventFetchedBandwidthInfo>() {

			@Override
			public void accept(IEventFetchedBandwidthInfo event) {
				_on(ExtEvent.FETCHED_BANDWIDTH_INFO, new ISubscriber() {

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

		eventFetchedCcuNumberOp.ifPresent(new Consumer<IEventFetchedCcuNumber>() {

			@Override
			public void accept(IEventFetchedCcuNumber event) {
				_on(ExtEvent.FETCHED_CCU_NUMBER, new ISubscriber() {

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

		eventMonitoringSystemOp.ifPresent(new Consumer<IEventMonitoringSystem>() {

			@Override
			public void accept(IEventMonitoringSystem event) {
				_on(ExtEvent.MONITORING_SYSTEM, new ISubscriber() {

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
