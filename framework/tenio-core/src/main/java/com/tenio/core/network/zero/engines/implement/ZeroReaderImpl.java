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
package com.tenio.core.network.zero.engines.implement;

import java.io.IOException;
import java.net.SocketAddress;
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

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.zero.engines.ZeroReader;
import com.tenio.core.network.zero.engines.listeners.ZeroAcceptorListener;
import com.tenio.core.network.zero.engines.listeners.ZeroReaderListener;
import com.tenio.core.network.zero.engines.listeners.ZeroWriterListener;

import javassist.NotFoundException;

public final class ZeroReaderImpl extends AbstractZeroEngine implements ZeroReader, ZeroReaderListener {

	private ZeroAcceptorListener __zeroAcceptorListener;
	private ZeroWriterListener __zeroWriterListener;
	private Selector __readableSelector;
	private NetworkReaderStatistic __networkReaderStatistic;

	public static ZeroReader newInstance(EventManager eventManager) {
		return new ZeroReaderImpl(eventManager);
	}

	private ZeroReaderImpl(EventManager eventManager) {
		super(eventManager);

		setName("reader");
	}

	private void __initializeSelector() throws ServiceRuntimeException {
		try {
			__readableSelector = Selector.open();
		} catch (IOException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}
	}

	private void __readableLoop(ByteBuffer readerBuffer) {
		__zeroAcceptorListener.handleAcceptableChannels();
		__readIncomingSocketData(readerBuffer);
	}

	private void __readIncomingSocketData(ByteBuffer readerBuffer) {
		SocketChannel socketChannel = null;
		DatagramChannel datagramChannel = null;
		SelectionKey selectionKey = null;

		try {
			// blocks until at least one channel is ready for the events you registered for
			int countReadyKeys = __readableSelector.selectNow();

			if (countReadyKeys == 0) {
				return;
			}

			synchronized (__readableSelector) {
				// readable selector was registered by OP_READ interested only socket channels,
				// but in some cases, we can received "can writable" signal from those sockets
				Set<SelectionKey> readyKeys = __readableSelector.selectedKeys();
				Iterator<SelectionKey> keyIterator = readyKeys.iterator();

				while (keyIterator.hasNext()) {
					selectionKey = keyIterator.next();
					// once a key is proceeded, it should be removed from the process to prevent
					// duplicating manipulation
					keyIterator.remove();

					if (selectionKey.isValid()) {
						SelectableChannel channel = selectionKey.channel();
						// we already registered 2 types of channels for this selector and need to
						// separate the processes
						if (channel instanceof SocketChannel) {
							socketChannel = (SocketChannel) channel;
							__readTcpData(socketChannel, selectionKey, readerBuffer);
						} else if (channel instanceof DatagramChannel) {
							datagramChannel = (DatagramChannel) channel;
							__readUpdData(datagramChannel, selectionKey, readerBuffer);
						}
					}

				}
			}

		} catch (ClosedSelectorException e1) {
			error(e1, "Selector is closed: ", e1.getMessage());
		} catch (CancelledKeyException e2) {
			error(e2, "Cancelled key: ", e2.getMessage());
		} catch (IOException e3) {
			error(e3, "I/O reading/selection error: ", e3.getMessage());
		} catch (Exception e4) {
			error(e4, "Generic reading/selection error: ", e4.getMessage());
		}

	}

	private void __readTcpData(SocketChannel socketChannel, SelectionKey selectionKey, ByteBuffer readerBuffer) {
		// retrieves session by its socket channel
		Session session = getSessionManager().getSessionBySocket(socketChannel);

		if (session == null) {
			debug("READ CHANNEL", "Reader handle a null session with the socket channel: ", socketChannel.toString());
			return;
		}

		// when a socket channel is writable, should make it highest priority
		// manipulation
		if (selectionKey.isWritable()) {
			// should continue put this session for sending all left packets first
			__zeroWriterListener.continueWriteInterestOp(session);
			// now we should set it back to interest in OP_READ
			selectionKey.interestOps(SelectionKey.OP_READ);
		}

		if (selectionKey.isReadable()) {
			// prepares the buffer first
			readerBuffer.clear();
			// reads data from socket and write them to buffer
			int byteCount = -1;
			try {
				byteCount = socketChannel.read(readerBuffer);
			} catch (IOException e) {
				error(e, "An exception was occured on channel: ", socketChannel.toString());
				getSocketIOHandler().sessionException(session, e);
			}
			// no left data is available, should close the connection
			if (byteCount == -1) {
				__closeTcpConnection(socketChannel);
			} else if (byteCount > 0) {
				// update statistic data
				session.addReadBytes(byteCount);
				__networkReaderStatistic.updateReadBytes(byteCount);
				// ready to read data from buffer
				readerBuffer.flip();
				// reads data from buffer and transfers them to the next process
				byte[] binary = new byte[readerBuffer.limit()];
				readerBuffer.get(binary);

				getSocketIOHandler().sessionRead(session, binary);
			}

		}
	}

