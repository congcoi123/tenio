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
package com.tenio.core.bootstrap.event.handlers;

import java.util.Optional;
import java.util.function.Consumer;

import com.tenio.common.bootstrap.annotations.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotations.Component;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.entities.defines.modes.ConnectionDisconnectMode;
import com.tenio.core.entities.defines.results.AttachedConnectionResult;
import com.tenio.core.entities.defines.results.ConnectionEstablishedResult;
import com.tenio.core.event.Subscriber;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.extension.events.EventAttachConnectionRequestValidation;
import com.tenio.core.extension.events.EventAttachedConnectionResult;
import com.tenio.core.extension.events.EventConnectionEstablishedResult;
import com.tenio.core.extension.events.EventDisconnectConnection;
import com.tenio.core.network.entities.session.Session;

@Component
public final class ConnectionEventHandler {

	@AutowiredAcceptNull
	private EventConnectionEstablishedResult __eventConnectionEstablishedResult;

	@AutowiredAcceptNull
	private EventAttachConnectionRequestValidation __eventAttachConnectionRequestValidation;

	@AutowiredAcceptNull
	private EventAttachedConnectionResult __eventAttachedConnectionResult;

	@AutowiredAcceptNull
	private EventDisconnectConnection __eventDisconnectConnection;

	public void initialize(EventManager eventManager) {

		Optional<EventConnectionEstablishedResult> eventConnectionEstablishedResultOp = Optional
				.ofNullable(__eventConnectionEstablishedResult);

		Optional<EventAttachConnectionRequestValidation> eventAttachConnectionRequestValidationOp = Optional
				.ofNullable(__eventAttachConnectionRequestValidation);
		Optional<EventAttachedConnectionResult> eventAttachedConnectionResultOp = Optional
				.ofNullable(__eventAttachedConnectionResult);

		Optional<EventDisconnectConnection> eventDisconnectConnectionOp = Optional
				.ofNullable(__eventDisconnectConnection);

		eventConnectionEstablishedResultOp.ifPresent(new Consumer<EventConnectionEstablishedResult>() {

			@Override
			public void accept(EventConnectionEstablishedResult event) {
				eventManager.on(ServerEvent.CONNECTION_ESTABLISHED_RESULT, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Session session = (Session) params[0];
						ServerMessage message = (ServerMessage) params[1];
						ConnectionEstablishedResult result = (ConnectionEstablishedResult) params[2];

						event.handle(session, message, result);

						return null;
					}
				});
			}
		});

		eventAttachConnectionRequestValidationOp.ifPresent(new Consumer<EventAttachConnectionRequestValidation>() {

			@Override
			public void accept(EventAttachConnectionRequestValidation event) {
				eventManager.on(ServerEvent.ATTACH_CONNECTION_REQUEST_VALIDATION, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						ServerMessage message = (ServerMessage) params[0];

						return event.handle(message);
					}
				});
			}
		});

		eventAttachedConnectionResultOp.ifPresent(new Consumer<EventAttachedConnectionResult>() {

			@Override
			public void accept(EventAttachedConnectionResult event) {
				eventManager.on(ServerEvent.ATTACHED_CONNECTION_RESULT, new Subscriber() {

					@Override
					public Object dispatch(Object... params) {
						Player player = (Player) params[0];
						AttachedConnectionResult result = (AttachedConnectionResult) params[1];

						event.handle(player, result);

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
					public Object dispatch(Object... params) {
						Session session = (Session) params[0];
						ConnectionDisconnectMode mode = (ConnectionDisconnectMode) params[1];

						event.handle(session, mode);

						return null;
					}
				});
			}
		});

	}

}
