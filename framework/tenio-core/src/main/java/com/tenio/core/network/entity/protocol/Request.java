package com.tenio.core.network.entity.protocol;

import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.Session;

public interface Request extends Message {
     TransportType getTransportType();

     void setTransportType(TransportType type);

     Session getSender();

     void setSender(Session session);

     MessagePriority getPriority();

     void setPriority(MessagePriority priority);

     long getTimeStamp();

     void setTimeStamp(long timestamp);

     boolean isTcp();

     boolean isUdp();

     boolean isWebsocket();
}
