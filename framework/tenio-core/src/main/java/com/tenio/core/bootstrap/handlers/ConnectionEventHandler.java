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

import com.tenio.common.data.elements.CommonObject;
import com.tenio.core.bootstrap.annotations.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotations.Component;
import com.tenio.core.configuration.defines.CoreMessageCode;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.event.Subscriber;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.ExtensionValueCastException;
import com.tenio.core.extension.events.EventAttachConnectionFailure;
import com.tenio.core.extension.events.EventAttachConnectionRequestValidation;
import com.tenio.core.extension.events.EventAttachConnectionSuccess;
import com.tenio.core.extension.events.EventConnectionEstablishedFailure;
import com.tenio.core.extension.events.EventConnectionEstablishedSuccess;
import com.tenio.core.extension.events.EventDisconnectConnection;
import com.tenio.core.extension.events.EventReceivedMessageFromConnection;

/**
 * @author kong
 */
@Component
//TODO: Add description
public final class ConnectionEventHandler {

	@AutowiredAcceptNull
	private EventConnectionEstablishedSuccess __eventConnectionEstablishedSuccess;

	@AutowiredAcceptNull
	private EventConnectionEstablishedFailure __eventConnectionEstablishedFailed;

	@AutowiredAcceptNull
	private EventReceivedMessageFromConnection __eventReceivedMessageFromConnection;

	@AutowiredAcceptNull
	private EventAttachConnectionRequestValidation __eventAttachConnectionRequestValidate;

	@AutowiredAcceptNull
	private EventAttachConnectionSuccess __eventAttachConnectionSuccess;

	@AutowiredAcceptNull
	private EventAttachConnectionFailure __eventAttachConnectionFailed;

	@AutowiredAcceptNull
	private EventDisconnectConnection __eventDisconnectConnection;

	public void initialize(EventManager eventManager) {
		Optional<EventConnectionEstablishedSuccess> eventConnectionEstablishedSuccessOp = Optional
				.ofNullable(__eventConnectionEstablishedSuccess);
		Optional<EventConnectionEstablishedFailure> eventConnectionEstablishedFailedOp = Optional
				.ofNullable(__eventConnectionEstablishedFailed);
		Optional<EventReceivedMessageFromConnection> eventReceivedMessageFromConnectionOp = Optional
				.ofNullable(__eventReceivedMessageFromConnection);

		Optional<EventAttachConnectionRequestValidation> eventAttachConnectionRequestValidateOp = Optional
				.ofNullable(__eventAttachConnectionRequestValidate);
		Optional<EventAttachConnectionSuccess> eventAttachConnectionSuccessOp = Optional
				.ofNullable(__eventAttachConnectionSuccess);
		Optional<EventAttachConnectionFailure> eventAttachConnectionFailedOp = Optional
				.ofNullable(__eventAttachConnectionFailed);

		Optional<EventDisconnectConnection> eventDisconnectConnectionOp = Optional
				.ofNullable(__eventDisconnectConnection);

		eventConnectionEstablishedSuccessOp.ifPresent(new Consumer<EventConnectionEstablishedSuccess>() {

			@Override
			public void accept(EventConnectionEstablishedSuccess event) {
				eventManager.on(ServerEvent.CONNECTION_ESTABLISHED_SUCCESS, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {

						event.handle(session, message);

						return null;
					}
				});
			}
		});

		eventConnectionEstablishedFailedOp.ifPresent(new Consumer<EventConnectionEstablishedFailure>() {

			@Override
			public void accept(EventConnectionEstablishedFailure event) {
				eventManager.on(ServerEvent.CONNECTION_ESTABLISHED_FAILURE, new Subscriber() {

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

		eventAttachConnectionRequestValidateOp.ifPresent(new Consumer<EventAttachConnectionRequestValidation>() {

			@Override
			public void accept(EventAttachConnectionRequestValidation event) {
				eventManager.on(ServerEvent.ATTACH_CONNECTION_REQUEST_VALIDATION, new Subscriber() {

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
				eventManager.on(ServerEvent.ATTACH_CONNECTION_SUCCESS, new Subscriber() {

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

		eventAttachConnectionFailedOp.ifPresent(new Consumer<EventAttachConnectionFailure>() {

			@Override
			public void accept(EventAttachConnectionFailure event) {
				eventManager.on(ServerEvent.ATTACH_CONNECTION_FAILURE, new Subscriber() {

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
				eventManager.on(ServerEvent.DISCONNECT_CONNECTION, new Subscriber() {

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
