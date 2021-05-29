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
package com.tenio.core.network;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import com.tenio.common.data.implement.ZeroObjectImpl;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.defines.data.PathConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.packet.implement.PacketImpl;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.entities.protocols.Response;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.entities.session.implement.SessionManagerImpl;
import com.tenio.core.network.jetty.JettyHttpService;
import com.tenio.core.network.netty.NettyWebSocketService;
import com.tenio.core.network.netty.NettyWebSocketServiceImpl;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.network.zero.ZeroSocketService;
import com.tenio.core.network.zero.ZeroSocketServiceImpl;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;

public final class NetworkServiceImpl extends AbstractManager implements NetworkService {

	private JettyHttpService __httpService;
	private NettyWebSocketService __websocketService;
	private ZeroSocketService __socketService;
	private SessionManager __sessionManager;
	private NetworkReaderStatistic __networkReaderStatistic;
	private NetworkWriterStatistic __networkWriterStatistic;

	private boolean __initialized;

	private boolean __httpServiceInitialized;
	private boolean __websocketServiceInitialized;
	private boolean __socketServiceInitialized;

	public static NetworkService newInstance(EventManager eventManager) {
		return new NetworkServiceImpl(eventManager);
	}

	private NetworkServiceImpl(EventManager eventManager) {
		super(eventManager);

		__initialized = false;

		__httpServiceInitialized = false;
		__websocketServiceInitialized = false;
		__socketServiceInitialized = false;

		__sessionManager = SessionManagerImpl.newInstance(eventManager);
		__networkReaderStatistic = NetworkReaderStatistic.newInstannce();
		__networkWriterStatistic = NetworkWriterStatistic.newInstance();

		__httpService = JettyHttpService.newInstance(eventManager);
		__websocketService = NettyWebSocketServiceImpl.newInstance(eventManager);
		__socketService = ZeroSocketServiceImpl.newInstance(eventManager);
	}

	@Override
	public void initialize() {
		__initializeServices();
		__initialized = true;
	}

	private void __initializeServices() {
		__websocketService.setSessionManager(__sessionManager);
		__websocketService.setNetworkReaderStatistic(__networkReaderStatistic);
		__websocketService.setNetworkWriterStatistic(__networkWriterStatistic);

		__socketService.setSessionManager(__sessionManager);
		__socketService.setNetworkReaderStatistic(__networkReaderStatistic);
		__socketService.setNetworkWriterStatistic(__networkWriterStatistic);

		if (__httpServiceInitialized) {
			__httpService.initialize();
		}
		if (__websocketServiceInitialized) {
			__websocketService.initialize();
		}
		if (__socketServiceInitialized) {
			__socketService.initialize();
		}
	}

	@Override
	public void start() {
		__httpService.start();
		__websocketService.start();
		__socketService.start();
	}

	@Override
	public void shutdown() {
		if (!__initialized) {
			return;
		}
		__shutdown();
	}

	private void __shutdown() {
		__httpService.shutdown();
		__websocketService.shutdown();
		__socketService.shutdown();

		__destroy();
	}

	private void __destroy() {
		__httpService = null;
		__websocketService = null;
		__socketService = null;

		__networkReaderStatistic = null;
		__networkWriterStatistic = null;
	}

	@Override
	public boolean isActivated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return "network";
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHttpPort(int port) {
		__httpService.setPort(port);
	}

	@Override
	public void setHttpPathConfigs(List<PathConfig> pathConfigs) {
		if (pathConfigs == null) {
			return;
		}
		__httpService.setPathConfigs(pathConfigs);
		__httpServiceInitialized = true;
	}

	@Override
	public void setConnectionFilterClass(Class<? extends ConnectionFilter> clazz, int maxConnectionsPerIp)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		var connectionFilter = clazz.getDeclaredConstructor().newInstance();
		connectionFilter.setMaxConnectionsPerIp(maxConnectionsPerIp);

