package com.tenio.core.network;

import com.tenio.core.events.EventManager;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.jetty.JettyHttpService;
import com.tenio.core.network.netty.NettyWebSocketService;
import com.tenio.core.network.netty.NettyWebSocketServiceImpl;
import com.tenio.core.network.zero.ZeroSocketService;
import com.tenio.core.network.zero.ZeroSocketServiceImpl;

public final class NetworkService {

	private JettyHttpService __httpService;
	private NettyWebSocketService __websocketService;
	private ZeroSocketService __socketService;

	public NetworkService(EventManager eventManager) {
		__httpService = JettyHttpService.newInstance(eventManager);
		__websocketService = NettyWebSocketServiceImpl.newInstance(eventManager);
		__socketService = ZeroSocketServiceImpl.newInstance(eventManager);
	}

	private void __setupHttpService() {

	}

	private void __setupWebsocketService() {

	}

	private void __setupSocketService() {

	}

	public void write(Packet packet) {
		if (packet.isWebSocket()) {
			__websocketService.write(packet);
		} else {
			__socketService.write(packet);
		}
	}

}
