package bitzero.engine.io;

import bitzero.engine.data.MessagePriority;
import bitzero.engine.data.TransportType;
import bitzero.engine.sessions.ISession;

public interface IRequest extends IEngineMessage {
     TransportType getTransportType();

     void setTransportType(TransportType var1);

     ISession getSender();

     void setSender(ISession var1);

     MessagePriority getPriority();

     void setPriority(MessagePriority var1);

     long getTimeStamp();

     void setTimeStamp(long var1);

     boolean isTcp();

     boolean isUdp();

     boolean isWebsocket();
}
