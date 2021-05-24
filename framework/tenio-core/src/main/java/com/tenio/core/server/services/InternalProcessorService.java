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
package com.tenio.core.server.services;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

import com.tenio.core.configuration.defines.CoreMessageCode;
import com.tenio.core.configuration.defines.ExtensionEvent;
import com.tenio.core.configuration.defines.InternalEvent;
import com.tenio.core.controller.AbstractController;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.events.EventManager;
import com.tenio.core.network.entities.protocols.Request;
import com.tenio.core.network.entities.protocols.implement.RequestImpl;
import com.tenio.core.network.entities.session.Session;

/**
 * Handle the main logic of the server.
 * 
 * @author kong
 */
public final class InternalProcessorService extends AbstractController {

	private static final String EVENT_KEY_BINARY = "binary";
	private static final String EVENT_KEY_EXCEPTION = "exception";
	private static final String EVENT_KEY_DATAGRAM = "datagram";

	private PlayerManager __playerManager;
	private int __maxNumberPlayers;

	public static InternalProcessorService newInstance(EventManager eventManager) {
		return new InternalProcessorService(eventManager);
	}

	private InternalProcessorService(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void subscribe() {
		__getInternalEvent().on(InternalEvent.SERVER_STARTED, params -> {
			start();
			return null;
		});

		__getInternalEvent().on(InternalEvent.SESSION_WAS_CREATED, params -> {
			// Request request = __createRequest(InternalEvent.SESSION_WAS_CREATED,
			// (Session) params[0]);
			// enqueueRequest(request);

			return null;
		});

		__getInternalEvent().on(InternalEvent.SESSION_REQUESTS_CONNECTION, params -> {
			Request request = __createRequest(InternalEvent.SESSION_REQUESTS_CONNECTION, (Session) params[0]);
			request.setContent((byte[]) params[1]);
			enqueueRequest(request);

			return null;
		});

		__getInternalEvent().on(InternalEvent.SESSION_OCCURED_EXCEPTION, params -> {
			// Request request = __createRequest(InternalEvent.SESSION_OCCURED_EXCEPTION,
			// (Session) params[0]);
			// request.setAttribute(EVENT_KEY_EXCEPTION, params[1]);
			// enqueueRequest(request);

			return null;
		});

		__getInternalEvent().on(InternalEvent.SESSION_IS_CLOSED, params -> {
			Request request = __createRequest(InternalEvent.SESSION_OCCURED_EXCEPTION, (Session) params[0]);
			enqueueRequest(request);

			return null;
		});

		__getInternalEvent().on(InternalEvent.SESSION_READ_BINARY, params -> {
			Request request = __createRequest(InternalEvent.SESSION_REQUESTS_CONNECTION, (Session) params[0]);
			request.setContent((byte[]) params[1]);
			enqueueRequest(request);

			return null;
		});

		__getInternalEvent().on(InternalEvent.DATAGRAM_CHANNEL_READ_BINARY, params -> {
			Request request = __createRequest(InternalEvent.SESSION_REQUESTS_CONNECTION, null);
			request.setAttribute(EVENT_KEY_DATAGRAM, params[0]);
			request.setContent((byte[]) params[1]);
			enqueueRequest(request);

			return null;
		});
	}

	private Request __createRequest(InternalEvent event, Session session) {
		Request request = RequestImpl.newInstance().setEvent(event).setSender(session);
		return request;
	}

	@Override
	public void processRequest(Request request) {
		switch (request.getEvent()) {
		case SESSION_REQUESTS_CONNECTION:
			__processSessionRequestsConnection(request);
			break;

		case SESSION_IS_CLOSED:
			__processSessionIsClosed(request);
			break;

		case SESSION_READ_BINARY:
			__processSessionReadBinary(request);
			break;

		case DATAGRAM_CHANNEL_READ_BINARY:
			__processDatagramChannelReadBinary(request);
			break;

		default:
			break;
		}
	}

	private void __processSessionRequestsConnection(Request request) {
		// check if it's reconnection request first
		var session = request.getSender();
		var message = request.getContent();

		var player = (Player) __getExtensionEvent().emit(ExtensionEvent.PLAYER_RECONNECTING_REQUEST_HANDLE, session,
				message);
		if (player != null) {
			if (!(player instanceof Player)) {
				// FIXME: throws an exception
				__getExtensionEvent().emit(ExtensionEvent.EXCEPTION, new ClassCastException(
						String.format("Unable to cast the object: %s to class Player", player.toString())));
			} else {
				session.setName(player.getName());
				player.setSession(session);
				__eventManager.getExtension().emit(ExtensionEvent.PLAYER_RECONNECTED_SUCCESSFULLY, player);
			}
		} else {
			// check the number of current players
			if (__playerManager.getPlayerCount() >= __maxNumberPlayers) {
				__eventManager.getExtension().emit(ExtensionEvent.CONNECTION_ESTABLISHED_FAILED, session,
						CoreMessageCode.REACHED_MAX_CONNECTION);
				try {
					session.close();
				} catch (IOException e) {
					error(e, "Session: %s", session.toString());
				}
			} else {
				__eventManager.getExtension().emit(ExtensionEvent.CONNECTION_ESTABLISHED_SUCCESSFULLY, session,
						message);
			}
		}

	}

	private void __processSessionIsClosed(Request request) {

	}

	private void __processSessionReadBinary(Request request) {

	}

	private void __processDatagramChannelReadBinary(Request request) {
		var datagramChannel = request.getAttribute(EVENT_KEY_DATAGRAM);
		var message = request.getContent();

		// the condition for creating sub-connection
		var player = (Player) __eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_REQUEST_VALIDATE,
				message);

		if (player == null) {
			__eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_FAILED, message,
					CoreMessageCode.PLAYER_NOT_FOUND);
		} else if (!player.containsSession()) {
			__eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_FAILED, message,
					CoreMessageCode.MAIN_CONNECTION_NOT_FOUND);
		} else {
			player.getSession().setDatagramChannel((DatagramChannel) datagramChannel);
			__eventManager.getExtension().emit(ExtensionEvent.ATTACH_CONNECTION_SUCCESSFULLY, player);
		}
	}

	public void setMaxNumberPlayers(int maxPlayers) {
		__maxNumberPlayers = maxPlayers;
	}

	public void setPlayerManager(PlayerManager playerManager) {
		__playerManager = playerManager;
	}

}
