package com.tenio.core.network.zero.engine.handler.writer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;

public final class SocketWriterHandler extends AbstractWriterHandler {

	public SocketWriterHandler(BlockingQueue<Session> sessionTicketsQueue, NetworkWriterStatistic statistic) {
		super(sessionTicketsQueue, statistic);
	}

	@Override
	public void send(PacketQueue packetQueue, Session session, Packet packet) throws IOException {
		SocketChannel channel = session.getSocketChannel();

		// this channel can be deactivated by some reasons, no need to throw an exception here
		if (channel == null) {
			debug("SOCKET CHANNEL SEND", "Skipping this packet, found null socket for session: ", session);
			return;
		}

		// clear the buffer first
		getBuffer().clear();

		// set priority for packet left unsent data (fragment)
		byte[] sendingData = packet.isFragmented() ? packet.getFragmentBuffer() : packet.getData();

		// buffer size is not enough, need to be allocated more bytes
		if (getBuffer().capacity() < sendingData.length) {
			debug("SOCKET CHANNEL SEND", "Allocate new buffer from ", getBuffer().capacity(), " to ",
					sendingData.length, " bytes");
			allocateBuffer(sendingData.length);
		}

		// start to read data to buffer
		getBuffer().put(sendingData);

		// ready to write on socket
		getBuffer().flip();

		// expect to write all data in buffer
		int expectedWrittingBytes = getBuffer().remaining();

		// but it's up to the channel, so it's possible to get left unsent bytes
		int realWrittenBytes = channel.write(getBuffer());

		// update statistic data
		getStatistic().updateWrittenBytes(realWrittenBytes);

		// update statistic data for the session too
		session.addWrittenBytes(realWrittenBytes);

		// the left unwritten bytes should be remain to the queue for next process
		if (realWrittenBytes < expectedWrittingBytes) {
			// create new bytes array to hold the left unsent bytes
			byte[] leftUnwrittenBytes = new byte[getBuffer().remaining()];

			// get bytes array value from buffer
			getBuffer().get(leftUnwrittenBytes);

			// save those bytes to the packet for next manipulation
			packet.setFragmentBuffer(leftUnwrittenBytes);

			// want to know when the socket can write, which should be noticed on
			// isWritable() method
			// when that event occurred, re-add the session to the tickets queue
			SelectionKey selectionKey = session.getSelectionKey();
			if (selectionKey != null && selectionKey.isValid()) {
				selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			} else {
				debug("SOCKET CHANNEL SEND", "Something went wrong with OP_WRITE key for session: ", session);
			}
		} else {
			// update the statistic data
			getStatistic().updateWrittenPackets(1);

			// now the packet can be safely removed
			packetQueue.take();

			// if the packet queue still contains more packets then put the session back to
			// the tickets queue
			if (!packetQueue.isEmpty()) {
				getSessionTicketsQueue().add(session);
			}
		}
	}

}
