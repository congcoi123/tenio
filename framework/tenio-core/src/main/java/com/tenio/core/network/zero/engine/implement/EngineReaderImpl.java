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
package com.tenio.core.network.zero.engine.implement;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.zero.engine.ZeroEngineAcceptor;
import com.tenio.core.network.zero.engine.ZeroEngineReaderListener;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.zero.engine.EngineReader;
import com.tenio.core.network.zero.engine.EngineWriter;
import com.tenio.core.network.zero.handler.SocketIOHandler;
import com.tenio.core.network.zero.option.ZeroConnectionOption;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
public final class EngineReaderImpl extends AbstractZeroEngine implements ZeroEngineReaderListener, EngineReader {

	private volatile int __threadId;
	private volatile long __readBytes;

	private ExecutorService __threadPool;
	private Selector __readSelector;

	private Configuration __configuration;
	private ZeroEngineAcceptor __engineAcceptor;
	private EngineWriter __engineWriter;
	private SocketIOHandler __ioHandler;

	private int __threadPoolSize;

	public EngineReaderImpl() {
		__readBytes = 0;
	}

	public void setup() throws IOException {

		__initializeWorkers();
		__initializeSelector();

	}

	@Override
	public void start() {

		__runWorkers();

	}

	private void __initializeSelector() throws IOException {
		__readSelector = Selector.open();
	}

	private void __initializeWorkers() {
		__threadId = 0;
		__threadPoolSize = __configuration.getInt(CoreConfigurationType.NUMBER_READER_WORKER);
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

		try {
			Thread.sleep(500L);
			__readSelector.close();
		} catch (IOException | InterruptedException e2) {
			_error(e2, "Exception while closing selector");
		} finally {
			__readSelector = null;
		}

		_info("ENGINE ACCEPTOR", _buildgen("Stopped. Unprocessed workers: ", leftOvers.size()));

	}

	@Override
	public void run() {
		var readBuffer = ByteBuffer.allocate(__configuration.getInt(CoreConfigurationType.READ_MAX_BUFFER_SIZE));

		__threadId++;

		_info("ENGINE READER", _buildgen("Start worker: ", __threadId));

		Thread.currentThread().setName(StringUtility.strgen("EngineReader-", __threadId));

		__readLoop(readBuffer);

		_info("ENGINE READER", _buildgen("Stopping worker: ", __threadId));
	}

	private void __readLoop(ByteBuffer readBuffer) {
		while (true) {
			try {
				__engineAcceptor.handleAcceptableChannels();
				__readIncomingSocketData(readBuffer);
				Thread.sleep(5L);
			} catch (InterruptedException e) {
				_error(e);
			}
		}
	}

	private void __readIncomingSocketData(ByteBuffer readBuffer) {
		SocketChannel socketChannel = null;
		DatagramChannel datagramChannel = null;
		SelectionKey selectionKey = null;

		try {
			int readyKeyCount = __readSelector.selectNow();

			if (readyKeyCount == 0) {
				return;
			}

			Set<SelectionKey> readyKeys = __readSelector.selectedKeys();
			Iterator<SelectionKey> keyIterators = readyKeys.iterator();

			while (keyIterators.hasNext()) {
				selectionKey = (SelectionKey) keyIterators.next();

				if (selectionKey.isValid()) {
					SelectableChannel channel = selectionKey.channel();
					if (channel instanceof SocketChannel) {
						socketChannel = (SocketChannel) channel;

						try {
							__readTcpData(socketChannel, selectionKey, readBuffer);
						} catch (IOException e1) {
							this.__closeTcpConnection(socketChannel);
							_error(e1, "Socket closed: ", socketChannel.toString());
						}

					} else if (channel instanceof DatagramChannel) {
						datagramChannel = (DatagramChannel) channel;

						
					}
				}

				keyIterators.remove();
			}

		} catch (ClosedSelectorException e2) {
			_error(e2, "Selector is closed!");
		} catch (CancelledKeyException e3) {
			_error(e3, "Cancelled key");
		} catch (IOException e4) {
			_error(e4, "I/O reading/selection error: ", e4.getMessage());
		} catch (Exception e5) {
			_error(e5, "Generic reading/selection error: ", e5.getMessage());
		}

	}

	private void __readTcpData(SocketChannel socketChannel, SelectionKey selectionKey, ByteBuffer readBuffer)
			throws IOException {
		Session session = getSessionManager().getLocalSessionByConnection(socketChannel);

		if (selectionKey.isWritable()) {
			selectionKey.interestOps(1);
			__engineWriter.continueWriteOp(session);
		}

		if (selectionKey.isReadable()) {
			readBuffer.clear();
			long byteCount = 0L;
			byteCount = (long) socketChannel.read(readBuffer);
			if (byteCount == -1L) {
				this.__closeTcpConnection(socketChannel);
			} else if (byteCount > 0L) {
				__readBytes += byteCount;
				readBuffer.flip();
				byte[] binaryData = new byte[readBuffer.limit()];
				readBuffer.get(binaryData);

				__ioHandler.channelRead(socketChannel, binaryData);
			}

		}
	}

	private void __closeTcpConnection(SelectableChannel channel) throws IOException {

		channel.close();

		if (channel instanceof SocketChannel) {
			__ioHandler.channelInactive((SocketChannel) channel);
		}

	}
	
	private void __readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey, ByteBuffer readBuffer) {
		Session session = getSessionManager().getLocalSessionAddress(datagramChannel.getRemoteAddress().toString());
		
		if (selectionKey.isWritable()) {
			selectionKey.interestOps(1);
			__engineWriter.continueWriteOp(session);
		}

		if (selectionKey.isReadable()) {
			readBuffer.clear();
			long byteCount = 0L;
			byteCount = (long) datagramChannel.read(readBuffer);
				__readBytes += byteCount;
				readBuffer.flip();
				byte[] binaryData = new byte[readBuffer.limit()];
				readBuffer.get(binaryData);

				__ioHandler.channelRead(datagramChannel, binaryData);
			}

		}
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		__configuration = configuration;
	}

	@Override
	public void setIoHandler(SocketIOHandler ioHandler) {
		__ioHandler = ioHandler;
	}

	@Override
	public void setEngineAcceptor(ZeroEngineAcceptor engineAcceptor) {
		__engineAcceptor = engineAcceptor;
	}

	@Override
	public void setEngineWriter(EngineWriter engineWriter) {
		__engineWriter = engineWriter;
	}

	@Override
	public Selector getSelector() {
		return __readSelector;
	}

	@Override
	public long getReadBytes() {
		return __readBytes;
	}

	@Override
	public void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException {
		datagramChannel.register(__readSelector, SelectionKey.OP_READ);
	}

	@Override
	public void acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException {
		socketChannel.register(__readSelector, SelectionKey.OP_READ);
	}

}
