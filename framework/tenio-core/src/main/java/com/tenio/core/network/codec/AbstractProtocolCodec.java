package com.tenio.core.network.codec;

import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.zero.handler.SocketIOHandler;

public abstract class AbstractProtocolCodec implements ProtocolCodec {
	
	private SocketIOHandler ioHandler;

	public AbstractProtocolCodec(InternalEventManager eventManager) {
		
	}
	
	public void emitRequest() {
		
	}
	
	@Override
	public SocketIOHandler getIOHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setIOHandler(SocketIOHandler var1) {
		// TODO Auto-generated method stub
		
	}
	
}
