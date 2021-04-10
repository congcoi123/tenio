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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.logger.SystemAbstractLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.TransportType;
import com.tenio.core.configuration.entity.SocketConfig;
import com.tenio.core.exception.RefusedAddressException;
import com.tenio.core.message.packet.DefaultPacketQueue;
import com.tenio.core.network.security.IConnectionFilter;
import com.tenio.core.network.zero.DefaultSocketOption;
import com.tenio.core.network.zero.engine.IEngineAcceptor;
import com.tenio.core.network.zero.engine.IEngineReader;
import com.tenio.core.network.zero.handler.IOHandler;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
public final class EngineAcceptor extends SystemAbstractLogger implements IEngineAcceptor, Runnable {

	private volatile int __threadId;

	private ExecutorService __threadPool;
	private List<SocketChannel> __acceptableChannels;
	private Selector __acceptableSelector;

	private IConfiguration __configuration;
	private IEngineReader __engineReader;
	private IConnectionFilter __connectionFilter;
	private IOHandler __ioHandler;

	private int __threadPoolSize;

	public EngineAcceptor() {
		__acceptableChannels = new ArrayList<SocketChannel>();
	}

	@Override
	public void setup() throws UnsupportedOperationException, IOException {

		__initializeWorkers();
		__initializeSockets();

	}

	@Override
	public void start() {

		__runWorkers();

	}

	@SuppressWarnings("unchecked")
	private void __initializeSockets() throws UnsupportedOperationException, IOException {

		__acceptableSelector = Selector.open();

		var serverAddress = __configuration.getString(CoreConfigurationType.SERVER_ADDRESS);
		var socketPorts = (List<SocketConfig>) __configuration.get(CoreConfigurationType.SOCKET_PORTS);

		for (var socketConfig : socketPorts) {
			__bindSocket(serverAddress, socketConfig);
		}
	}

	private void __bindSocket(String serverAddresss, SocketConfig socketConfig)
			throws UnsupportedOperationException, IOException {
		if (socketConfig.getType() == TransportType.TCP) {
			__bindTcpSocket(serverAddresss, socketConfig.getPort());
		} else {
			throw new UnsupportedOperationException("Unsupported transport type");
		}

	}

	private void __bindTcpSocket(String serverAddress, int port) throws IOException {
		ServerSocketChannel socketChannel = ServerSocketChannel.open();

		socketChannel.configureBlocking(false);
		socketChannel.socket().bind(new InetSocketAddress(serverAddress, port));
		socketChannel.socket().setReuseAddress(true);
		socketChannel.register(__acceptableSelector, SelectionKey.OP_ACCEPT);

		_info("TCP SOCKET BOUND", _buildgen("Address: ", serverAddress, ", Port: ", port));
	}

	private void __initializeWorkers() {
		__threadId = 0;
		__threadPoolSize = __configuration.getInt(CoreConfigurationType.NUMBER_ACCEPTOR_WORKER);
		__threadPool = Executors.newFixedThreadPool(__threadPoolSize);
	}

	private void __runWorkers() {

		for (int i = 0; i < __threadPoolSize; i++) {
			__threadPool.execute(this);
		}

	}

	@Override
	public void stop() {

		__shutDownBoundSockets();

		var leftOvers = __threadPool.shutdownNow();

		_info("ENGINE ACCEPTOR", _buildgen("Stopped. Unprocessed workers: ", leftOvers.size()));
	}

	private void __shutDownBoundSockets() {

		Iterator<SelectionKey> selectionKeys = __acceptableSelector.keys().iterator();

		while (selectionKeys.hasNext()) {

			SelectionKey key = selectionKeys.next();

			SelectableChannel channel = key.channel();

			if (channel instanceof SocketChannel) {

				SocketChannel socketChannel = (SocketChannel) channel;
				Socket socket = socketChannel.socket();
				String remoteHost = socket.getRemoteSocketAddress().toString();

				_debug("CLOSING SOCKET", remoteHost);

				try {

					socketChannel.close();

				} catch (IOException e1) {
					_error(e1, "Exception while closing socket");
				}

				key.cancel();
			}
		}

		_debug("CLOSING SELECTOR", "closing");

		try {
			Thread.sleep(500L);
			__acceptableSelector.close();
		} catch (IOException | InterruptedException e2) {
			_error(e2, "Exception while closing selector");
		} finally {
			__acceptableSelector = null;
		}

	}

