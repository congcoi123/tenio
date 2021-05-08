package com.tenio.core.network.zero.engine.handler.writer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;

import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.statistic.NetworkWriterStatistic;

public final class DatagramWriterHandler extends AbstractWriterHandler {

	public DatagramWriterHandler(BlockingQueue<Session> sessionTicketsQueue, NetworkWriterStatistic statistic) {
		super(sessionTicketsQueue, statistic);
	}

	@Override
	public void send(PacketQueue packetQueue, Session session, Packet packet) throws IOException {
		// retrieve the datagram channel instance from session
		DatagramChannel datagramChannel = session.getDatagramChannel();

		// the InetSocketAddress should be saved when the datagram channel receive first
		// messages from the client
		InetSocketAddress inetSocketAddress = session.getClientInetSocketAddress();

		// the datagram need to be declared first, something went wrong here, need to
		// throw an exception
		if (datagramChannel == null) {
			throw new IllegalStateException(
					String.format("UDP Packet cannot be sent to {}, no DatagramChannel set", session));
		} else if (inetSocketAddress == null) {
			throw new IllegalStateException(
					String.format("UDP Packet cannot be sent to {}, no InetSocketAddress set", session));
		}

		// clear the buffer first
		getBuffer().clear();

		// the datagram channel will send data by packet, so no fragment using here
		byte[] sendingData = packet.getData();

		// buffer size is not enough, need to be allocated more bytes
		if (getBuffer().capacity() < sendingData.length) {
			_debug("DATAGRAM CHANNEL SEND", "Allocate new buffer from ", getBuffer().capacity(), " to ",
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
		getStatistic().updateWrittenBytes(writtenBytes);
		getStatistic().updateWrittenPackets(1);

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
