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

import com.tenio.core.configuration.defines.InternalEvent;
import com.tenio.core.controller.AbstractController;
import com.tenio.core.events.EventManager;
import com.tenio.core.network.defines.RequestPriority;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.entities.protocols.Request;
import com.tenio.core.network.entities.protocols.implement.RequestImpl;
import com.tenio.core.network.entities.session.Session;

/**
 * Handle the main logic of the server.
 * 
 * @author kong
 */
public final class InternalProcessorService extends AbstractController {

	private InternalProcessorService(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void onInitialized() {
		__getInternalEvent().on(InternalEvent.SERVER_STARTED, args -> {
			start();
			return null;
		});
		__getInternalEvent().on(InternalEvent.SESSION_WAS_CREATED, args -> {
			Request request = RequestImpl.newInstance();
			request.setEvent(InternalEvent.SESSION_WAS_CREATED);
			request.setPriority(RequestPriority.NORMAL);
			request.setTransportType((TransportType) args[0]);
			request.setSender((Session) args[1]);

			enqueueRequest(request);

			return null;
		});
		__getInternalEvent().on(InternalEvent.SESSION_REQUEST_CONNECTION, args -> {
			Request request = RequestImpl.newInstance();
			request.setEvent(InternalEvent.SESSION_REQUEST_CONNECTION);
			request.setPriority(RequestPriority.NORMAL);
			request.setTransportType((TransportType) args[0]);
			request.setSender((Session) args[1]);
			request.setAttribute("message", args[2]);

			enqueueRequest(request);

			return null;
		});
		__getInternalEvent().on(InternalEvent.SESSION_OCCURED_EXCEPTION, args -> {
			Request request = RequestImpl.newInstance();
			request.setEvent(InternalEvent.SESSION_REQUEST_CONNECTION);
			request.setPriority(RequestPriority.NORMAL);
			request.setTransportType((TransportType) args[0]);
			request.setSender((Session) args[1]);
			request.setAttribute("message", args[2]);

			enqueueRequest(request);

			return null;
		});
		__getInternalEvent().on(InternalEvent.SESSION_WAS_CLOSED, args -> {

			return null;
		});
		__getInternalEvent().on(InternalEvent.SESSION_READ_BINARY, args -> {
			Request request = RequestImpl.newInstance();
			request.setEvent(InternalEvent.SESSION_READ_BINARY);
			request.setPriority(RequestPriority.NORMAL);
			request.setTransportType((TransportType) args[0]);
			request.setSender((Session) args[1]);
			request.setContent((byte[]) args[2]);

			enqueueRequest(request);

			return null;
		});
		__getInternalEvent().on(InternalEvent.DATAGRAM_CHANNEL_READ_BINARY, args -> {
			Request request = RequestImpl.newInstance();
			request.setEvent(InternalEvent.DATAGRAM_CHANNEL_READ_BINARY);
			request.setPriority(RequestPriority.NORMAL);
			request.setTransportType((TransportType) args[0]);
			request.setContent((byte[]) args[2]);
			request.setAttribute("datgram", args[1]);

			enqueueRequest(request);

			return null;
		});
		__getInternalEvent().on(InternalEvent.FORCE_PLAYER_TO_LEAVE_ROOM, args -> {

			return null;
		});
	}

	@Override
	public void onStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResumed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPaused() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHalted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroyed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processRequest(Request request) throws Exception {
		// TODO Auto-generated method stub

	}

}
