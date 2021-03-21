package com.tenio.core.network.zero;

import java.net.SocketOption;

import com.tenio.core.message.packet.IPacketQueue;

public final class DefaultSocketOption {

    public static final SocketOption<IPacketQueue> PACKET_QUEUE =
            new DefSocketOption<IPacketQueue>("PACKET_QUEUE", IPacketQueue.class);
    
    private static class DefSocketOption<T> implements SocketOption<T> {
        private final String name;
        private final Class<T> type;
        DefSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }
        @Override public String name() { return name; }
        @Override public Class<T> type() { return type; }
        @Override public String toString() { return name; }
    }

}
