package com.tenio.core.network.zero.codec;

import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.codec.AbstractProtocolCodec;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.implement.PacketImpl;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.zero.engine.EngineWriter;

public class ZeroBinaryCodec extends AbstractProtocolCodec {
	
	private EngineWriter __writer;
	private PacketEncoder __encoder;

	public ZeroBinaryCodec(InternalEventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void onPacketRead(Packet var1) {
		
	}

	@Override
	public void onPacketWrite(Response response) {
		byte[] binary = response.getContent();
		
        Packet packet = PacketImpl.newInstance();
        packet.setData(binary);
        packet.setRecipients(response.getRecipientSocketSessions());
        
        packet = __encoder.encode(packet);
        
        __writer.enqueuePacket(packet);
	}

}
