package com.tenio.core.network.codec;

import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.handler.IOHandler;

public abstract class AbstractProtocolCodec implements ProtocolCodec {
	
	private IOHandler ioHandler;

	public AbstractProtocolCodec(InternalEventManager eventManager) {
		
	}
	
	public void emitRequest() {
		
	}
	
	@Override
	public IOHandler getIOHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setIOHandler(IOHandler var1) {
		// TODO Auto-generated method stub
		
	}
	
}
