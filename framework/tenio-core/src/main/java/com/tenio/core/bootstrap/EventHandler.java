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
package com.tenio.core.bootstrap;

import java.util.Optional;
import java.util.function.Consumer;

import com.tenio.common.element.CommonObject;
import com.tenio.core.bootstrap.annotation.ExtAutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventConnectionEstablishedSuccess;
import com.tenio.core.extension.event.IEventFetchedCcuNumber;
import com.tenio.core.extension.event.IEventPlayerLoginedSuccess;
import com.tenio.core.extension.event.IEventReceivedMessageFromPlayer;
import com.tenio.core.network.IConnection;

/**
 * @author kong
 */
@ExtComponent
public final class EventHandler extends AbstractExtensionHandler {

	@ExtAutowiredAcceptNull
	private IEventFetchedCcuNumber __eventFetchedCcuNumber;

	@ExtAutowiredAcceptNull
	private IEventConnectionEstablishedSuccess __eventConnectionEstablishedSuccess;

	@ExtAutowiredAcceptNull
	private IEventPlayerLoginedSuccess __eventPlayerLoginedSuccess;

	@ExtAutowiredAcceptNull
	private IEventReceivedMessageFromPlayer __eventReceivedMessageFromPlayer;

	public void initialize() {
		// Declare optional instances
		Optional<IEventFetchedCcuNumber> eventFetchedCcuNumberOp = Optional.ofNullable(__eventFetchedCcuNumber);
		Optional<IEventConnectionEstablishedSuccess> eventConnectionEstablishedSuccessOp = Optional
				.ofNullable(__eventConnectionEstablishedSuccess);
		Optional<IEventPlayerLoginedSuccess> eventPlayerLoginedSuccessOp = Optional
				.ofNullable(__eventPlayerLoginedSuccess);
		Optional<IEventReceivedMessageFromPlayer> eventReceivedMessageFromPlayerOp = Optional
				.ofNullable(__eventReceivedMessageFromPlayer);

		// Handle events
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

		eventConnectionEstablishedSuccessOp.ifPresent(new Consumer<IEventConnectionEstablishedSuccess>() {

			@Override
			public void accept(IEventConnectionEstablishedSuccess event) {
				_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						IConnection connection = _getConnection(params[0]);
						CommonObject message = _getCommonObject(params[1]);

						event.handle(connection, message);

						return null;
					}
				});
			}
		});

		eventPlayerLoginedSuccessOp.ifPresent(new Consumer<IEventPlayerLoginedSuccess>() {

			public void accept(IEventPlayerLoginedSuccess event) {

				_on(ExtEvent.PLAYER_LOGINED_SUCCESS, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						IPlayer player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventReceivedMessageFromPlayerOp.ifPresent(new Consumer<IEventReceivedMessageFromPlayer>() {

			@Override
			public void accept(IEventReceivedMessageFromPlayer event) {
				_on(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						IPlayer player = _getPlayer(params[0]);
						int connectionIndex = _getInteger(params[1]);
						CommonObject message = _getCommonObject(params[2]);

						event.handle(player, connectionIndex, message);

						return null;
					}
				});
			}
		});

	}

}
