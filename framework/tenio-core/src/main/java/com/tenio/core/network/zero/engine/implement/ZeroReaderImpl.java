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

import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.zero.engine.ZeroReader;
import com.tenio.core.network.zero.engine.listener.ZeroAcceptorListener;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;
import com.tenio.core.network.zero.engine.listener.ZeroWriterListener;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
public final class ZeroReaderImpl extends AbstractZeroEngine implements ZeroReader, ZeroReaderListener {

	private ZeroAcceptorListener __zeroAcceptorListener;
	private ZeroWriterListener __zeroWriterListener;
	private Selector __readSelector;
	private NetworkReaderStatistic __statistic;

	public ZeroReaderImpl(int numberWorkers) {
		super(numberWorkers);
	}

	private void __initializeSelector() throws IOException {
		__readSelector = Selector.open();
	}

	private void __readLoop() {
		var readBuffer = ByteBuffer.allocate(getConfiguration().getInt(CoreConfigurationType.READ_MAX_BUFFER_SIZE));
		while (true) {
			try {
				__zeroAcceptorListener.handleAcceptableChannels();
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
						__readUpdData(datagramChannel, selectionKey, readBuffer);
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
		Session session = getSessionManager().getSessionBySocket(socketChannel);

		if (selectionKey.isWritable()) {
			__zeroWriterListener.continueWriteInterestOp(session);
			selectionKey.interestOps(SelectionKey.OP_READ);
		}

		if (selectionKey.isReadable()) {
			readBuffer.clear();
			long byteCount = 0L;
			byteCount = (long) socketChannel.read(readBuffer);
			if (byteCount == -1L) {
				__closeTcpConnection(socketChannel);
			} else if (byteCount > 0L) {
				session.addReadBytes(byteCount);
				__statistic.updateReadBytes(byteCount);
				readBuffer.flip();
				byte[] binaryData = new byte[readBuffer.limit()];
				readBuffer.get(binaryData);

				getSocketIOHandler().channelRead(socketChannel, binaryData);
			}

		}
	}

	private void __closeTcpConnection(SelectableChannel channel) throws IOException {
		getSocketIOHandler().channelInactive((SocketChannel) channel);
		channel.close();
	}

	private void __readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey, ByteBuffer readBuffer) {
		try {
			Session session = getSessionManager().getSessionByDatagram(datagramChannel.getRemoteAddress().toString());

			if (selectionKey.isWritable()) {
				selectionKey.interestOps(1);
				__zeroWriterListener.continueWriteInterestOp(session);
			}

			if (selectionKey.isReadable()) {
				readBuffer.clear();
				long byteCount = 0L;
				byteCount = (long) datagramChannel.read(readBuffer);
				session.addReadBytes(byteCount);
				__statistic.updateReadBytes(byteCount);
				readBuffer.flip();
				byte[] binaryData = new byte[readBuffer.limit()];
				readBuffer.get(binaryData);

				getDatagramIOHandler().channelRead(datagramChannel, binaryData);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException {
		datagramChannel.register(__readSelector, SelectionKey.OP_READ);
	}

	@Override
	public void acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException {
		socketChannel.register(__readSelector, SelectionKey.OP_READ);
	}

	@Override
	public void setZeroAcceptorListener(ZeroAcceptorListener zeroAcceptorListener) {
		__zeroAcceptorListener = zeroAcceptorListener;
	}

	@Override
	public void setZeroWriter(ZeroWriterListener zeroWriterListener) {
		__zeroWriterListener = zeroWriterListener;
	}

	@Override
	public NetworkReaderStatistic getNetworkReaderStatistic() {
		return __statistic;
	}

	@Override
	public void onSetup() {
		try {
			__initializeSelector();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRun() {
		__readLoop();
	}

	@Override
	public void onStop() {
		try {
			Thread.sleep(500L);
			__readSelector.close();
		} catch (IOException | InterruptedException e2) {
			_error(e2, "Exception while closing selector");
		} finally {
			__readSelector = null;
		}
	}

	@Override
	public String getEngineName() {
		return "reader";
	}

}