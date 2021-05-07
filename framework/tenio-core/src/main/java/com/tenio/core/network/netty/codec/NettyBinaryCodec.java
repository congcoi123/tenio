package com.tenio.core.network.netty.codec;

import com.tenio.core.event.internal.InternalEventManager;
import com.tenio.core.network.codec.AbstractProtocolCodec;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.protocol.Response;

public class NettyBinaryCodec extends AbstractProtocolCodec {

	public NettyBinaryCodec(InternalEventManager eventManager) {
		super(eventManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onPacketRead(Packet var1) {
		// TODO Auto-generated method stub
		
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
        
        this.getIOHandler().onDataWrite(null);
        
//        if (response.getRecipients().size() > 0 && this.logger.isDebugEnabled()) {
//             this.logger.debug("{OUT}: " + SystemRequest.fromId(response.getId()));
//        }
//
//        byte[] rawPacket = (byte[])((byte[])packet.getData());
//        if (rawPacket.length < 1024 && BitZeroServer.isDebug()) {
//             this.logger.debug(ByteUtils.fullHexDump(rawPacket));
//        }
//
//        ChannelBuffer cb = ChannelBuffers.wrappedBuffer(rawPacket);
//        Iterator var7 = response.getRecipients().iterator();
//
//        while(var7.hasNext()) {
//             Object tmp = var7.next();
//             ISession session = (ISession)tmp;
//             IWebSocketChannel channel = (IWebSocketChannel)session.getSystemProperty("wsChannel");
//             channel.write(cb);
//             session.addWrittenBytes((long)rawPacket.length);
//             int bytesLen = rawPacket.length;
//             this.webSocketStats.addWrittenPackets(1);
//             this.webSocketStats.addWrittenBytes(bytesLen);
//        }
	}

}
