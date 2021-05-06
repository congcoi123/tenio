package com.tenio.core.network.zero.engine.handler.writer;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.zero.engine.statistic.WriterStatistic;

public abstract class AbstractWriterHandler extends SystemLogger implements WriterHandler {

	private static final int BUFFER_SIZE = 32768;

	private final BlockingQueue<Session> __sessionTicketsQueue;
	private final WriterStatistic __statistic;
	private ByteBuffer __buffer;

	public AbstractWriterHandler(BlockingQueue<Session> sessionTicketsQueue, WriterStatistic statistic) {
		__sessionTicketsQueue = sessionTicketsQueue;
		__statistic = statistic;
		allocateBuffer(BUFFER_SIZE);
	}

	public BlockingQueue<Session> getSessionTicketsQueue() {
		return __sessionTicketsQueue;
	}

	public WriterStatistic getStatistic() {
		return __statistic;
	}

	public ByteBuffer getBuffer() {
		return __buffer;
	}

	public void allocateBuffer(int capacity) {
		__buffer = ByteBuffer.allocate(capacity);
	}

}
