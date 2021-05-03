package com.tenio.core.network.entity.packet;

import java.util.Collection;

import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.ISession;

public interface IPacket {
    Object getData();

    void setData(Object var1);

    TransportType getTransportType();

    void setTransportType(TransportType var1);

    MessagePriority getPriority();

    void setPriority(MessagePriority var1);

    Collection getRecipients();

    void setRecipients(Collection var1);

    ISession getSender();

    void setSender(ISession var1);

    Object getAttribute(String var1);

    void setAttribute(String var1, Object var2);

    String getOwnerNode();

    void setOwnerNode(String var1);

    long getCreationTime();

    void setCreationTime(long var1);

    int getOriginalSize();

    void setOriginalSize(int var1);

    boolean isTcp();

    boolean isUdp();

    IPacket clone();

    byte[] getFragmentBuffer();

    void setFragmentBuffer(byte[] var1);

    boolean isFragmented();

    void setId(Short var1);

    Short getId();
}
