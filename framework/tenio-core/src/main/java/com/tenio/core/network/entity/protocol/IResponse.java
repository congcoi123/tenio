package com.tenio.core.network.entity.protocol;

import java.util.Collection;

import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.ISession;

public interface IResponse extends ZeroMessage {
     TransportType getTransportType();

     void setTransportType(TransportType type);

     Object getTargetController();

     void setTargetController(Object controller);

     Collection getRecipients();

     void setRecipients(Collection collection);

     void setRecipients(ISession session);

     boolean isTCP();

     boolean isUDP();

     void write();

     void write(int delay);
}
