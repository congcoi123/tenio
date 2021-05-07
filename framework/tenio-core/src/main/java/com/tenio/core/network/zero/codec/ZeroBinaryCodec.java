package com.tenio.core.network.zero.codec;

import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.codec.AbstractProtocolCodec;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.protocol.Response;

public class ZeroBinaryCodec extends AbstractProtocolCodec {

	public ZeroBinaryCodec(InternalEventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void onPacketRead(Packet var1) {
		
	}

	@Override
	public void onPacketWrite(Response var1) {
		byte[] binData = (byte[])((byte[])response.getContent());
        ByteBuffer packetBuffer = ByteBuffer.allocate(3 + binData.length);
        packetBuffer.put((Byte)response.getTargetController());
        packetBuffer.putShort((Short)response.getId());
        packetBuffer.put(binData);
        IPacket packet = new Packet();
        packet.setId((Short)response.getId());
        packet.setTransportType(response.getTransportType());
        packet.setData(packetBuffer.array());
        packet.setRecipients(response.getRecipients());
        if (response.getRecipients().size() > 0) {
             this.logger.debug("{OUT}: " + SystemRequest.fromId(response.getId()) + " - " + response.getId());
        }

        if (response.getRecipients().size() > 0) {
             LoggerFactory.getLogger("request").debug(" {OUT} " + response.getId() + " - to : " + response.getRecipients().size());
        }

        this.ioHandler.onDataWrite(packet);
	}

}
