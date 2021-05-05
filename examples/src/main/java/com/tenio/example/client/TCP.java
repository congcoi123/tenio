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
package com.tenio.example.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.tenio.common.data.element.CommonObject;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.core.configuration.constant.CoreConstants;
import com.tenio.core.network.utility.MessagePackerUtitlity;

/**
 * Create an object for handling a socket connection. It is used to send
 * messages to a server or receive messages from that one.
 * 
 * @author kong
 * 
 */
public final class TCP {

	private ISocketListener __listener;
	private Future<?> __future;
	private Socket __socket;
	private DataOutputStream __out;
	private DataInputStream __in;

	/**
	 * The size of the received packet
	 */
	private short __dataSize = 0;
	/**
	 * This flag is used to determine how many numbers of received bytes can be used
	 * for one packet's header (that contains the packet's length)
	 */
	private boolean __flagRecvHeader = true;

	/**
	 * Listen in a port on the local machine
	 * 
	 * @param port the desired port
	 */
	public TCP(int port) {
		try {
			__socket = new Socket("localhost", port);
			__out = new DataOutputStream(__socket.getOutputStream());
			__in = new DataInputStream(__socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message to the server
	 * 
	 * @param message the desired message, see {@link CommonObject}
	 */
	public void send(CommonObject message) {
		// convert message object to bytes data
		var pack = MsgPackConverter.serialize(message);
		// attach the packet's length to packet's header
		var bytes = MessagePackerUtitlity.pack(pack);
		try {
			__out.write(bytes);
			__out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listen for messages that came from the server
	 * 
	 * @param listener, see {@link ISocketListener}
	 */
	public void receive(ISocketListener listener) {
		__listener = listener;
		var executorService = Executors.newSingleThreadExecutor();
		__future = executorService.submit(() -> {
			var buffer = new byte[10240];
			try {
				while (__in.read(buffer) > 0) {
					__onRecvData(buffer);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		});
	}

	private void __onRecvData(byte[] bytes) {
		if (bytes.length <= 0) {
			return;
		}

		if (__flagRecvHeader) {
			__updateRecvHeaderData(bytes);
		} else {
			__updateRecvData(bytes);
		}
	}

	private void __updateRecvHeaderData(byte[] bytes) {
		if (bytes.length >= CoreConstants.HEADER_BYTES) { // header length
			var header = Arrays.copyOfRange(bytes, 0, CoreConstants.HEADER_BYTES);
			__dataSize = MessagePackerUtitlity.byteToShort(header); // network to host short
			__flagRecvHeader = false;
			// package = |2 bytes header| <content bytes> |
			var data = Arrays.copyOfRange(bytes, CoreConstants.HEADER_BYTES, __dataSize + CoreConstants.HEADER_BYTES);
			__onRecvData(data); // recursion
		}
	}

	private void __updateRecvData(byte[] bytes) {
		if (bytes.length >= __dataSize) {
			__onRecvMessage(bytes);
			__flagRecvHeader = true; // reset header count
		}
	}

	private void __onRecvMessage(byte[] bytes) {
		// convert a received array of bytes to a message
		var message = MsgPackConverter.unserialize(bytes);
		__listener.onReceivedTCP(message);
	}

	/**
	 * Close this connection
	 */
	public void close() {
		try {
			__socket.close();
			__future.cancel(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
