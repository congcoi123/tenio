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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.RefusedConnectionAddressException;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.engines.ZeroAcceptor;
import com.tenio.core.network.zero.engines.listeners.ZeroAcceptorListener;
import com.tenio.core.network.zero.engines.listeners.ZeroReaderListener;

public final class ZeroAcceptorImpl extends AbstractZeroEngine implements ZeroAcceptor, ZeroAcceptorListener {

	private List<SocketChannel> __acceptableSockets;
	private List<SelectableChannel> __boundSockets;
	private Selector __acceptableSelector;
	private ConnectionFilter __connectionFilter;
	private ZeroReaderListener __zeroReaderListener;
	private String __serverAddress;
	private List<SocketConfig> __socketConfigs;

	public static ZeroAcceptor newInstance(EventManager eventManager) {
		return new ZeroAcceptorImpl(eventManager);
	}

	private ZeroAcceptorImpl(EventManager eventManager) {
		super(eventManager);

		__acceptableSockets = new ArrayList<SocketChannel>();
		__boundSockets = new ArrayList<SelectableChannel>();
		__serverAddress = CoreConstant.LOCAL_HOST;

		setName("acceptor");
	}

	private void __initializeSockets() throws ServiceRuntimeException {
		// opens a selector to handle server socket, udp datagram and accept all
		// incoming
		// client socket
		try {
			__acceptableSelector = Selector.open();
		} catch (IOException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}

		// each socket configuration constructs a server socket or an udp datagram
		for (var socketConfig : __socketConfigs) {
			__bindSocket(socketConfig);
		}
	}

	private void __bindSocket(SocketConfig socketConfig) throws ServiceRuntimeException {
		if (socketConfig.getType() == TransportType.TCP) {
			__bindTcpSocket(socketConfig.getPort());
		} else if (socketConfig.getType() == TransportType.UDP) {
			__bindUdpSocket(socketConfig.getPort());
		}

	}

	private void __bindTcpSocket(int port) throws ServiceRuntimeException {
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(__serverAddress, port));
			serverSocketChannel.socket().setReuseAddress(true);
			// only server socket should interest in this key OP_ACCEPT
			serverSocketChannel.register(__acceptableSelector, SelectionKey.OP_ACCEPT);
			synchronized (__boundSockets) {
				__boundSockets.add(serverSocketChannel);
			}

