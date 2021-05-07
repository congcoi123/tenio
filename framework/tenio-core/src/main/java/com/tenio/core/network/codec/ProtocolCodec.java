package com.tenio.core.network.codec;

import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.handler.IOHandler;

public interface ProtocolCodec {
    void onPacketRead(Packet var1);

    void onPacketWrite(Response var1);

    IOHandler getIOHandler();

    void setIOHandler(IOHandler var1);
}
