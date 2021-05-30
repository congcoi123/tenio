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
package com.tenio.core.network.zero.engines.writers;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.packet.PacketQueue;
import com.tenio.core.network.entities.session.Session;

public final class DatagramWriterHandler extends AbstractWriterHandler {

	public static DatagramWriterHandler newInstance() {
		return new DatagramWriterHandler();
	}

	private DatagramWriterHandler() {

	}

	@Override
	public void send(PacketQueue packetQueue, Session session, Packet packet) {
		// retrieve the datagram channel instance from session
		DatagramChannel datagramChannel = session.getDatagramChannel();

		// the InetSocketAddress should be saved when the datagram channel receive first
		// messages from the client
		SocketAddress remoteSocketAddress = session.getDatagramRemoteSocketAddress();

		// the datagram need to be declared first, something went wrong here, need to
		// log the exception content
		if (datagramChannel == null) {
			debug("DATAGRAM CHANNEL SEND", "UDP Packet cannot be sent to ", session.toString(),
					", no DatagramChannel was set");
		} else if (remoteSocketAddress == null) {
			debug("DATAGRAM CHANNEL SEND", "UDP Packet cannot be sent to ", session.toString(),
					", no InetSocketAddress was set");
		}

		// clear the buffer first
		getBuffer().clear();

		// the datagram channel will send data by packet, so no fragment using here
		byte[] sendingData = packet.getData();

		// buffer size is not enough, need to be allocated more bytes
		if (getBuffer().capacity() < sendingData.length) {
			debug("DATAGRAM CHANNEL SEND", "Allocate new buffer from ", getBuffer().capacity(), " to ",
					sendingData.length, " bytes");
			allocateBuffer(sendingData.length);
		}

		// put data to buffer
		getBuffer().put(sendingData);

		// ready to send
		getBuffer().flip();

		// send data to the client
		try {
			int writtenBytes = datagramChannel.send(getBuffer(), remoteSocketAddress);
			// update statistic data
			getNetworkWriterStatistic().updateWrittenBytes(writtenBytes);
			getNetworkWriterStatistic().updateWrittenPackets(1);

			// update statistic data for session
			session.addWrittenBytes(writtenBytes);
		} catch (IOException e) {
			error(e, "Error occured in writing on session: ", session.toString());
		}

		// it is always safe to remove the packet from queue hence it should be sent
		packetQueue.take();

		// if the packet queue still contains more packets then put the session back to
		// the tickets queue
		if (!packetQueue.isEmpty()) {
			getSessionTicketsQueue().add(session);
		}
	}

}
