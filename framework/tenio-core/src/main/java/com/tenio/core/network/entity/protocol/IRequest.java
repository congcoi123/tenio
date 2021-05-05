package com.tenio.core.network.entity.protocol;

import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.ISession;

public interface IRequest extends ZeroMessage {
     TransportType getTransportType();

     void setTransportType(TransportType type);

     ISession getSender();

     void setSender(ISession session);

     MessagePriority getPriority();

     void setPriority(MessagePriority priority);

     long getTimeStamp();

     void setTimeStamp(long timestamp);

     boolean isTcp();

     boolean isUdp();

     boolean isWebsocket();
}
