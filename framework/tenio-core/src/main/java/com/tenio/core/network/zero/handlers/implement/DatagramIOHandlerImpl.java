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
package com.tenio.core.network.zero.handlers.implement;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

import com.tenio.common.data.utilities.ZeroDataSerializerUtility;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.zero.handlers.DatagramIOHandler;

public final class DatagramIOHandlerImpl extends AbstractIOHandler implements DatagramIOHandler {

	public static DatagramIOHandler newInstance(EventManager eventManager) {
		return new DatagramIOHandlerImpl(eventManager);
	}

	private DatagramIOHandlerImpl(EventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void channelRead(DatagramChannel datagramChannel, SocketAddress remoteAddress, byte[] binary) {
		var data = ZeroDataSerializerUtility.binaryToElement(binary);
		var message = ServerMessage.newInstance().setData(data);
		__eventManager.emit(ServerEvent.DATAGRAM_CHANNEL_READ_MESSAGE, datagramChannel, remoteAddress, message);
	}

	@Override
	public void sessionRead(Session session, byte[] binary) {
		var data = ZeroDataSerializerUtility.binaryToElement(binary);
		var message = ServerMessage.newInstance().setData(data);
		__eventManager.emit(ServerEvent.SESSION_READ_MESSAGE, session, message);
	}

	@Override
	public void channelException(DatagramChannel datagramChannel, Exception exception) {
		// do nothing, the exception was already logged
	}

	@Override
	public void sessionException(Session session, Exception exception) {
		__eventManager.emit(ServerEvent.SESSION_OCCURED_EXCEPTION, session, exception);
	}

}
