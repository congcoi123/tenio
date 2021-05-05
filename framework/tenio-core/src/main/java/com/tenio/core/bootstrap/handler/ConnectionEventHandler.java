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
import com.tenio.core.bootstrap.annotation.ExtAutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.entity.ZeroPlayer;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventAttachConnectionFailed;
import com.tenio.core.extension.event.IEventAttachConnectionRequestValidate;
import com.tenio.core.extension.event.IEventAttachConnectionSuccess;
import com.tenio.core.extension.event.IEventConnectionEstablishedFailed;
import com.tenio.core.extension.event.IEventConnectionEstablishedSuccess;
import com.tenio.core.extension.event.IEventDisconnectConnection;
import com.tenio.core.extension.event.IEventReceivedMessageFromConnection;
import com.tenio.core.network.entity.connection.Connection;

/**
 * @author kong
 */
@ExtComponent
public final class ConnectionEventHandler extends AbstractExtensionHandler {

	@ExtAutowiredAcceptNull
	private IEventConnectionEstablishedSuccess __eventConnectionEstablishedSuccess;

	@ExtAutowiredAcceptNull
	private IEventConnectionEstablishedFailed __eventConnectionEstablishedFailed;

	@ExtAutowiredAcceptNull
	private IEventReceivedMessageFromConnection __eventReceivedMessageFromConnection;

	@ExtAutowiredAcceptNull
	private IEventAttachConnectionRequestValidate __eventAttachConnectionRequestValidate;

	@ExtAutowiredAcceptNull
	private IEventAttachConnectionSuccess __eventAttachConnectionSuccess;

	@ExtAutowiredAcceptNull
	private IEventAttachConnectionFailed __eventAttachConnectionFailed;

	@ExtAutowiredAcceptNull
	private IEventDisconnectConnection __eventDisconnectConnection;

	public void initialize() {
		Optional<IEventConnectionEstablishedSuccess> eventConnectionEstablishedSuccessOp = Optional
				.ofNullable(__eventConnectionEstablishedSuccess);
		Optional<IEventConnectionEstablishedFailed> eventConnectionEstablishedFailedOp = Optional
				.ofNullable(__eventConnectionEstablishedFailed);
		Optional<IEventReceivedMessageFromConnection> eventReceivedMessageFromConnectionOp = Optional
				.ofNullable(__eventReceivedMessageFromConnection);

		Optional<IEventAttachConnectionRequestValidate> eventAttachConnectionRequestValidateOp = Optional
				.ofNullable(__eventAttachConnectionRequestValidate);
		Optional<IEventAttachConnectionSuccess> eventAttachConnectionSuccessOp = Optional
				.ofNullable(__eventAttachConnectionSuccess);
		Optional<IEventAttachConnectionFailed> eventAttachConnectionFailedOp = Optional
				.ofNullable(__eventAttachConnectionFailed);

		Optional<IEventDisconnectConnection> eventDisconnectConnectionOp = Optional
				.ofNullable(__eventDisconnectConnection);

		eventConnectionEstablishedSuccessOp.ifPresent(new Consumer<IEventConnectionEstablishedSuccess>() {

			@Override
			public void accept(IEventConnectionEstablishedSuccess event) {
				_on(ZeroEvent.CONNECTION_ESTABLISHED_SUCCESS, new ISubscriber() {

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

		eventConnectionEstablishedFailedOp.ifPresent(new Consumer<IEventConnectionEstablishedFailed>() {

			@Override
			public void accept(IEventConnectionEstablishedFailed event) {
				_on(ZeroEvent.CONNECTION_ESTABLISHED_FAILED, new ISubscriber() {

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

		eventReceivedMessageFromConnectionOp.ifPresent(new Consumer<IEventReceivedMessageFromConnection>() {

			@Override
			public void accept(IEventReceivedMessageFromConnection event) {
				_on(ZeroEvent.RECEIVED_MESSAGE_FROM_CONNECTION, new ISubscriber() {

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

		eventAttachConnectionRequestValidateOp.ifPresent(new Consumer<IEventAttachConnectionRequestValidate>() {

			@Override
			public void accept(IEventAttachConnectionRequestValidate event) {
				_on(ZeroEvent.ATTACH_CONNECTION_REQUEST_VALIDATE, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						int connectionIndex = _getInteger(params[0]);
						CommonObject message = _getCommonObject(params[1]);

						return event.handle(connectionIndex, message);
					}
				});
			}
		});

		eventAttachConnectionSuccessOp.ifPresent(new Consumer<IEventAttachConnectionSuccess>() {

			@Override
			public void accept(IEventAttachConnectionSuccess event) {
				_on(ZeroEvent.ATTACH_CONNECTION_SUCCESS, new ISubscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						int connectionIndex = _getInteger(params[0]);
						ZeroPlayer player = _getPlayer(params[1]);

						event.handle(connectionIndex, player);

						return null;
					}
				});
			}
		});

		eventAttachConnectionFailedOp.ifPresent(new Consumer<IEventAttachConnectionFailed>() {

			@Override
			public void accept(IEventAttachConnectionFailed event) {
				_on(ZeroEvent.ATTACH_CONNECTION_FAILED, new ISubscriber() {

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

		eventDisconnectConnectionOp.ifPresent(new Consumer<IEventDisconnectConnection>() {

			@Override
			public void accept(IEventDisconnectConnection event) {
				_on(ZeroEvent.DISCONNECT_CONNECTION, new ISubscriber() {

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
