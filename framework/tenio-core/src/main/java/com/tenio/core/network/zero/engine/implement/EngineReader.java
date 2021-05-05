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
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.logger.ZeroSystemLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.zero.engine.IEngineAcceptor;
import com.tenio.core.network.zero.engine.IEngineReader;
import com.tenio.core.network.zero.engine.IEngineWriter;
import com.tenio.core.network.zero.handler.IOHandler;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
public final class EngineReader extends ZeroSystemLogger implements IEngineReader, Runnable {

	private volatile int __threadId;
	private volatile long __readBytes;

	private ExecutorService __threadPool;
	private Selector __readSelector;

	private IConfiguration __configuration;
	private IEngineAcceptor __engineAcceptor;
	private IEngineWriter __engineWriter;
	private IOHandler __ioHandler;

	private int __threadPoolSize;

	public EngineReader() {
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
					socketChannel = (SocketChannel) selectionKey.channel();
					readBuffer.clear();

					try {
						__readTcpData(socketChannel, selectionKey, readBuffer);
					} catch (IOException e1) {
						this.__closeTcpConnection(socketChannel);
						_error(e1, "Socket closed: ", socketChannel.toString());
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

		if (selectionKey.isWritable()) {
			selectionKey.interestOps(1);
			__engineWriter.continueWriteOp(socketChannel);
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

	@Override
	public void setConfiguration(IConfiguration configuration) {
		__configuration = configuration;
	}

	@Override
	public void setIoHandler(IOHandler ioHandler) {
		__ioHandler = ioHandler;
	}

	@Override
	public void setEngineAcceptor(IEngineAcceptor engineAcceptor) {
		__engineAcceptor = engineAcceptor;
	}

	@Override
	public void setEngineWriter(IEngineWriter engineWriter) {
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

}