	private void __closeTcpConnection(SelectableChannel channel) {
		SocketChannel socketChannel = (SocketChannel) channel;
		getSocketIOHandler().channelInactive(socketChannel);
		if (!socketChannel.socket().isClosed()) {
			try {
				socketChannel.socket().shutdownInput();
				socketChannel.socket().shutdownOutput();
				socketChannel.close();
			} catch (IOException e) {
				error(e, "Error on closing socket channel: ", socketChannel.toString());
			}
		}

	}

	private void __readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey, ByteBuffer readerBuffer) {

		Session session = null;

		if (selectionKey.isReadable()) {
			// prepares the buffer first
			readerBuffer.clear();
			// reads data from socket and write them to buffer
			SocketAddress remoteAddress = null;
			try {
				remoteAddress = datagramChannel.receive(readerBuffer);
			} catch (IOException e) {
				error(e, "An exception was occured on channel: ", datagramChannel.toString());
				getDatagramIOHandler().channelException(datagramChannel, e);
			}

			if (remoteAddress == null) {
				var addressNotFoundException = new NotFoundException("Remove addess for the datagram channel");
				error(addressNotFoundException, "An exception was occured on channel: ", datagramChannel.toString());
				getDatagramIOHandler().channelException(datagramChannel, addressNotFoundException);
				return;
			}

			int byteCount = readerBuffer.position();

			// update statistic data
			__networkReaderStatistic.updateReadBytes(byteCount);
			// ready to read data from buffer
			readerBuffer.flip();
			// reads data from buffer and transfers them to the next process
			byte[] binary = new byte[readerBuffer.limit()];
			readerBuffer.get(binary);

			// retrieves session by its datagram channel, hence we are using only one
			// datagram channel for all sessions, we use incoming request remote address to
			// distinguish them
			session = getSessionManager().getSessionByDatagram(remoteAddress);

			if (session == null) {
				getDatagramIOHandler().channelRead(datagramChannel, remoteAddress, binary);
			} else {
				session.addReadBytes(byteCount);
				getDatagramIOHandler().sessionRead(session, binary);
			}
		}

		if (selectionKey.isWritable() && session != null) {
			// should continue put this session for sending all left packets first
			__zeroWriterListener.continueWriteInterestOp(session);
			// now we should set it back to interest in OP_READ
			selectionKey.interestOps(SelectionKey.OP_READ);
		}

	}

	@Override
	public void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException {
		datagramChannel.register(__readableSelector, SelectionKey.OP_READ);
	}

	@Override
	public SelectionKey acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException {
		return socketChannel.register(__readableSelector, SelectionKey.OP_READ);
	}

	@Override
	public void setZeroAcceptorListener(ZeroAcceptorListener zeroAcceptorListener) {
		__zeroAcceptorListener = zeroAcceptorListener;
	}

	@Override
	public void setZeroWriterListener(ZeroWriterListener zeroWriterListener) {
		__zeroWriterListener = zeroWriterListener;
	}

	@Override
	public NetworkReaderStatistic getNetworkReaderStatistic() {
		return __networkReaderStatistic;
	}

	@Override
	public void wakeup() {
		if (isActivated()) {
			__readableSelector.wakeup();
		}
	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
		__networkReaderStatistic = networkReaderStatistic;
	}

	@Override
	public void onInitialized() {
		__initializeSelector();
	}

	@Override
	public void onStarted() {
		// do nothing
	}

	@Override
	public void onRunning() {
		ByteBuffer readerBuffer = ByteBuffer.allocate(getMaxBufferSize());

		while (true) {
			if (isActivated()) {
				__readableLoop(readerBuffer);
			}
		}
	}

	@Override
	public void onShutdown() {
		try {
			Thread.sleep(500L);
			__readableSelector.close();
		} catch (IOException | InterruptedException e) {
			error(e, "Exception while closing the selector");
		}
	}

	@Override
	public void onDestroyed() {
		__readableSelector = null;
	}

}
