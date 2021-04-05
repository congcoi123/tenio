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
package com.tenio.core.network.zero.engine.implement;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.logger.SystemAbstractLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.message.packet.IPacketQueue;
import com.tenio.core.network.zero.DefaultSocketOption;
import com.tenio.core.network.zero.engine.IEngineWriter;
import com.tenio.core.network.zero.handler.IOHandler;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
public final class EngineWriter extends SystemAbstractLogger implements IEngineWriter, Runnable {

	private volatile int __threadId;

	private final BlockingQueue<SocketChannel> __channelTicketsQueue;

	private ExecutorService __threadPool;
	private IConfiguration __configuration;
	private IOHandler __ioHandler;

	private int __threadPoolSize;

	private volatile long __droppedPacketsCount;
	private volatile long __writtenBytes;
	private volatile long __writtenPackets;

	public EngineWriter(int threadPoolSize) {
		__channelTicketsQueue = new LinkedBlockingQueue<SocketChannel>();
		__droppedPacketsCount = 0L;
		__writtenBytes = 0L;
		__writtenPackets = 0L;
	}

	public void setup() throws IOException {

		__initializeWorkers();

	}

	@Override
	public void start() {

		__runWorkers();

	}

	private void __initializeWorkers() {
		__threadId = 0;
		__threadPoolSize = __configuration.getInt(CoreConfigurationType.NUMBER_WRITER_WORKER);
		__threadPool = Executors.newFixedThreadPool(__threadPoolSize);
	}

	private void __runWorkers() {

		for (int i = 0; i < __threadPoolSize; i++) {
			__threadPool.execute(this);
		}

	}

	@Override
	public void stop() {

		var leftOvers = __threadPool.shutdownNow();
		_info("ENGINE ACCEPTOR", _buildgen("Stopped. Unprocessed workers: ", leftOvers.size()));

	}

	@Override
	public void run() {
		var writeBuffer = ByteBuffer.allocate(__configuration.getInt(CoreConfigurationType.WRITE_MAX_BUFFER_SIZE));

		__threadId++;

		_info("ENGINE WRITER", _buildgen("Start worker: ", __threadId));

		Thread.currentThread().setName(StringUtility.strgen("EngineReader-", __threadId));

		__writeLoop(writeBuffer);

		_info("ENGINE WRITER", _buildgen("Stopping worker: ", __threadId));

	}

	private void __writeLoop(ByteBuffer writeBuffer) {
		while (true) {
			try {
				SocketChannel socketChannel = __channelTicketsQueue.take();
				this.processSessionQueue(writeBuffer, socketChannel);
			} catch (InterruptedException e1) {
				_error(e1);
			} catch (Throwable e2) {
				_error(e2);
			}
		}
	}

	private void processSessionQueue(ByteBuffer writeBuffer, SocketChannel socketChannel) {
		if (socketChannel != null) {
			byte[] packet = null;

			try {
				IPacketQueue packetQueue = socketChannel.getOption(DefaultSocketOption.PACKET_QUEUE);
				synchronized (packetQueue) {
					if (packetQueue.isEmpty()) {
						return;
					}

					packet = packetQueue.peek();
					if (packet == null) {
						if (!packetQueue.isEmpty()) {
							packetQueue.take();
						}

						return;
					}

					packet = packetQueue.take();

					__tcpSend(writeBuffer, socketChannel, packet);

					return;
				}
			} catch (ClosedChannelException e1) {
				_error(e1);
			} catch (IOException e2) {
				_error(e2);
			} catch (Exception e3) {
				_error(e3);
			}
		}

	}

	private void __tcpSend(ByteBuffer writeBuffer, SocketChannel socketChannel, byte[] buffer) throws Exception {

		if (socketChannel == null) {
			// do nothing

		} else {
			writeBuffer.clear();

			if (writeBuffer.capacity() < buffer.length) {
				writeBuffer = ByteBuffer.allocate(buffer.length);
			}

			writeBuffer.put(buffer);
			writeBuffer.flip();

			while (writeBuffer.hasRemaining()) {
				long bytesWritten = (long) socketChannel.write(writeBuffer);
				__writtenBytes += bytesWritten;
			}

			__ioHandler.channelWrite(socketChannel, buffer);

		}
	}

	@Override
	public void continueWriteOp(SocketChannel socketChannel) {

		if (socketChannel != null) {
			__channelTicketsQueue.add(socketChannel);
		}

	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		__configuration = configuration;
	}

	@Override
	public void setIoHandler(IOHandler ioHandler) {
		__ioHandler = ioHandler;
	}

	@Override
	public long getDroppedPacketsCount() {
		return __droppedPacketsCount;
	}

	@Override
	public long getWrittenBytes() {
		return __writtenBytes;
	}

	@Override
	public long getWrittenPackets() {
		return __writtenPackets;
	}

}
