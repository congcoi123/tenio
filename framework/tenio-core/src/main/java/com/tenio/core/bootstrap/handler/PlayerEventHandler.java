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

import com.tenio.common.data.element.CommonObject;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.event.Subscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.EventDisconnectPlayer;
import com.tenio.core.extension.event.EventPlayerGotTimeout;
import com.tenio.core.extension.event.EventPlayerLoginedFailed;
import com.tenio.core.extension.event.EventPlayerLoginedSuccess;
import com.tenio.core.extension.event.EventPlayerReconnectRequestHandle;
import com.tenio.core.extension.event.EventPlayerReconnectSuccess;
import com.tenio.core.extension.event.EventReceivedMessageFromPlayer;
import com.tenio.core.extension.event.EventSendMessageToPlayer;
import com.tenio.core.network.entity.session.Connection;

/**
 * @author kong
 */
@Component
//TODO: Add description
public final class PlayerEventHandler extends AbstractExtensionHandler {

	@AutowiredAcceptNull
	private EventPlayerLoginedFailed __eventPlayerLoginedFailed;

	@AutowiredAcceptNull
	private EventPlayerLoginedSuccess __eventPlayerLoginedSuccess;

	@AutowiredAcceptNull
	private EventPlayerReconnectRequestHandle __eventPlayerReconnectRequestHandle;

	@AutowiredAcceptNull
	private EventPlayerReconnectSuccess __eventPlayerReconnectSuccess;

	@AutowiredAcceptNull
	private EventPlayerGotTimeout __eventPlayerGotTimeout;

	@AutowiredAcceptNull
	private EventReceivedMessageFromPlayer __eventReceivedMessageFromPlayer;

	@AutowiredAcceptNull
	private EventSendMessageToPlayer __eventSendMessageToPlayer;

	@AutowiredAcceptNull
	private EventDisconnectPlayer __eventDisconnectPlayer;

	public void initialize() {
		Optional<EventPlayerLoginedFailed> eventPlayerLoginedFailedOp = Optional
				.ofNullable(__eventPlayerLoginedFailed);
		Optional<EventPlayerLoginedSuccess> eventPlayerLoginedSuccessOp = Optional
				.ofNullable(__eventPlayerLoginedSuccess);

		Optional<EventPlayerReconnectRequestHandle> eventPlayerReconnectRequestHandleOp = Optional
				.ofNullable(__eventPlayerReconnectRequestHandle);
		Optional<EventPlayerReconnectSuccess> eventPlayerReconnectSuccessOp = Optional
				.ofNullable(__eventPlayerReconnectSuccess);
		Optional<EventPlayerGotTimeout> eventPlayerGotTimeoutOp = Optional.ofNullable(__eventPlayerGotTimeout);

		Optional<EventReceivedMessageFromPlayer> eventReceivedMessageFromPlayerOp = Optional
				.ofNullable(__eventReceivedMessageFromPlayer);
		Optional<EventSendMessageToPlayer> eventSendMessageToPlayerOp = Optional
				.ofNullable(__eventSendMessageToPlayer);

		Optional<EventDisconnectPlayer> eventDisconnectPlayerOp = Optional.ofNullable(__eventDisconnectPlayer);

		eventPlayerLoginedFailedOp.ifPresent(new Consumer<EventPlayerLoginedFailed>() {

			public void accept(EventPlayerLoginedFailed event) {

				_on(ExtensionEvent.PLAYER_LOGINED_FAILED, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);
						CoreMessageCode code = _getCoreMessageCode(params[1]);

						event.handle(player, code);

						return null;
					}
				});
			}
		});

		eventPlayerLoginedSuccessOp.ifPresent(new Consumer<EventPlayerLoginedSuccess>() {

			public void accept(EventPlayerLoginedSuccess event) {

				_on(ExtensionEvent.PLAYER_LOGINED_SUCCESS, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventPlayerReconnectRequestHandleOp.ifPresent(new Consumer<EventPlayerReconnectRequestHandle>() {

			public void accept(EventPlayerReconnectRequestHandle event) {

				_on(ExtensionEvent.PLAYER_RECONNECT_REQUEST_HANDLE, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Connection connection = _getConnection(params[0]);
						CommonObject message = _getCommonObject(params[1]);

						event.handle(connection, message);

						return null;
					}
				});
			}
		});

		eventPlayerReconnectSuccessOp.ifPresent(new Consumer<EventPlayerReconnectSuccess>() {

			public void accept(EventPlayerReconnectSuccess event) {

				_on(ExtensionEvent.PLAYER_RECONNECT_SUCCESS, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventPlayerGotTimeoutOp.ifPresent(new Consumer<EventPlayerGotTimeout>() {

			public void accept(EventPlayerGotTimeout event) {

				_on(ExtensionEvent.PLAYER_GOT_TIMEOUT, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventReceivedMessageFromPlayerOp.ifPresent(new Consumer<EventReceivedMessageFromPlayer>() {

			@Override
			public void accept(EventReceivedMessageFromPlayer event) {
				_on(ExtensionEvent.RECEIVED_MESSAGE_FROM_PLAYER, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);
						int connectionIndex = _getInteger(params[1]);
						CommonObject message = _getCommonObject(params[2]);

						event.handle(player, connectionIndex, message);

						return null;
					}
				});
			}
		});

		eventSendMessageToPlayerOp.ifPresent(new Consumer<EventSendMessageToPlayer>() {

			@Override
			public void accept(EventSendMessageToPlayer event) {
				_on(ExtensionEvent.SEND_MESSAGE_TO_PLAYER, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);
						int connectionIndex = _getInteger(params[1]);
						CommonObject message = _getCommonObject(params[2]);

						event.handle(player, connectionIndex, message);

						return null;
					}
				});
			}
		});

		eventDisconnectPlayerOp.ifPresent(new Consumer<EventDisconnectPlayer>() {

			@Override
			public void accept(EventDisconnectPlayer event) {
				_on(ExtensionEvent.DISCONNECT_PLAYER, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Player player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});
	}

}
