/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.examples.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.tenio.entities.element.TObject;
import com.tenio.message.codec.MsgPackConverter;

/**
 * Create an object for handling a Datagram socket connection. It is used to
 * send messages to a server or receive messages from that one.
 * 
 * @author kong
 * 
 */
public class UDP {

	/**
	 * @see Future
	 */
	private Future<?> __future;
	/**
	 * @see DatagramSocket
	 */
	private DatagramSocket __socket;
	/**
	 * @see InetAddress
	 */
	private InetAddress __address;
	/**
	 * The desired port for listening
	 */
	private int __port;

	/**
	 * Listen in a port on the local machine
	 * 
	 * @param port the desired port
	 */
	public UDP(int port) {
		try {
			__socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try {
			__address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		__port = port;
	}

	/**
	 * Send a message to the server
	 * 
	 * @param message the desired message @see {@link TObject}
	 */
	public void send(TObject message) {
		var pack = MsgPackConverter.serialize(message);
		var request = new DatagramPacket(pack, pack.length, __address, __port);
		try {
			__socket.send(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listen for messages that came from the server
	 * 
	 * @param listener @see {@link IDatagramListener}
	 */
	public void receive(IDatagramListener listener) {
		var executorService = Executors.newSingleThreadExecutor();
		__future = executorService.submit(() -> {
			while (true) {
				try {
					byte[] buffer = new byte[10240];
					var response = new DatagramPacket(buffer, buffer.length);
					__socket.receive(response);
					var message = MsgPackConverter.unserialize(buffer);
					listener.onReceivedUDP(message);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		});
	}

	/**
	 * Close this connection
	 */
	public void close() {
		__socket.close();
		__future.cancel(true);
	}

}