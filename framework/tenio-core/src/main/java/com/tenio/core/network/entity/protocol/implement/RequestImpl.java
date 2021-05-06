package com.tenio.core.network.entity.protocol.implement;

import com.tenio.core.network.define.MessagePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.protocol.Request;

public final class RequestImpl extends AbstractMessage implements Request {
     private Session sender;
     private TransportType type;
     private MessagePriority priority;
     private long timeStamp;

     public RequestImpl() {
          this.type = TransportType.TCP;
          this.priority = MessagePriority.NORMAL;
          this.timeStamp = System.nanoTime();
     }

     public Session getSender() {
          return this.sender;
     }

     public TransportType getTransportType() {
          return this.type;
     }

     public void setSender(Session session) {
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
