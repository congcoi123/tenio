package com.tenio.core.network.zero.engine.handler.writer;

import java.util.concurrent.BlockingQueue;

import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.zero.engine.statistic.WriterStatistic;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public final class WebSocketWriterHandler extends AbstractWriterHandler {

	public WebSocketWriterHandler(BlockingQueue<Session> sessionTicketsQueue, WriterStatistic statistic) {
		super(sessionTicketsQueue, statistic);
	}

	@Override
	public void send(PacketQueue packetQueue, Session session, Packet packet) throws Exception {
		Channel channel = session.getWebSocketChannel();

		// this channel can be deactivated by some reasons, no need to throw an
		// exception here
		if (channel == null) {
			_debug("WEBSOCKET CHANNEL SEND", "Skipping this packet, found null socket for session: ", session);
			return;
		}

		// the web-socket channel will send data by packet, so no fragment using here
		byte[] sendingData = packet.getData();

		// send data to client, the result is in the future
		channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(sendingData)));

		// get packet's length
		int writtenBytes = sendingData.length;

		// update the statistic data
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