	@Override
	public void run() {
		__threadId++;

		_info("ENGINE ACCEPTOR", _buildgen("Start worker: ", __threadId));

		Thread.currentThread().setName(StringUtility.strgen("EngineAcceptor-", __threadId));

		__acceptLoop();

		_info("ENGINE ACCEPTOR", _buildgen("Stopping worker: ", __threadId));
	}

	private void __acceptLoop() {
		while (true) {
			try {
				// blocks until at least one channel is ready for the events you registered for
				__acceptableSelector.select();

				Set<SelectionKey> selectedKeys = __acceptableSelector.selectedKeys();

				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {

					SelectionKey selectionKey = keyIterator.next();

					if (selectionKey.isAcceptable()) {
						// a connection was accepted by a ServerSocketChannel
						try {

							ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
							SocketChannel clientChannel = serverChannel.accept();

							if (clientChannel != null) {
								synchronized (__acceptableChannels) {

									_debug("ACCEPTED CLIENT CHANNEL",
											_buildgen("Server address: ",
													serverChannel.socket().getInetAddress().getHostAddress(),
													", Server port: ", serverChannel.socket().getLocalPort()));

									__acceptableChannels.add(clientChannel);

									__ioHandler.channelActive(clientChannel);
								}
							}

						} catch (IOException e) {
							_error(e);
						}
					} else if (selectionKey.isConnectable()) {
						// a connection was established with a remote server

					} else if (selectionKey.isReadable()) {
						// a channel is ready for reading

					} else if (selectionKey.isWritable()) {
						// a channel is ready for writing
					}

					keyIterator.remove();
				}
			} catch (IOException e1) {
				_error(e1);
			}
		}
	}

	@Override
	public void handleAcceptableChannels() {

		if (__acceptableChannels.size() != 0) {
			synchronized (__acceptableChannels) {
				Iterator<SocketChannel> itSocketChannel = __acceptableChannels.iterator();

				while (true) {
					while (itSocketChannel.hasNext()) {
						SocketChannel socketChannel = (SocketChannel) itSocketChannel.next();
						itSocketChannel.remove();
						if (socketChannel == null) {
							_debug("ACCEPTABLE CHANNEL", "Engine Acceptor handle a null socketchannel");
						} else {
							Socket socket = socketChannel.socket();
							if (socket == null) {
								_debug("ACCEPTABLE CHANNEL", "Engine Acceptor handle a null socket");
							} else {
								InetAddress ipAddress = socket.getInetAddress();
								if (ipAddress != null) {
									try {
										__connectionFilter.validateAndAddAddress(ipAddress.getHostAddress());

										socketChannel.configureBlocking(false);
										socketChannel.socket().setTcpNoDelay(false);

										var packetQueue = new DefaultPacketQueue(__configuration
												.getInt(CoreConfigurationType.CHANNEL_PACKET_QUEUE_SIZE));
										socketChannel.setOption(DefaultSocketOption.PACKET_QUEUE, packetQueue);

										socketChannel.register(__engineReader.getSelector(), SelectionKey.OP_READ);

									} catch (RefusedAddressException e1) {
										_error(e1, "Refused connection with address: ", e1.getMessage());

										try {
											socketChannel.socket().shutdownInput();
											socketChannel.socket().shutdownOutput();
											socketChannel.close();
										} catch (IOException e2) {
											_error(e2,
													"Additional problem with refused connection. Was not able to shut down the channel: ",
													e2.getMessage());
										}
									} catch (IOException e3) {
										var logger = _buildgen("Failed accepting connection: ");
										if (socketChannel != null && socketChannel.socket() != null) {
											logger.append(socketChannel.socket().getInetAddress().getHostAddress());
										}

										_error(e3, logger);
									}
								}
							}
						}
					}

					return;
				}
			}
		}
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		__configuration = configuration;
	}

	@Override
	public void setConnectionFilter(IConnectionFilter filter) {
		__connectionFilter = filter;
	}

	@Override
	public void setIoHandler(IOHandler ioHandler) {
		__ioHandler = ioHandler;
	}

	@Override
	public void setEngineReader(IEngineReader engineReader) {
		__engineReader = engineReader;
	}

}
