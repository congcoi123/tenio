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
package com.tenio.examples.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.tenio.common.data.implement.ZeroObjectImpl;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.network.entities.packet.implement.PacketImpl;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.entities.session.implement.SessionImpl;
import com.tenio.core.network.zero.codec.compression.DefaultBinaryPacketCompressor;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.DefaultBinaryPacketDecoder;
import com.tenio.core.network.zero.codec.decoder.PacketDecoderResultListener;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encoder.DefaultBinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encryption.DefaultBinaryPacketEncrypter;

/**
 * Create an object for handling a socket connection. It is used to send
 * messages to a server or receive messages from that one.
 */
public final class TCP implements PacketDecoderResultListener {

	private static final int DEFAULT_BYTE_BUFFER_SIZE = 10240;
	private static final String LOCAL_HOST = "localhost";

	private SocketListener __listener;
	private Future<?> __future;
	private Socket __socket;
	private DataOutputStream __out;
	private DataInputStream __in;
	private ByteArrayOutputStream __buffer;
	private Session __session;
	private BinaryPacketEncoder __encoder;
	private BinaryPacketDecoder __decoder;

	/**
	 * Listen in a port on the local machine
	 * 
	 * @param port the desired port
	 */
	public TCP(int port) {
		try {
			__socket = new Socket(LOCAL_HOST, port);
			__out = new DataOutputStream(__socket.getOutputStream());
			__in = new DataInputStream(__socket.getInputStream());
			__buffer = new ByteArrayOutputStream();

			__session = SessionImpl.newInstance();
			__session.createPacketSocketHandle();

			var binaryCompressor = new DefaultBinaryPacketCompressor();
			var binaryEncrypter = new DefaultBinaryPacketEncrypter();

			__encoder = new DefaultBinaryPacketEncoder();
			__encoder.setCompressor(binaryCompressor);
			__encoder.setEncrypter(binaryEncrypter);

			__decoder = new DefaultBinaryPacketDecoder();
			__decoder.setCompressor(binaryCompressor);
			__decoder.setEncrypter(binaryEncrypter);
			__decoder.setResultListener(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message to the server
	 * 
	 * @param message the desired message
	 */
	public void send(ServerMessage message) {
		// convert message object to bytes data
		var packet = PacketImpl.newInstance();
		packet.setData(message.getData().toBinary());
		packet = __encoder.encode(packet);
		// attach the packet's length to packet's header
		var bytes = packet.getData();
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
	 * @param listener
	 */
	public void receive(SocketListener listener) {
		__listener = listener;
		var executorService = Executors.newSingleThreadExecutor();
		__future = executorService.submit(() -> {
			var binary = new byte[DEFAULT_BYTE_BUFFER_SIZE];
			int readBytes = -1;
			try {
				while ((readBytes = __in.read(binary, 0, binary.length)) != -1) {
					__buffer.reset();
					__buffer.write(binary, 0, readBytes);
					__decoder.decode(__session, __buffer.toByteArray());
				}
			} catch (IOException | RuntimeException e) {
				e.printStackTrace();
				return;
			}
		});
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

	@Override
	public void resultFrame(Session session, byte[] binary) {
		var data = ZeroObjectImpl.newInstance(binary);
		__listener.onReceivedTCP(ServerMessage.newInstance().setData(data));
	}

	@Override
	public void updateDroppedPackets(long numberPackets) {
		// do nothing
	}

	@Override
	public void updateReadPackets(long numberPackets) {
		// do nothing
	}

}
