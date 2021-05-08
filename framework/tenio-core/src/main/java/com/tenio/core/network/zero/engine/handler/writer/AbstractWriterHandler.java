package com.tenio.core.network.zero.engine.handler.writer;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;

public abstract class AbstractWriterHandler extends SystemLogger implements WriterHandler {

	private static final int BUFFER_SIZE = 32768;

	private final BlockingQueue<Session> __sessionTicketsQueue;
	private final NetworkWriterStatistic __statistic;
	private ByteBuffer __buffer;

	public AbstractWriterHandler(BlockingQueue<Session> sessionTicketsQueue, NetworkWriterStatistic statistic) {
		__sessionTicketsQueue = sessionTicketsQueue;
		__statistic = statistic;
		allocateBuffer(BUFFER_SIZE);
	}

	public BlockingQueue<Session> getSessionTicketsQueue() {
		return __sessionTicketsQueue;
	}

	public NetworkWriterStatistic getStatistic() {
		return __statistic;
	}

	public ByteBuffer getBuffer() {
		return __buffer;
	}

	public void allocateBuffer(int capacity) {
		__buffer = ByteBuffer.allocate(capacity);
	}

}
