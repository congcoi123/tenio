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

import com.tenio.common.data.CommonObject;
import com.tenio.core.bootstrap.annotation.ExtAutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.entity.ZeroPlayer;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventDisconnectPlayer;
import com.tenio.core.extension.event.IEventPlayerGotTimeout;
import com.tenio.core.extension.event.IEventPlayerLoginedFailed;
import com.tenio.core.extension.event.IEventPlayerLoginedSuccess;
import com.tenio.core.extension.event.IEventPlayerReconnectRequestHandle;
import com.tenio.core.extension.event.IEventPlayerReconnectSuccess;
import com.tenio.core.extension.event.IEventReceivedMessageFromPlayer;
import com.tenio.core.extension.event.IEventSendMessageToPlayer;
import com.tenio.core.network.IConnection;

/**
 * @author kong
 */
@ExtComponent
public final class PlayerEventHandler extends AbstractExtensionHandler {

	@ExtAutowiredAcceptNull
	private IEventPlayerLoginedFailed __eventPlayerLoginedFailed;

	@ExtAutowiredAcceptNull
	private IEventPlayerLoginedSuccess __eventPlayerLoginedSuccess;

	@ExtAutowiredAcceptNull
	private IEventPlayerReconnectRequestHandle __eventPlayerReconnectRequestHandle;

	@ExtAutowiredAcceptNull
	private IEventPlayerReconnectSuccess __eventPlayerReconnectSuccess;

	@ExtAutowiredAcceptNull
	private IEventPlayerGotTimeout __eventPlayerGotTimeout;

	@ExtAutowiredAcceptNull
	private IEventReceivedMessageFromPlayer __eventReceivedMessageFromPlayer;

	@ExtAutowiredAcceptNull
	private IEventSendMessageToPlayer __eventSendMessageToPlayer;

	@ExtAutowiredAcceptNull
	private IEventDisconnectPlayer __eventDisconnectPlayer;

	public void initialize() {
		Optional<IEventPlayerLoginedFailed> eventPlayerLoginedFailedOp = Optional
				.ofNullable(__eventPlayerLoginedFailed);
		Optional<IEventPlayerLoginedSuccess> eventPlayerLoginedSuccessOp = Optional
				.ofNullable(__eventPlayerLoginedSuccess);

		Optional<IEventPlayerReconnectRequestHandle> eventPlayerReconnectRequestHandleOp = Optional
				.ofNullable(__eventPlayerReconnectRequestHandle);
		Optional<IEventPlayerReconnectSuccess> eventPlayerReconnectSuccessOp = Optional
				.ofNullable(__eventPlayerReconnectSuccess);
		Optional<IEventPlayerGotTimeout> eventPlayerGotTimeoutOp = Optional.ofNullable(__eventPlayerGotTimeout);

		Optional<IEventReceivedMessageFromPlayer> eventReceivedMessageFromPlayerOp = Optional
				.ofNullable(__eventReceivedMessageFromPlayer);
		Optional<IEventSendMessageToPlayer> eventSendMessageToPlayerOp = Optional
				.ofNullable(__eventSendMessageToPlayer);

		Optional<IEventDisconnectPlayer> eventDisconnectPlayerOp = Optional.ofNullable(__eventDisconnectPlayer);

		eventPlayerLoginedFailedOp.ifPresent(new Consumer<IEventPlayerLoginedFailed>() {

			public void accept(IEventPlayerLoginedFailed event) {

				_on(ZeroEvent.PLAYER_LOGINED_FAILED, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);
						CoreMessageCode code = _getCoreMessageCode(params[1]);

						event.handle(player, code);

						return null;
					}
				});
			}
		});

		eventPlayerLoginedSuccessOp.ifPresent(new Consumer<IEventPlayerLoginedSuccess>() {

			public void accept(IEventPlayerLoginedSuccess event) {

				_on(ZeroEvent.PLAYER_LOGINED_SUCCESS, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventPlayerReconnectRequestHandleOp.ifPresent(new Consumer<IEventPlayerReconnectRequestHandle>() {

			public void accept(IEventPlayerReconnectRequestHandle event) {

				_on(ZeroEvent.PLAYER_RECONNECT_REQUEST_HANDLE, new ISubscriber() {

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

		eventPlayerReconnectSuccessOp.ifPresent(new Consumer<IEventPlayerReconnectSuccess>() {

			public void accept(IEventPlayerReconnectSuccess event) {

				_on(ZeroEvent.PLAYER_RECONNECT_SUCCESS, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventPlayerGotTimeoutOp.ifPresent(new Consumer<IEventPlayerGotTimeout>() {

			public void accept(IEventPlayerGotTimeout event) {

				_on(ZeroEvent.PLAYER_GOT_TIMEOUT, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});

		eventReceivedMessageFromPlayerOp.ifPresent(new Consumer<IEventReceivedMessageFromPlayer>() {

			@Override
			public void accept(IEventReceivedMessageFromPlayer event) {
				_on(ZeroEvent.RECEIVED_MESSAGE_FROM_PLAYER, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);
						int connectionIndex = _getInteger(params[1]);
						CommonObject message = _getCommonObject(params[2]);

						event.handle(player, connectionIndex, message);

						return null;
					}
				});
			}
		});

		eventSendMessageToPlayerOp.ifPresent(new Consumer<IEventSendMessageToPlayer>() {

			@Override
			public void accept(IEventSendMessageToPlayer event) {
				_on(ZeroEvent.SEND_MESSAGE_TO_PLAYER, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);
						int connectionIndex = _getInteger(params[1]);
						CommonObject message = _getCommonObject(params[2]);

						event.handle(player, connectionIndex, message);

						return null;
					}
				});
			}
		});

		eventDisconnectPlayerOp.ifPresent(new Consumer<IEventDisconnectPlayer>() {

			@Override
			public void accept(IEventDisconnectPlayer event) {
				_on(ZeroEvent.DISCONNECT_PLAYER, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						ZeroPlayer player = _getPlayer(params[0]);

						event.handle(player);

						return null;
					}
				});
			}
		});
	}

}