		__websocketService.setConnectionFilter(connectionFilter);
		__socketService.setConnectionFilter(connectionFilter);
	}

	@Override
	public void setWebsocketConsumerWorkers(int workerSize) {
		__websocketService.setConsumerWorkerSize(workerSize);
	}

	@Override
	public void setWebsocketProducerWorkers(int workerSize) {
		__websocketService.setProducerWorkerSize(workerSize);
	}

	@Override
	public void setWebsocketSenderBufferSize(int bufferSize) {
		__websocketService.setSenderBufferSize(bufferSize);
	}

	@Override
	public void setWebsocketReceiverBufferSize(int bufferSize) {
		__websocketService.setReceiverBufferSize(bufferSize);
	}

	@Override
	public void setWebsocketUsingSSL(boolean usingSSL) {
		__websocketService.setUsingSSL(usingSSL);
	}

	@Override
	public void setSocketAcceptorWorkers(int workerSize) {
		__socketService.setAcceptorWorkerSize(workerSize);
	}

	@Override
	public void setSocketReaderWorkers(int workerSize) {
		__socketService.setReaderWorkerSize(workerSize);
	}

	@Override
	public void setSocketWriterWorkers(int workerSize) {
		__socketService.setWriterWorkerSize(workerSize);
	}

	@Override
	public void setSocketAcceptorBufferSize(int bufferSize) {
		__socketService.setAcceptorBufferSize(bufferSize);
	}

	@Override
	public void setSocketReaderBufferSize(int bufferSize) {
		__socketService.setReaderBufferSize(bufferSize);
	}

	@Override
	public void setSocketWriterBufferSize(int bufferSize) {
		__socketService.setWriterBufferSize(bufferSize);
	}

	@Override
	public void setSocketConfigs(List<SocketConfig> socketConfigs) {
		if (__containsSocketPort(socketConfigs)) {
			__socketServiceInitialized = true;
			__socketService.setSocketConfigs(socketConfigs);
		}

		if (__containsWebsocketPort(socketConfigs)) {
			__websocketServiceInitialized = true;
			__websocketService.setWebSocketConfig(socketConfigs.stream()
					.filter(socketConfig -> socketConfig.getType() == TransportType.WEB_SOCKET).findFirst().get());
		}
	}

	private boolean __containsSocketPort(List<SocketConfig> socketConfigs) {
		return socketConfigs.stream().filter(socketConfig -> socketConfig.getType() == TransportType.TCP
				|| socketConfig.getType() == TransportType.UDP).findFirst().isPresent();
	}

	private boolean __containsWebsocketPort(List<SocketConfig> socketConfigs) {
		return socketConfigs.stream().filter(socketConfig -> socketConfig.getType() == TransportType.WEB_SOCKET)
				.findFirst().isPresent();
	}

	@Override
	public void setPacketQueuePolicy(Class<? extends PacketQueuePolicy> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		__sessionManager.setPacketQueuePolicy(clazz);
	}

	@Override
	public void setPacketQueueSize(int queueSize) {
		__sessionManager.setPacketQueueSize(queueSize);
	}

	@Override
	public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
		__socketService.setPacketEncoder(packetEncoder);
	}

	@Override
	public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
		__socketService.setPacketDecoder(packetDecoder);
	}

	@Override
	public NetworkReaderStatistic getNetworkReaderStatistic() {
		return __networkReaderStatistic;
	}

	@Override
	public NetworkWriterStatistic getNetworkWriterStatistic() {
		return __networkWriterStatistic;
	}

	@Override
	public void write(Response response) {
		var data = ZeroObjectImpl.newInstance(response.getContent());
		var message = ServerMessage.newInstance().setData(data);

		var playerIterator = response.getPlayers().iterator();
		while (playerIterator.hasNext()) {
			var player = playerIterator.next();
			__eventManager.emit(ServerEvent.SEND_MESSAGE_TO_PLAYER, player, message);
		}

		var nonSessionPlayers = response.getNonSessionPlayers();
		if (nonSessionPlayers != null) {
			var nonSessionIterator = nonSessionPlayers.iterator();
			while (nonSessionIterator.hasNext()) {
				var player = nonSessionIterator.next();
				__eventManager.emit(ServerEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, message);
			}
		}

		var socketSessions = response.getRecipientSocketSessions();
		var datagramSessions = response.getRecipientDatagramSessions();
		var websocketSessions = response.getRecipientWebSocketSessions();

		if (socketSessions != null) {
			Packet packet = __createPacket(response, socketSessions, TransportType.TCP);
			__socketService.write(packet);
		}

		if (datagramSessions != null) {
			Packet packet = __createPacket(response, datagramSessions, TransportType.UDP);
			__socketService.write(packet);
		}

		if (websocketSessions != null) {
			Packet packet = __createPacket(response, websocketSessions, TransportType.WEB_SOCKET);
			__websocketService.write(packet);
		}
	}

	private Packet __createPacket(Response response, Collection<Session> recipients, TransportType transportType) {
		Packet packet = PacketImpl.newInstance();
		packet.setData(response.getContent());
		packet.setEncrypted(response.isEncrypted());
		packet.setPriority(response.getPriority());
		packet.setRecipients(recipients);
		packet.setTransportType(transportType);

		return packet;
	}

}
