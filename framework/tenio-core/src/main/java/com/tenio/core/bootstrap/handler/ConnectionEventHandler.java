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
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.entity.Player;
import com.tenio.core.event.Subscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.EventAttachConnectionFailed;
import com.tenio.core.extension.event.EventAttachConnectionRequestValidate;
import com.tenio.core.extension.event.EventAttachConnectionSuccess;
import com.tenio.core.extension.event.EventConnectionEstablishedFailed;
import com.tenio.core.extension.event.EventConnectionEstablishedSuccess;
import com.tenio.core.extension.event.EventDisconnectConnection;
import com.tenio.core.extension.event.EventReceivedMessageFromConnection;
import com.tenio.core.network.entity.connection.Connection;

/**
 * @author kong
 */
@Component
public final class ConnectionEventHandler extends AbstractExtensionHandler {

	@AutowiredAcceptNull
	private EventConnectionEstablishedSuccess __eventConnectionEstablishedSuccess;

	@AutowiredAcceptNull
	private EventConnectionEstablishedFailed __eventConnectionEstablishedFailed;

	@AutowiredAcceptNull
	private EventReceivedMessageFromConnection __eventReceivedMessageFromConnection;

	@AutowiredAcceptNull
	private EventAttachConnectionRequestValidate __eventAttachConnectionRequestValidate;

	@AutowiredAcceptNull
	private EventAttachConnectionSuccess __eventAttachConnectionSuccess;

	@AutowiredAcceptNull
	private EventAttachConnectionFailed __eventAttachConnectionFailed;

	@AutowiredAcceptNull
	private EventDisconnectConnection __eventDisconnectConnection;

	public void initialize() {
		Optional<EventConnectionEstablishedSuccess> eventConnectionEstablishedSuccessOp = Optional
				.ofNullable(__eventConnectionEstablishedSuccess);
		Optional<EventConnectionEstablishedFailed> eventConnectionEstablishedFailedOp = Optional
				.ofNullable(__eventConnectionEstablishedFailed);
		Optional<EventReceivedMessageFromConnection> eventReceivedMessageFromConnectionOp = Optional
				.ofNullable(__eventReceivedMessageFromConnection);

		Optional<EventAttachConnectionRequestValidate> eventAttachConnectionRequestValidateOp = Optional
				.ofNullable(__eventAttachConnectionRequestValidate);
		Optional<EventAttachConnectionSuccess> eventAttachConnectionSuccessOp = Optional
				.ofNullable(__eventAttachConnectionSuccess);
		Optional<EventAttachConnectionFailed> eventAttachConnectionFailedOp = Optional
				.ofNullable(__eventAttachConnectionFailed);

		Optional<EventDisconnectConnection> eventDisconnectConnectionOp = Optional
				.ofNullable(__eventDisconnectConnection);

		eventConnectionEstablishedSuccessOp.ifPresent(new Consumer<EventConnectionEstablishedSuccess>() {

			@Override
			public void accept(EventConnectionEstablishedSuccess event) {
				_on(ZeroEvent.CONNECTION_ESTABLISHED_SUCCESS, new Subscriber() {

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

		eventConnectionEstablishedFailedOp.ifPresent(new Consumer<EventConnectionEstablishedFailed>() {

			@Override
			public void accept(EventConnectionEstablishedFailed event) {
				_on(ZeroEvent.CONNECTION_ESTABLISHED_FAILED, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Connection connection = _getConnection(params[0]);
						CoreMessageCode code = _getCoreMessageCode(params[1]);

						event.handle(connection, code);

						return null;
					}
				});
			}
		});

		eventReceivedMessageFromConnectionOp.ifPresent(new Consumer<EventReceivedMessageFromConnection>() {

			@Override
			public void accept(EventReceivedMessageFromConnection event) {
				_on(ZeroEvent.RECEIVED_MESSAGE_FROM_CONNECTION, new Subscriber() {

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

		eventAttachConnectionRequestValidateOp.ifPresent(new Consumer<EventAttachConnectionRequestValidate>() {

			@Override
			public void accept(EventAttachConnectionRequestValidate event) {
				_on(ZeroEvent.ATTACH_CONNECTION_REQUEST_VALIDATE, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						int connectionIndex = _getInteger(params[0]);
						CommonObject message = _getCommonObject(params[1]);

						return event.handle(connectionIndex, message);
					}
				});
			}
		});

		eventAttachConnectionSuccessOp.ifPresent(new Consumer<EventAttachConnectionSuccess>() {

			@Override
			public void accept(EventAttachConnectionSuccess event) {
				_on(ZeroEvent.ATTACH_CONNECTION_SUCCESS, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						int connectionIndex = _getInteger(params[0]);
						Player player = _getPlayer(params[1]);

						event.handle(connectionIndex, player);

						return null;
					}
				});
			}
		});

		eventAttachConnectionFailedOp.ifPresent(new Consumer<EventAttachConnectionFailed>() {

			@Override
			public void accept(EventAttachConnectionFailed event) {
				_on(ZeroEvent.ATTACH_CONNECTION_FAILED, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						int connectionIndex = _getInteger(params[0]);
						CommonObject message = _getCommonObject(params[1]);
						CoreMessageCode code = _getCoreMessageCode(params[2]);

						event.handle(connectionIndex, message, code);

						return null;
					}
				});
			}
		});

		eventDisconnectConnectionOp.ifPresent(new Consumer<EventDisconnectConnection>() {

			@Override
			public void accept(EventDisconnectConnection event) {
				_on(ZeroEvent.DISCONNECT_CONNECTION, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						Connection connection = _getConnection(params[0]);

						event.handle(connection);

						return null;
					}
				});
			}
		});

	}

}
