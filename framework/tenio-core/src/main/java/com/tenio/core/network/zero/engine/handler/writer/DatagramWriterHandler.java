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
package com.tenio.core.network.zero.engine.handler.writer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;

/**
 * @author kong
 */
// TODO: Add description
public final class DatagramWriterHandler extends AbstractWriterHandler {

	@Override
	public void send(PacketQueue packetQueue, Session session, Packet packet) throws IOException {
		// retrieve the datagram channel instance from session
		DatagramChannel datagramChannel = session.getDatagramChannel();

		// the InetSocketAddress should be saved when the datagram channel receive first
		// messages from the client
		InetSocketAddress inetSocketAddress = session.getDatagramInetSocketAddress();

		// the datagram need to be declared first, something went wrong here, need to
		// throw an exception
		if (datagramChannel == null) {
			throw new IllegalStateException(
					String.format("UDP Packet cannot be sent to {}, no DatagramChannel was set", session));
		} else if (inetSocketAddress == null) {
			throw new IllegalStateException(
					String.format("UDP Packet cannot be sent to {}, no InetSocketAddress was set", session));
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
		int writtenBytes = datagramChannel.send(getBuffer(), inetSocketAddress);

		// update statistic data
		getNetworkWriterStatistic().updateWrittenBytes(writtenBytes);
		getNetworkWriterStatistic().updateWrittenPackets(1);

		// update statistic data for session
		session.addWrittenBytes(writtenBytes);

		// it is always safe to remove the packet from queue hence it should be sent
		packetQueue.take();

		// if the packet queue still contains more packets then put the session back to
		// the tickets queue
		if (!packetQueue.isEmpty()) {
			getSessionTicketsQueue().add(session);
		}
	}

}
