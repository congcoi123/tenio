package com.tenio.core.network.entity.protocol;

import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.ISession;

public final class Request extends AbstractMessage implements IRequest {
     private ISession sender;
     private TransportType type;
     private MessagePriority priority;
     private long timeStamp;

     public Request() {
          this.type = TransportType.TCP;
          this.priority = MessagePriority.NORMAL;
          this.timeStamp = System.nanoTime();
     }

     public ISession getSender() {
          return this.sender;
     }

     public TransportType getTransportType() {
          return this.type;
     }

     public void setSender(ISession session) {
          this.sender = session;
     }

     public void setTransportType(TransportType type) {
          this.type = type;
     }

     public MessagePriority getPriority() {
          return this.priority;
     }

     public void setPriority(MessagePriority priority) {
          this.priority = priority;
     }

     public long getTimeStamp() {
          return this.timeStamp;
     }

     public void setTimeStamp(long timeStamp) {
          this.timeStamp = timeStamp;
     }

     public boolean isTcp() {
          return this.type == TransportType.TCP;
     }

     public boolean isUdp() {
          return this.type == TransportType.UDP;
     }

     public boolean isWebsocket() {
    	 return this.type == TransportType.WEB_SOCKET;
     }

}