			info("TCP SOCKET", buildgen("Started at address: ", __serverAddress, ", port: ", port));
		} catch (IOException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}
	}

	private void __bindUdpSocket(int port) throws ServiceRuntimeException {
		try {
			DatagramChannel datagramChannel = DatagramChannel.open();
			datagramChannel.socket().bind(new InetSocketAddress(__serverAddress, port));
			datagramChannel.socket().setReuseAddress(true);
			datagramChannel.configureBlocking(false);
			// udp datagram is a connectionless protocol, we don't need to create
			// bi-direction connection, that why it's not necessary to register it to
			// acceptable selector. Just leave it to the reader selector later
			__zeroReaderListener.acceptDatagramChannel(datagramChannel);
			synchronized (__boundSockets) {
				__boundSockets.add(datagramChannel);
			}

			info("UDP SOCKET", buildgen("Started at address: ", __serverAddress, ", port: ", port));
		} catch (IOException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}
	}

	private void __acceptableLoop() throws IOException {
		// blocks until at least one channel is ready for the events you registered for
		__acceptableSelector.select();

		synchronized (__acceptableSelector) {
			// retrieves a set of selected keys
			Set<SelectionKey> selectedKeys = __acceptableSelector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

			while (keyIterator.hasNext()) {
				// iterates the list of selection keys
				SelectionKey selectionKey = keyIterator.next();
				// once a key is proceeded, it should be removed from the process to prevent
				// duplicating manipulation
				keyIterator.remove();

				// a client socket was accepted by a server socket
				// we only interest in this event
				if (selectionKey.isAcceptable()) {
					try {
						// get the server socket channel from the selector
						ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
						// and accept the incoming request from client
						SocketChannel clientChannel = serverChannel.accept();

						// make sure that the socket is available
						if (clientChannel != null) {
							synchronized (__acceptableSockets) {
								__acceptableSockets.add(clientChannel);
							}
						}

					} catch (IOException e) {
						error(e);
					}
				}
			}
		}

		// wakes up the reader selector for bellow channels handling
		__zeroReaderListener.wakeup();

	}

	private void __shutdownAcceptedSockets() {
		synchronized (__acceptableSockets) {
			// iterates the list of client socket channels
			Iterator<SocketChannel> socketIterator = __acceptableSockets.iterator();

			while (socketIterator.hasNext()) {
				SocketChannel socketChannel = socketIterator.next();
				socketIterator.remove();

				try {
					getSocketIOHandler().channelInactive(socketChannel);
					if (!socketChannel.socket().isClosed()) {
						socketChannel.socket().shutdownInput();
						socketChannel.socket().shutdownOutput();
						socketChannel.close();
					}
				} catch (IOException e) {
					getSocketIOHandler().channelException(socketChannel, e);
					error(e);
				}
			}
		}
	}

	private void __shutdownBoundSockets() {
		synchronized (__boundSockets) {
			// iterates the list of server socket channels, datagram channels
			Iterator<SelectableChannel> boundSocketIterator = __boundSockets.iterator();

			while (boundSocketIterator.hasNext()) {
				SelectableChannel socketChannel = boundSocketIterator.next();
				boundSocketIterator.remove();

				try {
					socketChannel.close();
				} catch (IOException e) {
					error(e);
				}
			}
		}
	}

	private void __shutdownSelector() {
		try {
			Thread.sleep(500L);
			__acceptableSelector.close();
		} catch (IOException | InterruptedException e) {
			error(e);
		}
	}

	private void __cleanup() {
		__boundSockets = null;
		__acceptableSelector = null;
		__acceptableSockets = null;
	}

	@Override
	public void handleAcceptableChannels() {

		// we need to register all new client channel to the reader selector before
		// reading
		synchronized (__acceptableSockets) {
			if (__acceptableSockets.isEmpty()) {
				return;
			}

			Iterator<SocketChannel> socketIterator = __acceptableSockets.iterator();

			while (socketIterator.hasNext()) {
				SocketChannel socketChannel = (SocketChannel) socketIterator.next();
				// makes sure that the loop will never check this item again
				socketIterator.remove();

				if (socketChannel == null) {
					debug("ACCEPTABLE CHANNEL", "Acceptor handle a null socket channel");
				} else {
					Socket socket = socketChannel.socket();

					if (socket == null) {
						debug("ACCEPTABLE CHANNEL", "Acceptor handle a null socket");
					} else {
						InetAddress inetAddress = socket.getInetAddress();
						if (inetAddress != null) {
							try {
								__connectionFilter.validateAndAddAddress(inetAddress.getHostAddress());
								socketChannel.configureBlocking(false);
								socketChannel.socket().setTcpNoDelay(true);
								SelectionKey selectionKey = __zeroReaderListener.acceptSocketChannel(socketChannel);

								getSocketIOHandler().channelActive(socketChannel, selectionKey);
							} catch (RefusedConnectionAddressException e1) {
								getSocketIOHandler().channelException(socketChannel, e1);
								error(e1, "Refused connection with address: ", e1.getMessage());

								try {
									getSocketIOHandler().channelInactive(socketChannel);
									socketChannel.socket().shutdownInput();
									socketChannel.socket().shutdownOutput();
									socketChannel.close();
								} catch (IOException e2) {
									getSocketIOHandler().channelException(socketChannel, e2);
									error(e2,
											"Additional problem with refused connection. Was not able to shut down the channel: ",
											e2.getMessage());
								}
							} catch (IOException e3) {
								getSocketIOHandler().channelException(socketChannel, e3);
								var logger = buildgen("Failed accepting connection: ");
								if (socketChannel != null && socketChannel.socket() != null) {
									logger.append(socketChannel.socket().getInetAddress().getHostAddress());
								}

								error(e3, logger);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void setConnectionFilter(ConnectionFilter filter) {
		__connectionFilter = filter;
	}

	@Override
	public void setServerAddress(String serverAddress) {
		__serverAddress = serverAddress;
	}

	@Override
	public void setSocketConfigs(List<SocketConfig> socketConfigs) {
		__socketConfigs = socketConfigs;
	}

	@Override
	public void setZeroReaderListener(ZeroReaderListener zeroReaderListener) {
		__zeroReaderListener = zeroReaderListener;
	}

	@Override
	public void onInitialized() {
		__initializeSockets();
	}

	@Override
	public void onStarted() {
		// do nothing
	}

	@Override
	public void onRunning() {
		while (true) {
			if (isActivated()) {
				try {
					__acceptableLoop();
				} catch (IOException e) {
					error(e, e.getMessage());
				}
			}
		}
	}

	@Override
	public void onShutdown() {
		__shutdownBoundSockets();
		__shutdownAcceptedSockets();
		__shutdownSelector();
	}

	@Override
	public void onDestroyed() {
		__cleanup();
	}

}
