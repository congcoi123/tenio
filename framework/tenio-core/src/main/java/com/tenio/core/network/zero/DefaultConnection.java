package com.tenio.core.network.zero;

import java.net.InetSocketAddress;

import com.tenio.common.element.CommonObject;
import com.tenio.core.configuration.define.TransportType;
import com.tenio.core.event.IEventManager;
import com.tenio.core.network.Connection;
import com.tenio.core.network.IConnection;

public final class DefaultConnection extends Connection {

	public DefaultConnection(IEventManager eventManager, TransportType type, int index) {
		super(eventManager, type, index);
	}

	@Override
	public void send(CommonObject packet) {
	}

	@Override
	public void close() {
	}

	@Override
	public void clean() {
	}

	@Override
	public void setRemote(InetSocketAddress remote) {
	}

	@Override
	public IConnection getThis() {
		return null;
	}

	@Override
	public void setThis() {
	}

	@Override
	public void removeThis() {
	}

}
