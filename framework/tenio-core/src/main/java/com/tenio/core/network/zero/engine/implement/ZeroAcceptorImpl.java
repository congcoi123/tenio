package com.tenio.core.network.zero.engine.implement;

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

import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.exception.RefusedAddressException;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.define.data.SocketConfig;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.engine.ZeroAcceptor;
import com.tenio.core.network.zero.engine.listener.ZeroAcceptorListener;
import com.tenio.core.network.zero.engine.listener.ZeroReaderListener;

public final class ZeroAcceptorImpl extends AbstractZeroEngine implements ZeroAcceptor, ZeroAcceptorListener {

	private final List<SocketChannel> __acceptableChannels;
	private Selector __acceptableSelector;
	private ConnectionFilter __connectionFilter;
	private ZeroReaderListener __zeroReaderListener;

	public ZeroAcceptorImpl(int numberWorkers) {
		super(numberWorkers);
		__acceptableChannels = new ArrayList<SocketChannel>();
	}

	@SuppressWarnings("unchecked")
	private void __initializeSockets() throws IOException {

		__acceptableSelector = Selector.open();

		var serverAddress = getConfiguration().getString(CoreConfigurationType.SERVER_ADDRESS);
		var socketPorts = (List<SocketConfig>) getConfiguration().get(CoreConfigurationType.SOCKET_PORTS);

		for (var socketConfig : socketPorts) {
			__bindSocket(serverAddress, socketConfig);
		}
	}

	private void __bindSocket(String serverAddresss, SocketConfig socketConfig) {
		if (socketConfig.getType() == TransportType.TCP) {
			__bindTcpSocket(serverAddresss, socketConfig.getPort());
		} else if (socketConfig.getType() == TransportType.UDP) {
			__bindUdpSocket(serverAddresss, socketConfig.getPort());
		}

	}

	private void __bindTcpSocket(String serverAddress, int port) {
		ServerSocketChannel socketChannel;
		try {
			socketChannel = ServerSocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.socket().bind(new InetSocketAddress(serverAddress, port));
			socketChannel.socket().setReuseAddress(true);
			socketChannel.register(__acceptableSelector, SelectionKey.OP_ACCEPT);

			_info("TCP SOCKET BOUND", _buildgen("Address: ", serverAddress, ", Port: ", port));
		} catch (IOException e) {
			_error(e);
		}
	}

	private void __bindUdpSocket(String serverAddress, int port) {
		DatagramChannel datagramChannel;
		try {
			datagramChannel = DatagramChannel.open();
			datagramChannel.configureBlocking(false);
			datagramChannel.socket().bind(new InetSocketAddress(serverAddress, port));
			datagramChannel.socket().setReuseAddress(true);
			__zeroReaderListener.acceptDatagramChannel(datagramChannel);

			_info("UDP SOCKET BOUND", _buildgen("Address: ", serverAddress, ", Port: ", port));
		} catch (IOException e) {
			_error(e);
		}
	}

	private void __acceptLoop() {
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

								__acceptableChannels.add(clientChannel);
								getSocketIOHandler().channelActive(clientChannel, selectionKey);

								_debug("ACCEPTED CLIENT CHANNEL",
										_buildgen("Server address: ",
												serverChannel.socket().getInetAddress().getHostAddress(),
												", Server port: ", serverChannel.socket().getLocalPort()));
							}
						}

					} catch (IOException e) {
						_error(e);
					}
				}

				keyIterator.remove();
			}
		} catch (IOException e1) {
			_error(e1);
		}
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
									socketChannel.configureBlocking(false);
									socketChannel.socket().setTcpNoDelay(false);
									Session session;
									
									try {
										__connectionFilter.validateAndAddAddress(ipAddress.getHostAddress());

										socketChannel.configureBlocking(false);
										socketChannel.socket().setTcpNoDelay(false);
										
										__zeroReaderListener.acceptSocketChannel(socketChannel);

									} catch (RefusedAddressException e1) {
										_error(e1, "Refused connection with address: ", e1.getMessage());
										getSocketIOHandler().channelException(session, e1);

										try {
											socketChannel.socket().shutdownInput();
											socketChannel.socket().shutdownOutput();
											socketChannel.close();
										} catch (IOException e2) {
											getSocketIOHandler().channelException(socketChannel, e2);
											_error(e2,
													"Additional problem with refused connection. Was not able to shut down the channel: ",
													e2.getMessage());
										}
									} catch (IOException e3) {
										getSocketIOHandler().channelException(socketChannel, e3);
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
	public void setConnectionFilter(ConnectionFilter filter) {
		__connectionFilter = filter;
	}

	@Override
	public void onSetup() {
		try {
			__initializeSockets();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRun() {
		__acceptLoop();
	}

	@Override
	public void onStop() {
		__shutDownBoundSockets();
	}

	@Override
	public String getEngineName() {
		return "acceptor";
	}

	@Override
	public void setZeroReaderListener(ZeroReaderListener zeroReaderListener) {
		__zeroReaderListener = zeroReaderListener;
	}

	@Override
	public void onInitialized() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResumed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRunning() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPaused() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroyed() {
		// TODO Auto-generated method stub

	}

}
